package psu.signlinguamobile.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BmpUtils
{
    public Bitmap rotateToLandscapeLeft(File photoFile)
    {
        // Step 1: Decode the image file into a Bitmap
        Bitmap originalBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        if (originalBitmap == null) {
            // Handle the error if the file could not be decoded
            return null;
        }

        // Step 2: Read the Exif data for orientation
        int orientation = getOrientation(photoFile);

        // Step 3: Rotate the image to make it landscape (if needed)
        Bitmap rotatedBitmap;

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            // The image is in portrait, but already rotated (landscape-left), no need to rotate
            rotatedBitmap = originalBitmap;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            // Rotate the image 180 degrees
            rotatedBitmap = rotateBitmap(originalBitmap, 180);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            // Rotate the image 270 degrees (this means landscape-right)
            rotatedBitmap = rotateBitmap(originalBitmap, 270);
        } else {
            // Rotate the image 90 degrees (portrait to landscape-left)
            rotatedBitmap = rotateBitmap(originalBitmap, 90);
        }

        // Step 4: Save the rotated Bitmap back to the disk (overwrite the original file)
        try (FileOutputStream out = new FileOutputStream(photoFile)) {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);  // Save as JPEG, adjust quality as needed
        } catch (IOException e) {
            e.printStackTrace();
            return null;  // Handle exception if saving fails
        }

        // Recycle bitmaps after usage
        //originalBitmap.recycle();
       // rotatedBitmap.recycle();

        // Return the rotated Bitmap (optional)
        return rotatedBitmap;
    }

    // Helper method to get the image's orientation
    private int getOrientation(File photoFile) {
        try {
            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    // Helper method to rotate a Bitmap
    private Bitmap rotateBitmap(Bitmap originalBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
    }
}
