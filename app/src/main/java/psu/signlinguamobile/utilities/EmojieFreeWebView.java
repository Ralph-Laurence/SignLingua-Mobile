package psu.signlinguamobile.utilities;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

/*
============================================================
THIS SPECIAL WEB VIEW HIDES THE EMOJI TRIGGER FROM SOFT KEY
............................................................
SPECIAL THANKS to "Rasul":
https://stackoverflow.com/a/55515203

answered Apr 4, 2019 at 11:45
............................................................

Original Answer:
You should create your custom WebView and override the onCreateInputConnection like this:
@Override
public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        outAttrs.inputType =InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        return connection;
}
............................................................
============================================================
*/

public class EmojieFreeWebView extends WebView {
    public EmojieFreeWebView(Context context) {
        super(context);
    }

    public EmojieFreeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmojieFreeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        return connection;
    }
}

