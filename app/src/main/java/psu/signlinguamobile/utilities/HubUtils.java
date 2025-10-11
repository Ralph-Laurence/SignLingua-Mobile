package psu.signlinguamobile.utilities;

public class HubUtils
{
    private static final boolean isDevelopment = true;
    private static final String CHATHUB_URL_DEPLOY = "https://playground-test-chatserver.runasp.net/chathub";
    private static final String CHATHUB_URL_LOCAL = "http://192.168.1.150:5041/chathub";

    private static final String VIDEOCALLHUB_URL_DEPLOY = "https://playground-test-chatserver.runasp.net/videocallhub";
    private static final String VIDEOCALLHUB_URL_LOCAL = "http://192.168.1.150:5041/videocallhub";

    private static final String NOTIFHUB_URL_DEPLOY = "https://playground-test-chatserver.runasp.net/notificationhub";
    private static final String NOTIFHUB_URL_LOCAL = "http://192.168.1.150:5041/notificationhub";

    public static String getChatHubUrl()
    {
        if (isDevelopment)
            return CHATHUB_URL_LOCAL;

        return CHATHUB_URL_DEPLOY;
    }

    public static String getVideoCallHubUrl()
    {
        if (isDevelopment)
            return VIDEOCALLHUB_URL_LOCAL;

        return VIDEOCALLHUB_URL_DEPLOY;
    }

    public static String getNotifHubUrl()
    {
        if (isDevelopment)
            return NOTIFHUB_URL_LOCAL;

        return NOTIFHUB_URL_DEPLOY;
    }
}
