package psu.signlinguamobile.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.yalantis.ucrop.UCrop;

import java.io.File;

import psu.signlinguamobile.delegates.UCropCallback;

public class UCropActivityResultHandler
{
    private static UCropCallback callback;

    public static void setCallback(UCropCallback cb) {
        callback = cb;
    }

    public static void handleResult(int requestCode, int resultCode, @Nullable Intent data, Context context)
    {
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK && data != null)
        {
            Log.d("MINE", "UCropActivityResultHandler CALLED");
            Uri resultUri = UCrop.getOutput(data);

            Log.d("MINE", "UCropActivityResultHandler URI: " + resultUri);
            if (resultUri == null || resultUri.getPath() == null)
            {
                if (callback != null) {
                    callback.onCropFailure("Image file is unreadable.");
                    Log.d("MINE", "UCropActivityResultHandler: Image file is unreadable");
                }
                return;
            }

            File file = new File(resultUri.getPath());

            if (file.exists() && file.length() > 0)
            {
                Log.d("MINE", "File exists");
                if (callback != null) {
                    callback.onCropSuccess(file);
                    Log.d("MINE", "UCropActivityResultHandler SUCCEEDs");
                }
                else
                {
                    Log.d("MINE", "But callback is null");
                }
            }
            else
            {
                if (callback != null) {
                    callback.onCropFailure("Cropped image file is missing or empty.");
                    Log.d("MINE", "UCropActivityResultHandler: Cropped image file is missing or empty");
                }
            }

        }
        else if (resultCode == UCrop.RESULT_ERROR && data != null)
        {
            Throwable cropError = UCrop.getError(data);
            if (callback != null) callback.onCropFailure("Crop error: " + cropError.getMessage());
        }

        // Clean up to avoid memory leaks
        callback = null;
    }
}
