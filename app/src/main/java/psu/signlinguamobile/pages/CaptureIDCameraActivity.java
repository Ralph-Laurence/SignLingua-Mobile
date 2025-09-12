package psu.signlinguamobile.pages;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;

import psu.signlinguamobile.R;
import psu.signlinguamobile.utilities.PhotoCaptureManager;

/**
 * This is the main activity that handles the capturing of the ID
 */
public class CaptureIDCameraActivity extends AppCompatActivity
{
    private LinearLayout hintLayout;
    private Button btnHintGotIt;

    private PhotoCaptureManager photoCaptureManager;
    private PreviewView previewView;
    private ImageButton shutterButton;
    private ImageView idHintPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_capture_idactivity);
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

        idHintPreview   = findViewById(R.id.previewer_hint_id);
        hintLayout      = findViewById(R.id.hint_layout);
        btnHintGotIt    = findViewById(R.id.btn_hint_got_it);

        btnHintGotIt.setOnClickListener(v -> {
            Log.d("MINE", "OK GOT IT!");
            hintLayout.setVisibility(View.INVISIBLE);
        });

        previewView = findViewById(R.id.previewView);
        photoCaptureManager = new PhotoCaptureManager(this, this);
        photoCaptureManager.setOnGoBack(this::onGoBack);

        photoCaptureManager.initialize(previewView);

        // We only enable the "OK, GOT IT" button once the
        // camera is ready rendering.
        photoCaptureManager.onCameraReady = () -> btnHintGotIt.setEnabled(true);

        photoCaptureManager.onCaptureSucceed = (uri) -> {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("result", uri.toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        };

        photoCaptureManager.onCaptureFailed = (err) -> alert(err, "Capture Failed");

        String captureFilename = getIntent().getStringExtra("captureFilename");

        if (captureFilename != null)
        {
            Drawable hintImage = captureFilename.contains("front")
                    ? ContextCompat.getDrawable(this, R.drawable.specimen_id)
                    : ContextCompat.getDrawable(this, R.drawable.specimen_id_back);

            idHintPreview.setImageDrawable(hintImage);
        }

        Log.d("MINE", "capture file name -> " + captureFilename);
        shutterButton = findViewById(R.id.capture_button);
        shutterButton.setOnClickListener(v -> {
            photoCaptureManager.setOutputFilename(captureFilename);
            photoCaptureManager.captureImage();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        photoCaptureManager.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onGoBack()
    {
        Intent intent = new Intent(CaptureIDCameraActivity.this, CaptureIDMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        setResult(RESULT_CANCELED);
        startActivity(intent);
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
}