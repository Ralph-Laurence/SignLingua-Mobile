package psu.signlinguamobile.delegates;

import org.mozilla.geckoview.GeckoSession;

import java.util.List;

public class GeckoCustomNavigationDelegate implements GeckoSession.NavigationDelegate {
    @Override
    public void onCanGoBack(GeckoSession session, boolean canGoBack) {
        // Do nothing — we're ignoring this
    }

    @Override
    public void onLocationChange(GeckoSession session, String url, List<GeckoSession.PermissionDelegate.ContentPermission> perms, Boolean hasUserGesture) {
        // Optional: handle location changes
    }
}