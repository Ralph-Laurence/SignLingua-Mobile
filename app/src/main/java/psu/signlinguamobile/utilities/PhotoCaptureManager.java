package psu.signlinguamobile.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import psu.signlinguamobile.data.Constants;

public class PhotoCaptureManager
{
    private AudioManager shutterAudio;
    private Executor mainExecutor;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageCapture imageCapture;
    private PreviewView cameraPreviewView;

    private final WeakReference<Activity> activityRef;
    private final WeakReference<LifecycleOwner> m_lifecycleOwner;

    private final Context m_context;

    private Runnable ev_onGoBack;
    public Runnable onCameraReady;
    public Runnable onBeginCapture;
    public Runnable onEndCapture;
    public Consumer<Uri> onCaptureSucceed;
    public Consumer<String> onCaptureFailed;

    private String outputFilename;

    private static final int REQUEST_CODE_CAMERA_PERMISSION = 211;
    private final String[] requiredPerms = new String[] {
        Manifest.permission.CAMERA,
    };

    public PhotoCaptureManager(Activity activity, LifecycleOwner lifecycleOwner)
    {
        activityRef      = new WeakReference<>(activity);
        m_context        = activityRef.get().getApplicationContext();
        m_lifecycleOwner = new WeakReference<>(lifecycleOwner);
    }

    public void initialize(PreviewView cameraPreviewView)
    {
        shutterAudio = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
        mainExecutor = ContextCompat.getMainExecutor(m_context);
        this.cameraPreviewView = cameraPreviewView;

        requestPerms();
    }

    public void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this.m_context);

        cameraProviderFuture.addListener(() -> {
            try
            {
                LifecycleOwner lifecycleOwner = m_lifecycleOwner.get();

                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                // Use the front camera automatically on first load
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture);

                // Wait for the surface to show or render what's captured on the first frame.
                // We can use this to track if the camera is "ready".

                final boolean[] cameraReady = {false};

                cameraPreviewView.getPreviewStreamState().observe(lifecycleOwner, state -> {
                    if (state == PreviewView.StreamState.STREAMING && !cameraReady[0])
                    {
                        cameraReady[0] = true;
                        Log.d("MINE", "CAMERA IS RENDERING NOW");
                        cameraReady[0] = true;
                        notifyOnCameraReady();
                    }
                });

                // Timeout fallback (e.g., 2.5 seconds)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!cameraReady[0]) {
                        cameraReady[0] = true;
                        notifyOnCameraReady();
                    }
                }, 2500);
            }
            catch (Exception e)
            {
                Log.d("MINE", e.getMessage());
                // e.printStackTrace();
            }
        }, mainExecutor);
    }

    private void notifyOnCameraReady()
    {
        if (onCameraReady != null)
            onCameraReady.run();
    }

    private void initSetup()
    {
        startCamera();
//        btnRetryCapture     = findViewById(R.id.btn_previewer_retry);
//        btnProceedPicture   = findViewById(R.id.btn_previewer_proceed);
//        btnHintGotIt        = findViewById(R.id.btn_hint_got_it);
//        cameraPreviewView   = findViewById(R.id.previewView);
//        previewOverlay      = findViewById(R.id.capture_previewer_overlay);
//        capturePreviewer    = findViewById(R.id.capture_previewer);
//        overlayOnCapture    = findViewById(R.id.overlay_on_process);

        //setupCaptureButton(); // Add capture logic

//        btnHintGotIt.setOnClickListener(click -> hintLayout.setVisibility(View.GONE));
//        btnRetryCapture.setOnClickListener(click -> {
//            previewOverlay.setVisibility(View.INVISIBLE);
//            showOverlayOnCapture(false);
//        });
//        btnProceedPicture.setOnClickListener(click -> {
//            if (capturedPhotoUri == null)
//            {
//                alert("Oops! Something went wrong while processing the photo. Please try again.", "Change photo");
//                return;
//            }
//
//        });
    }

    public void setOnGoBack(Runnable onGoBack)
    {
        ev_onGoBack = onGoBack;
    }

    private void onGoBack()
    {
        if (ev_onGoBack != null)
            ev_onGoBack.run();
    }

    //=================================================
    // <editor-fold desc="Permission Handling">
    //=================================================

    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        Activity activity = activityRef.get();
        
        if (requestCode != REQUEST_CODE_CAMERA_PERMISSION || !permissions[0].equals(Manifest.permission.CAMERA))
            return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            initSetup();
        }
        else
        {
            SharedPreferences prefs = m_context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);

            // Check only CAMERA permission
            denialCount++;
            prefs.edit().putInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, denialCount).commit();

            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setTitle("Camera Permission Denied")
                    .setMessage("Dear User,\n\nYou have denied camera access, which is required for taking photos.\n\nWithout this permission, this feature cannot function and the app will now exit.")
                    .setPositiveButton("OK", (dialog, which) -> onGoBack())
                    .show();
        }
    }

    private void showGoToSettingsDialog()
    {
        Activity activity = activityRef.get();

        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle("Camera Access Required")
                .setMessage("Dear user,\n\nYou've permanently denied access to the camera. This permission is essential for capturing photos, so you won't be able to take pictures without it.\n\nTo enable camera access, please visit your app settings and grant the permission manually.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", m_context.getPackageName(), null));
                    activity.startActivity(intent);
                    activity.finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> onGoBack())
                .show();
    }

    private boolean hasAllPermissions()
    {
        for (String perm : requiredPerms)
        {
            if (ContextCompat.checkSelfPermission(m_context, perm) != PackageManager.PERMISSION_GRANTED)
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
            Log.d("MINE", "HAS ALL PERMS, STARTING NOW...");
            initSetup();
            return;
        }

        // Ask directly. Do not check rationale now.
        // ActivityCompat.requestPermissions(this, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        SharedPreferences prefs = m_context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        int denialCount = prefs.getInt(Constants.SharedPrefKeys.CAMERA_PERMISSION_DENIALS, 0);
        Log.d("MINE", String.valueOf(denialCount));

        if (denialCount == 2)
        {
            // Third+ denial: assume “Don’t ask again”
            showGoToSettingsDialog();
            return;
        }

        Activity activity = activityRef.get();

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requiredPerms[0]))
        {
            // Second denial: show rationale dialog
            new AlertDialog.Builder(m_context)
                    .setCancelable(false)
                    .setTitle("Camera Access Required")
                    .setMessage("Dear user,\n\nTo capture photos, we need access to your camera.\n\nWe respect your privacy and assure you that the camera will be used solely to take your ID picture—nothing more.\n\nPlease grant camera access in your settings to continue using this feature.")
                    .setPositiveButton("I Understand", (dialog, which) -> {
                        ActivityCompat.requestPermissions(activity, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
                    })
                    .setNegativeButton("No, Thanks", (dialog, which) -> onGoBack())
                    .show();
        }

        else
        {
            ActivityCompat.requestPermissions(activity, requiredPerms, REQUEST_CODE_CAMERA_PERMISSION);
        }
    }
    //=================================================
    // </editor-fold>
    //=================================================

    //=================================================
    // <editor-fold desc="Camera Capture">
    //=================================================

    public void setOutputFilename(String outputFilename)
    {
        this.outputFilename = outputFilename;
    }

    public void captureImage()
    {
        if (outputFilename.isEmpty())
            outputFilename = "captured.jpg";

        // showOverlayOnCapture(true);

        if (onBeginCapture != null)
            onBeginCapture.run();

        File tempProfilePath = new File(m_context.getExternalFilesDir(null), "temp_verification");

        if (!tempProfilePath.exists())
            tempProfilePath.mkdirs();

        File photoFile = new File(tempProfilePath, outputFilename);
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, mainExecutor, new ImageCapture.OnImageSavedCallback()
        {
            @Override
            public void onCaptureStarted()
            {
                ImageCapture.OnImageSavedCallback.super.onCaptureStarted();

                playShutterSound();
            }

            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults)
            {
                // capturePreviewer.setImageURI(capturedPhotoUri);

                if (onEndCapture != null)
                    onEndCapture.run();

                if (onCaptureSucceed != null)
                    onCaptureSucceed.accept(Uri.fromFile(photoFile));

//                previewOverlay.setVisibility(View.VISIBLE);
//                showOverlayOnCapture(false);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception)
            {
                if (onEndCapture != null)
                    onEndCapture.run();

                if (onCaptureFailed != null)
                    onCaptureFailed.accept("Oops! Sorry, we couldn't take the photo due to a technical error.");

                Log.e("MINE", "Capture failed: " + exception.getMessage());
            }
        });
    }

    private void playShutterSound()
    {
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
    //=================================================
    // </editor-fold>
    //=================================================
}
