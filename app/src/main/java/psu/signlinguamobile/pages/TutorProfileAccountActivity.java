package psu.signlinguamobile.pages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import psu.signlinguamobile.R;
import psu.signlinguamobile.api.apirequest.UpdateProfileAccountRequest;
import psu.signlinguamobile.api.apiresponse.CommonResponse;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.data.TutorProfileBannerCache;
import psu.signlinguamobile.delegates.TutorProfileAccountJsBridge;
import psu.signlinguamobile.managers.SessionManager;
import psu.signlinguamobile.models.UpdatedProfileAccount;
import psu.signlinguamobile.models.User;
import psu.signlinguamobile.utilities.HttpCodes;
import psu.signlinguamobile.utilities.UXMessages;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutorProfileAccountActivity
       extends BaseTutorProfileActivity
       implements TutorProfileAccountJsBridge.TutorProfileAccountJsBridgeListener
{
    //=================================================
    // <editor-fold desc="SECTION: LIFECYCLE METHODS">
    //=================================================

    private ActivityResultLauncher<String> galleryPicker;
    private ActivityResultLauncher<Intent> profileImagePreviewer;

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        shouldUpdateRibbonOnLoad(false);
        cacheRibbonDetailsOnFetch(true);

        // The activity that handles the previewing, cropping, and submission of the selected image.
        // Upon successful submit, this should return updated photo url
        profileImagePreviewer = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK)
                    {
                        Intent data = result.getData();
                        if (data != null)
                        {
                            String updatedPhoto = data.getStringExtra("updatedPhotoUrl");
                            // Use the returned value here
                            bridgeCall_function("renderProfilePicture", updatedPhoto );
                        }
                    }
                }
        );

        galleryPicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {

            if (uri != null)
            {
                // Upon selecting a photo from gallery, copy it to a temporary file
                File tempProfilePath = new File(getExternalFilesDir(null), "temp_profile");
                if (!tempProfilePath.exists()) tempProfilePath.mkdirs();

                File photoFile = new File(tempProfilePath, "captured.jpg");

                // The image selected from gallery is a "content://" uri stream and not an actual file.
                // We must copy its stream to an output file before we can access its file uri.
                try (InputStream inputStream = getContentResolver().openInputStream(uri);
                     FileOutputStream outputStream = new FileOutputStream(photoFile))
                {

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    // Proceed with uCrop or preview
                    Uri captureUri = Uri.fromFile(photoFile);

                    Log.d("MINE", "SELECTED: " + captureUri.toString());
//                    Intent intent = new Intent(this, PickedFromGalleryPreviewer.class);
//
//                    intent.putExtra("launchedFrom", "tutorProfile");
//                    intent.putExtra("photoUri", captureUri);
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    startActivity(intent);

                    Intent intent = new Intent(this, PickedFromGalleryPreviewer.class);

                    intent.putExtra("launchedFrom", "tutorProfile");
                    intent.putExtra("photoUri", captureUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    profileImagePreviewer.launch(intent);
                }
                catch (Exception e)
                {
                    Log.d("MINE", "Stream copy failed: " + e.getMessage());
                    bridgeCall_alertWarn("Sorry, the selected image is unreadable or not supported. Please try again.");
                }
            }
        });

        registerJsBridge(new TutorProfileAccountJsBridge(this), JS_BRIDGE_NAME);

        renderView("tutor_myprofile_account");
    }

    @Override
    protected void onBackKey()
    {
        launch(TutorHomeActivity.class);
    }

    @Override
    protected void onViewLoaded()
    {
        fetchProfileDetails(getApiService().fetchAccountDetails(), null);
    }

    @Override
    protected void onDispose()
    {
        unregisterJsBridge(JS_BRIDGE_NAME); // Prevents leaks
        navController.removeBridge();
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: JS BRIDGE METHODS">
    //=================================================

    @Override
    public void onGoBack()
    {
        runOnUiThread(() -> launch(TutorHomeActivity.class));
    }

    @Override
    public void onCapturePhoto()
    {
        Intent intent = new Intent(this, CapturePhotoActivity.class);

        intent.putExtra("launchedFrom", "tutorProfile");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        profileImagePreviewer.launch(intent);
    }

    @Override
    public void onSelectGalleryPhoto()
    {
        galleryPicker.launch("image/*");
    }

    @Override
    public void onSaveProfile(String email, String username, String bio)
    {
        runOnUiThread(() -> performUpdate(email, username, bio));
    }

    @Override
    public void onRemoveProfilePic()
    {
        getApiService().removeProfilePicture().enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<User>> call, Response<CommonResponse<User>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                CommonResponse<User> res = response.body();

                if (!response.isSuccessful() || res == null) {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                    return;
                }

                String msg = res.getMessage();

                if (res.getStatus() != HttpCodes.SUCCESS) {
                    bridgeCall_alertWarn(msg);
                    return;
                }

                User user = res.getContent();

                // Update the cached user picture
                Gson gson = new Gson();
                String userJson = gson.toJson(user);

                // Save updated user JSON:
                SharedPreferences prefs = getSharedPreferences(Constants.SharedPrefKeys.AUTH, Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.SharedPrefKeys.USER_DETAILS, userJson).commit();

                // Also cache in session manager:
                SessionManager.getInstance().saveSession(user, null);
                TutorProfileBannerCache.store(null, user.getPhoto());
                Log.d("MINE", "Removed photo fallback to: " +  user.getPhoto());
                bridgeCall_function("renderProfilePicture", user.getPhoto() );
            }

            @Override
            public void onFailure(Call<CommonResponse<User>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="SECTION: BUSINESS LOGIC">
    //=================================================

    private void performUpdate(String email, String username, String bio)
    {
        UpdateProfileAccountRequest request = new UpdateProfileAccountRequest(email, username, bio);

        getApiService().updateProfileAccount(request).enqueue(new Callback<>()
        {
            @Override
            public void onResponse(Call<CommonResponse<UpdatedProfileAccount>> call, Response<CommonResponse<UpdatedProfileAccount>> response)
            {
                bridgeCall_hideWebViewLoadingOverlay();

                CommonResponse<UpdatedProfileAccount> res = response.body();

                if (!response.isSuccessful() || res == null) {
                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
                    return;
                }

                String msg = res.getMessage();

                if (res.getStatus() != HttpCodes.SUCCESS) {
                    bridgeCall_alertWarn(msg);
                    return;
                }

                UpdatedProfileAccount updatedData = res.getContent();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("username", updatedData.getUsername());
                responseData.put("email", updatedData.getEmail());
                responseData.put("bio", updatedData.getBio());
                responseData.put("msg", msg);

                bridgeCall_execJavascriptFunction("renderUpdatedDetails", encodeJson(responseData));
            }

            @Override
            public void onFailure(Call<CommonResponse<UpdatedProfileAccount>> call, Throwable t)
            {
                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
            }
        });
    }


//    private void alert(String message, String title)
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(false);
//        builder.setTitle(title.isEmpty() ? getString(R.string.app_name) : title);
//        builder.setMessage(message);
//        builder.setPositiveButton("OK", null);
//        builder.setIcon(R.drawable.app_logo_xl);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//    private void fetchProfileDetails()
//    {
//        getApiService().fetchAccountDetails().enqueue(new Callback<>()
//        {
//            @Override
//            public void onResponse(Call<TutorProfileDetailsResponse> call, Response<TutorProfileDetailsResponse> response)
//            {
//                bridgeCall_hideWebViewLoadingOverlay();
//
//                if (!response.isSuccessful() || response.body() == null) {
//                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
//                    return;
//                }
//
//                TutorProfileBannerCache.store(response.body().getUsername(), response.body().getPhoto());
//
//                try
//                {
//                    String jsonResponse = encodeJson(response.body());
//
//                    bridgeCall_execJavascriptFunction("renderDetails", jsonResponse);
//
//                }
//                catch (Exception e)
//                {
//                    bridgeCall_alertWarn(UXMessages.ERR_TECHNICAL);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<TutorProfileDetailsResponse> call, Throwable t)
//            {
//                bridgeCall_alertWarn(UXMessages.ERR_NETWORK);
//            }
//        });
//    }

    //=================================================
    // </editor-fold>
    //=================================================
}