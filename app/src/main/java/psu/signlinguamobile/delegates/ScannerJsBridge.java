package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class ScannerJsBridge
{
    public interface ScannerJsBridgeListener
    {
        void onReadLetter(String letter);
        void onStopRead();
        void onGoBack();
    }

    private final ScannerJsBridge.ScannerJsBridgeListener listener;

    public ScannerJsBridge(ScannerJsBridge.ScannerJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onReadLetter(String letter)
    {
        if (listener != null) {
            listener.onReadLetter(letter);
        }
    }

    @JavascriptInterface
    public void onStopRead()
    {
        if (listener != null)
            listener.onStopRead();
    }

    @JavascriptInterface
    public void onGoBack()
    {
        if (listener != null)
            listener.onGoBack();
    }
}
