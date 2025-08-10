package psu.signlinguamobile.delegates;

import java.io.File;

public interface UCropCallback {
    void onCropSuccess(File croppedFile);
    void onCropFailure(String errorMessage);
}
