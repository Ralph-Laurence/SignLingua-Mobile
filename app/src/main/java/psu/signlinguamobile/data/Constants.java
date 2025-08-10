package psu.signlinguamobile.data;

import psu.signlinguamobile.models.User;

public class Constants
{
    public static class SharedPrefKeys
    {
        public static final String AUTH = "auth";
        public static final String TOKEN = "token";
        public static final String USER_ID = "user_id";
        public static final String USER_DETAILS = "user_details";
        public static final String CAMERA_PERMISSION_DENIALS = "camera_permission_denials";
    }

    public static class ApiRoutes
    {
        public static final String BASE_LOCAL_HOST = "http://192.168.1.150:8000/api/";
        public static final String BASE_PRODUCTION = "https://remarkably-patient-wildcat.ngrok-free.app/api/";
        public static final String BASE = BASE_LOCAL_HOST;
    }
}
