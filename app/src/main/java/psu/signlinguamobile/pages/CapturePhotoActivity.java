package psu.signlinguamobile.pages;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import psu.signlinguamobile.R;
import psu.signlinguamobile.data.Constants;
import psu.signlinguamobile.data.UCropActivityResultHandler;
import psu.signlinguamobile.delegates.UCropCallback;
import psu.signlinguamobile.managers.ProfilePictureManager;

public class CapturePhotoActivity extends AppCompatActivity
{
    private Executor mainExecutor;
    private ImageCapture imageCapture;
    private PreviewView cameraPreviewView;
    private GestureDetector gestureDetector;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private LinearLayout hintLayout;
    private RelativeLayout previewOverlay;
    private LinearLayout overlayOnCapture;
    private ImageView capturePreviewer;
    private Button btnHintGotIt;
    private Button btnRetryCapture;
    private Button btnProceedPicture;
    private AudioManager shutterAudio;
    private Uri capturedPhotoUri;
    private boolean isFrontCamera = true;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 211;
    private final String[] requiredPerms = new String[]
            {
                    Manifest.permission.CAMERA,
                    //Manifest.permission.RECORD_AUDIO
            };

    private ProfilePictureManager picMan;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_capture_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Modernized approach to handle back key press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                onGoBack();
            }
        });

        shutterAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestPerms();
    }

    private void initSetup()
    {
        hintLayout          = findViewById(R.id.hint_layout);
        btnRetryCapture     = findViewById(R.id.btn_previewer_retry);
        btnProceedPicture   = findViewById(R.id.btn_previewer_proceed);
        btnHintGotIt        = findViewById(R.id.btn_hint_got_it);
        cameraPreviewView   = findViewById(R.id.previewView);
        previewOverlay      = findViewById(R.id.capture_previewer_overlay);
        capturePreviewer    = findViewById(R.id.capture_previewer);
        overlayOnCapture    = findViewById(R.id.overlay_on_process);

        picMan = new ProfilePictureManager(this);
        picMan.onNetworkError = (msg) -> alert(msg, "Network Error");
        picMan.onFailure      = (msg) -> alert(msg, "Failure");
        picMan.onSubmitBegan  = () -> showOverlayOnCapture(true);
        picMan.onSubmitEnded  = () -> showOverlayOnCapture(false);
        picMan.onSuccess      = (updatedPhotoUrl) -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedPhotoUrl", updatedPhotoUrl);
            setResult(RESULT_OK, resultIntent);
            finish();
        };

        mainExecutor = ContextCompat.getMainExecutor(this);

        startCamera();
        setupCaptureButton(); // Add capture logic

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener()
        {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)
                {
                    // Swipe down (> 0) : front cam
                    // Swipe up (< 0): back cam
                    boolean useFrontCam = (diffY > 0);
                    switchCamera(useFrontCam);

                    return true;
                }
                return false;
            }
        });

        //previewView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        cameraPreviewView.setOnTouchListener((v, event) -> {
            boolean handled = gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick(); // For accessibility compliance
            }
            return handled;
        });

        btnHintGotIt.setOnClickListener(click -> hintLayout.setVisibility(View.GONE));
        btnRetryCapture.setOnClickListener(click -> {
            previewOverlay.setVisibility(View.INVISIBLE);
            showOverlayOnCapture(false);
        });
        btnProceedPicture.setOnClickListener(click -> {
            if (capturedPhotoUri == null)
            {
                alert("Oops! Something went wrong while processing the photo. Please try again.", "Change photo");
                return;
            }

            UCropActivityResultHandler.setCallback(new UCropCallback()
            {
                @Override
                public void onCropSuccess(File croppedFile) {
                    picMan.submitProfilePicture(croppedFile);
                }

                @Override
                public void onCropFailure(String errorMessage) {
                    alert(errorMessage, "Upload Error");
                }
            });

            picMan.launchCrop(this, capturedPhotoUri);

        });


    }

    private void onGoBack()
    {
        Intent intent = null;

        String launchedFrom = getIntent().getStringExtra("launchedFrom");

        if (launchedFrom.equals("learnerProfile"))
            intent = new Intent(CapturePhotoActivity.this, LearnerProfileActivity.class);

        else if (launchedFrom.equals("tutorProfile"))
            intent = new Intent(CapturePhotoActivity.this, TutorProfileAccountActivity.class);

        if (intent != null)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void alert(String message, String title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title.isEmpty() ? getString(R.string.app_name) : title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setIcon(R.drawable.app_logo_xl);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        UCropActivityResultHandler.handleResult(requestCode, resultCode, data, this);
    }

    //=================================================
    // <editor-fold desc="Overlays">
    //=================================================
    private void showHint()
    {
        ImageView hintFinger = findViewById(R.id.hint_indicator_finger);

        hintLayout.setVisibility(View.VISIBLE);
        hintFinger.setPivotX(hintFinger.getWidth());
        hintFinger.setPivotY(hintFinger.getHeight());

        new Handler(Looper.getMainLooper())
                .postDelayed(() -> startSwingAnimation(hintFinger), 1200);

    }

    private void showOverlayOnCapture(boolean show)
    {
        if (show)
        {
            overlayOnCapture.setAlpha(0f);
            overlayOnCapture.setVisibility(View.VISIBLE); // Make it visible but still transparent
            overlayOnCapture.animate()
                    .alpha(1f)
                    .setDuration(300) // or whatever duration feels right
                    .start();
        }
        else
        {
            overlayOnCapture.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        overlayOnCapture.setVisibility(View.GONE); // or INVISIBLE if needed
                    })
                    .start();
        }
    }

    private void startSwingAnimation(ImageView hintFinger)
    {
        int swingCount = 5;
        long singleDuration = 200; // ms per swing

        AnimatorSet swingSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();

        for (int i = 0; i < swingCount; i++) {
            float targetRotation = (i % 2 == 0) ? -20f : 20f; // Alternate directions

            ObjectAnimator swing = ObjectAnimator.ofFloat(hintFinger, "rotation", targetRotation);
            swing.setDuration(singleDuration);
            swing.setInterpolator(new AccelerateDecelerateInterpolator());
            animations.add(swing);
        }

        // Return to neutral rotation
        ObjectAnimator settle = ObjectAnimator.ofFloat(hintFinger, "rotation", 0f);
        settle.setDuration(150);
        animations.add(settle);

        swingSet.playSequentially(animations);
        swingSet.start();

        // Hide after animation completes
        swingSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {

//                    View parent = (View) hintFinger.getParent();
//                    parent.setVisibility(View.GONE);

                    // Show the "Got it" button
                    btnHintGotIt.setVisibility(View.VISIBLE);
                }, 600); // Pause before hiding
            }
        });
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Image Processing">
    //=================================================

    /**
     * Applies selfie correction by flipping and rotating the captured front-facing image.
     * Selfie previews are typically mirrored on-screen, but the saved file may not match this orientation.
     * This method ensures the captured image is corrected and saved permanently before passing it to uCrop.
     *
     * The corrected image is flipped (horizontally or vertically), rotated based on EXIF metadata,
     * and overwritten to the original file path so downstream consumers like uCrop can read the updated image.
     *
     * @param photoFile The saved image file to correct.
     * @param flipX Whether to mirror the image horizontally.
     * @param flipY Whether to mirror the image vertically.
     * @return The corrected Bitmap (also saved back to photoFile).
     */
    public Bitmap applySelfieCorrection(File photoFile, boolean flipX, boolean flipY)
    {
        try
        {
            Uri savedUri = Uri.fromFile(photoFile);
            capturedPhotoUri = savedUri;

            InputStream inputStream = getContentResolver().openInputStream(savedUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
                case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
                case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
            }

            matrix.postScale(flipX ? -1 : 1, flipY ? -1 : 1);

            Bitmap corrected = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            try (FileOutputStream out = new FileOutputStream(photoFile)) {
                corrected.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }

            return corrected;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Camera Handling">
    //=================================================
    private void setupCaptureButton()
    {
        ImageButton captureBtn = findViewById(R.id.capture_button);
        captureBtn.setOnClickListener(v -> {

            showOverlayOnCapture(true);

            File tempProfilePath = new File(getExternalFilesDir(null), "temp_profile");

            if (!tempProfilePath.exists())
                tempProfilePath.mkdirs();

            File photoFile = new File(tempProfilePath, "captured.jpg");
            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(outputOptions, mainExecutor, new ImageCapture.OnImageSavedCallback()
            {
                @Override
                public void onCaptureStarted()
                {
                    ImageCapture.OnImageSavedCallback.super.onCaptureStarted();

                    switch( shutterAudio.getRingerMode() )
                    {
                        case AudioManager.RINGER_MODE_NORMAL:
                            MediaActionSound sound = new MediaActionSound();
                            sound.play(MediaActionSound.SHUTTER_CLICK);
                            break;

                        case AudioManager.RINGER_MODE_SILENT:
                        case AudioManager.RINGER_MODE_VIBRATE:
                            // Don't play shutter sound
                            break;
                    }
                }

                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults)
                {
                    if (isFrontCamera)
                    {
                        Bitmap captured = applySelfieCorrection(photoFile, true, false);
                        capturePreviewer.setImageBitmap(captured);
                    }
                    else
                    {
                        capturedPhotoUri = Uri.fromFile(photoFile);
                        capturePreviewer.setImageURI(capturedPhotoUri);
                    }

                    previewOverlay.setVisibility(View.VISIBLE);
                    showOverlayOnCapture(false);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception)
                {
                    alert("Oops! Sorry, we couldn't take the photo due to a technical error.", "Take photo");
                    Log.e("CameraX", "Capture failed: " + exception.getMessage());
                }
            });
        });
    }

    private void switchCamera(boolean useFrontCamera)
    {
        if (cameraProvider == null) return;

        isFrontCamera = useFrontCamera;

        CameraSelector cameraSelector = useFrontCamera ?
                CameraSelector.DEFAULT_FRONT_CAMERA :
                CameraSelector.DEFAULT_BACK_CAMERA;

        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try
            {
                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                // Use the front camera automatically on first load
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                // Wait for the surface to show or render what's captured on the first frame.
                // We can use this to track if the camera is "ready" then we can begin showing
                // the hint overlay.

                final boolean[] hintShown = {false}; // flag to prevent double triggering

                cameraPreviewView.getPreviewStreamState().observe(this, state -> {
                    if (state == PreviewView.StreamState.STREAMING && !hintShown[0])
                    {
                        hintShown[0] = true;
                        //Log.d("MINE", "CAMERA IS RENDERING NOW");
                        showHint();
                    }
                });

                // Timeout fallback (e.g., 2.5 seconds)
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                    if (!hintShown[0]) {
//                        hintShown[0] = true;
//                        waitForHintLayoutThenAnimate();
//                    }
//                }, 2500);
            }
            catch (Exception e)
            {
                Log.d("MINE", e.getMessage());
                // e.printStackTrace();
            }
        }, mainExecutor);
    }
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Permission Handling">
    //=================================================
    private boolean hasAllPermissions()
    {
        for (String perm : requiredPerms)
        {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    private void requestPerms()
    {
        if (hasAllPermissions())
        {
            initSetup();
            return;
        }

        // Ask directly. Do not check rationale now.
        // ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);
        Log.d("MINE", String.valueOf(denialCount));

        if (denialCount == 2)
        {
            // Third+ denial: assume “Don’t ask again”
            showGoToSettingsDialog();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPerms[0])) {
            // Second denial: show rationale dialog
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Camera Access Required")
                    .setMessage("Dear user,\n\nTo capture photos, we need access to your camera.\n\nWe respect your privacy and assure you that the camera will be used solely to take your profile picture—nothing more.\n\nPlease grant camera access in your settings to continue using this feature.")
                    .setPositiveButton("I Understand", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
                    })
                    .setNegativeButton("No, Thanks", (dialog, which) -> onGoBack())
                    .show();
        }

        else
        {
            ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_CAMERA_PERMISSION || !permissions[0].equals(Manifest.permission.CAMERA))
            return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            initSetup();
        }
        else
        {
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);

            // Check only CAMERA permission
            denialCount++;
            prefs.edit().putInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, denialCount).commit();

            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Camera Permission Denied")
                    .setMessage("Dear User,\n\nYou have denied camera access, which is required for taking photos.\n\nWithout this permission, this feature cannot function and the app will now exit.")
                    .setPositiveButton("OK", (dialog, which) -> onGoBack())
                    .show();
        }
    }

    private void showGoToSettingsDialog()
    {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Camera Access Required")
                .setMessage("Dear user,\n\nYou've permanently denied access to the camera. This permission is essential for capturing photos, so you won't be able to take pictures without it.\n\nTo enable camera access, please visit your app settings and grant the permission manually.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> onGoBack())
                .show();
    }
    //=================================================
    // </editor-fold>
    //=================================================
}