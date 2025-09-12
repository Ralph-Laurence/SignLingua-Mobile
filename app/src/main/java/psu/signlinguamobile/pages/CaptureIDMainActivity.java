package psu.signlinguamobile.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.RegistrationApiService;
import psu.signlinguamobile.api.apiservice.TutorManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.BmpUtils;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity handles the submission of the id into the server.
 */
public class CaptureIDMainActivity extends AppCompatActivity
{
    private ImageView previewerFrontId;
    private ImageView previewerBackId;
    private Button btnBeginCaptureFront;
    private Button btnBeginCaptureBack;
    private Button btnSubmitVerification;
    private LinearLayout loadingOverlay;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private File frontIdFile;
    private File backIdFile;

    private RegistrationApiService registrationApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_capture_idmain);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewerFrontId        = findViewById(R.id.previewer_front_id);
        previewerBackId         = findViewById(R.id.previewer_back_id);
        btnBeginCaptureFront    = findViewById(R.id.button_capture_front);
        btnBeginCaptureBack     = findViewById(R.id.button_capture_back);
        btnSubmitVerification   = findViewById(R.id.button_submit);
        loadingOverlay          = findViewById(R.id.loading_overlay);

        registrationApiService = ApiClient.getClient(this, true)
                .create(RegistrationApiService.class);

        btnSubmitVerification.setOnClickListener(v -> {

            // Ensure both files are present
            if (frontIdFile == null) {
                alert("Please take a photo of the front side of your ID.", "Verification Failed");
                return;
            }

            if (backIdFile == null) {
                alert("Please take a photo of the back side of your ID.", "Verification Failed");
                return;
            }

            if (!frontIdFile.exists()) {
                alert("The front ID photo appears to be corrupt or unreadable. Please retake the photo.", "Verification Failed");
                return;
            }

            if (!backIdFile.exists()) {
                alert("The back ID photo appears to be corrupt or unreadable. Please retake the photo.", "Verification Failed");
                return;
            }

            runOnUiThread(() -> submitVerification(frontIdFile, backIdFile));
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    {
                        String returnedData = result.getData().getStringExtra("result");
                        Log.d("MINE", "Returned: " + returnedData);

                        if (returnedData == null && returnedData.isEmpty())
                            return;

                        // Write the photo file with correction applied
                        File tempPath = new File(getExternalFilesDir(null), "temp_verification");

                        if (!tempPath.exists())
                            tempPath.mkdirs();

                        Uri returnedUri = Uri.parse(returnedData);
                        File photoFile = new File(returnedUri.getPath());

                        BmpUtils bmpUtils = new BmpUtils();
                        Bitmap preview = bmpUtils.rotateToLandscapeLeft(photoFile);

                        if (returnedData.contains("front"))
                        {
                            frontIdFile = photoFile;
                            previewerFrontId.setImageBitmap(preview);
                        }

                        else if (returnedData.contains("back"))
                        {
                            backIdFile = photoFile;
                            previewerBackId.setImageBitmap(preview);
                        }

                    }
                });

        btnBeginCaptureFront.setOnClickListener(v -> {
            HashMap<String, String> extras = new HashMap<>();
            extras.put("captureFilename", "id_front.jpg");

            launch(CaptureIDCameraActivity.class, extras);
        });

        btnBeginCaptureBack.setOnClickListener(v -> {
            HashMap<String, String> extras = new HashMap<>();
            extras.put("captureFilename", "id_back.jpg");

            launch(CaptureIDCameraActivity.class, extras);
        });
    }

    protected <T> void launch(Class<T> activity, HashMap<String, String> extras)
    {
        Intent intent = new Intent(CaptureIDMainActivity.this, activity);

        if (extras != null)
        {
            for (Map.Entry<String, String> kvp : extras.entrySet())
            {
                intent.putExtra(kvp.getKey(), kvp.getValue());
            }
        }

        cameraLauncher.launch(intent);
    }

    private void alert(String message, String title)
    {
        alert(message, title, null);
    }

    private void alert(String message, String title, DialogInterface.OnClickListener onOK)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title.isEmpty() ? getString(R.string.app_name) : title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", onOK);
        builder.setIcon(R.drawable.app_logo_xl);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void submitVerification(File frontIdFile, File backIdFile)
    {
        showLoading();

        MediaType mediaType = MediaType.parse("image/*");

        RequestBody frontRequestBody = RequestBody.create(frontIdFile, mediaType);
        RequestBody backRequestBody  = RequestBody.create(backIdFile, mediaType);

        MultipartBody.Part frontId = MultipartBody.Part.createFormData(
                "front_id", frontIdFile.getName(), frontRequestBody);

        MultipartBody.Part backId = MultipartBody.Part.createFormData(
                "back_id", backIdFile.getName(), backRequestBody);

       // RequestBody methodOverride = RequestBody.create("PATCH", MediaType.parse("text/plain"));

        registrationApiService.verifyAccountByID(frontId, backId).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<Void>> call, Response<CommonResponse<Void>> response)
            {
                hideLoading();

                if (!response.isSuccessful())
                {
                    try
                    {
                        String errorJson = response.errorBody().string(); // Only call .string() once
                        Log.d("MINE", "Raw error: " + errorJson);

                        Gson gson = new Gson();
                        CommonResponse<Void> errorResponse = gson.fromJson(errorJson, new TypeToken<CommonResponse<Void>>(){}.getType());

                        if (response.code() == HttpCodes.VALIDATION_ERROR)
                            alert(errorResponse.getMessage(), "Validation Error");

                        else
                            alert(UXMessages.ERR_TECHNICAL, "Failure");

                    }
                    catch (IOException e)
                    {
                        Log.e("MINE", "Error parsing response", e);
                        alert("Oops! We ran into a problem while trying to verify your account.", "Failure");
                    }

                    return;
                }

                CommonResponse<Void> res = response.body();
                String msg = res.getMessage();

                alert(msg, "Verification", (dialogInterface, i) -> launch(UnderModReviewActivity.class, null));

            }

            @Override
            public void onFailure(Call<CommonResponse<Void>> call, Throwable t)
            {
                hideLoading();
                alert(UXMessages.ERR_NETWORK, "Network Error");
            }
        });
    }

    public void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }
}