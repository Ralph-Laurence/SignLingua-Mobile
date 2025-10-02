package psu.signlinguamobile.delegates;

import android.webkit.JavascriptInterface;

public class ContactsJsBridge
{
    public interface ContactsJsBridgeListener
    {
        String JS_BRIDGE_NAME = "ContactsJsBridge";
        void onProfileLink();
        void onContactItemSelected(String contactId);
    }

    private final ContactsJsBridge.ContactsJsBridgeListener listener;

    public ContactsJsBridge(ContactsJsBridge.ContactsJsBridgeListener listener)
    {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onProfileLink()
    {
        if (listener != null) {
            listener.onProfileLink();
        }
    }

    @JavascriptInterface
    public void onContactItemSelected(String contactId)
    {
        if (listener != null) {
            listener.onContactItemSelected(contactId);
        }
    }
}
