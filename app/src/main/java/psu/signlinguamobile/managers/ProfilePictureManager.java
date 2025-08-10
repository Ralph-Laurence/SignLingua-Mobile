package psu.signlinguamobile.managers;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.api.apiservice.TutorManagementApiService;
import psu.signlinguamobile.api.client.ApiClient;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePictureManager
{
    private final TutorManagementApiService tutorMgtApiService;
    private final Context m_context;
    private final int SQUARE_CROP_SIZE = 512;

    public Consumer<String> onFailure;
    public Consumer<String> onSuccess;
    public Consumer<String> onNetworkError;
    public Runnable onSubmitBegan;
    public Runnable onSubmitEnded;

    public ProfilePictureManager(Context context)
    {
        m_context = context;
        // Initialize Retrofit API Service
        tutorMgtApiService = ApiClient.getClient(m_context, true).create(TutorManagementApiService.class);
    }

    public void launchCrop(Activity fromActivity, Uri sourceUri)
    {
        File tempProfilePath = new File(m_context.getExternalFilesDir(null), "temp_profile");

        if (!tempProfilePath.exists())
            tempProfilePath.mkdirs();

        Uri destinationUri = Uri.fromFile(new File(tempProfilePath, "cropped.jpg"));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(SQUARE_CROP_SIZE, SQUARE_CROP_SIZE) // was originally 800
                .start(fromActivity);
    }

    public void submitProfilePicture(File file)
    {
        if (onSubmitBegan != null)
            onSubmitBegan.run();

        Log.d("MINE", "Started submit");
        // Use MediaType.parse() if you're not using Kotlin extensions
        MediaType mediaType = MediaType.parse("image/*");

        // Use the newer asRequestBody() method
        RequestBody requestBody = RequestBody.create(file, mediaType); // OkHttp 4.x style

        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

        tutorMgtApiService.uploadProfilePicture(body).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<User>> call, Response<CommonResponse<User>> response)
            {
                if (onSubmitEnded != null)
                    onSubmitEnded.run();

                CommonResponse<User> res = response.body();

                if (!response.isSuccessful() || res == null)
                {
                    if (onFailure != null)
                        onFailure.accept(UXMessages.ERR_TECHNICAL);
                    // alert(UXMessages.ERR_TECHNICAL, "Failure");
                    return;
                }

                String msg = res.getMessage();

                // Log.d("MINE", "Status: " + res.getStatus());
                if (res.getStatus() != HttpCodes.SUCCESS)
                {
                    if (onFailure != null)
                        onFailure.accept("Oops! We ran into a problem while trying to update your profile picture.");

                    // alert("Oops! We ran into a problem while trying to update your profile picture.", "Failure");
                    return;
                }

                if (onSuccess != null)
                    onSuccess.accept(res.getContent().getPhoto());
            }

            @Override
            public void onFailure(Call<CommonResponse<User>> call, Throwable t)
            {
                if (onSubmitEnded != null)
                    onSubmitEnded.run();

                if (onNetworkError != null)
                    onNetworkError.accept(UXMessages.ERR_NETWORK); // (UXMessages.ERR_NETWORK, "Network Error")
            }
        });
    }
}
