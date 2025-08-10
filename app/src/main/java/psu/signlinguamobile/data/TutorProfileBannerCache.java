package psu.signlinguamobile.data;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class TutorProfileBannerCache
{
    private static String username = null;
    private static String photoUrl = null;

    public static void store(String tempUsername, String tempPhotoUrl)
    {
        if (tempUsername != null)
            username = tempUsername;

        if (tempPhotoUrl != null)
            photoUrl = tempPhotoUrl;
    }

    /**
     * Retrieve the cached username and photo in JSON form.
     * This will be useful when sending via js bridge.
     * @return JSON
     */
    public static String get()
    {
        Map<String, String> cache = new HashMap<>();

        cache.put("username", getUsername());
        cache.put("photo", getPhoto());

        Gson gson = new Gson();

        return gson.toJson(cache);
    }

    public static String getUsername() {
        return username;
    }

    public static String getPhoto() {
        return photoUrl;
    }
}
