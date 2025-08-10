package psu.signlinguamobile.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

import psu.signlinguamobile.R;
import psu.signlinguamobile.data.UCropActivityResultHandler;
import psu.signlinguamobile.delegates.UCropCallback;
import psu.signlinguamobile.managers.ProfilePictureManager;

public class PickedFromGalleryPreviewer extends AppCompatActivity
{
    private LinearLayout processingOverlay;
    private ImageView previewer;
    private ProfilePictureManager picMan;
    private Button btnRetry;
    private Button btnProceed;
    private File mref_croppedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_picked_from_gallery_previewer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRetry    = findViewById(R.id.btn_previewer_retry);
        btnProceed  = findViewById(R.id.btn_previewer_proceed);
        previewer   = findViewById(R.id.capture_previewer);

        processingOverlay = findViewById(R.id.overlay_on_process);

        picMan = new ProfilePictureManager(this);

        picMan.onSubmitBegan  = () -> processingOverlay.setVisibility(View.VISIBLE);
        picMan.onSubmitEnded  = () -> processingOverlay.setVisibility(View.INVISIBLE);
        picMan.onFailure      = (msg) -> alert(msg, "Failure");
        picMan.onNetworkError = (msg) -> alert(msg, "Network Error");

        picMan.onSuccess      = (updatedPhotoUrl) -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedPhotoUrl", updatedPhotoUrl);
            setResult(RESULT_OK, resultIntent);
            finish();
        };

        Uri uri = getIntent().getParcelableExtra("photoUri");

        if (uri != null && "file".equals(uri.getScheme()))
        {
            File file = new File(uri.getPath());

            if (file.exists() && file.length() > 0)
            {
                launchCropper(uri);
            }
            else
            {
                // Go back to previous activity
                alert("Sorry, the image file is missing or unreadable. Please try again.", "Failure", (dialogInterface, i) -> finish());
            }
        }

        btnProceed.setOnClickListener(click -> {
            if (mref_croppedFile == null)
            {
                alert("Submit failed. No cropped image to upload.", "Error");
                return;
            }

            picMan.submitProfilePicture(mref_croppedFile);
        });

        btnRetry.setOnClickListener(click -> {
            if (mref_croppedFile == null)
            {
                alert("Oops! The cropped image is corrupt or unreadable. Please try again.", "Error");
                return;
            }

            launchCropper(uri);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
        {
            // Go back to the last activity
            finish();
            return;
        }

        UCropActivityResultHandler.handleResult(requestCode, resultCode, data, this);
    }

    private void launchCropper(Uri uri)
    {
        UCropActivityResultHandler.setCallback(new UCropCallback()
        {
            @Override
            public void onCropSuccess(File croppedFile) {

                mref_croppedFile = croppedFile;

                // We must first preview the cropped image to make sure the user
                // has been satisfied of the results. If not, we allow him to
                // pick another image and relaunch UCrop accordingly
                previewer.setImageURI(null); // Clear first
                previewer.setImageURI(Uri.fromFile(croppedFile));
                previewer.invalidate();      // Force redraw
            }

            @Override
            public void onCropFailure(String errorMessage) {
                alert(errorMessage, "Upload Error");
            }
        });

        picMan.launchCrop(PickedFromGalleryPreviewer.this, uri);
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
}