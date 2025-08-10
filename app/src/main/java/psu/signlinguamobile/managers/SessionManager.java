package psu.signlinguamobile.managers;

import psu.signlinguamobile.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String token;

    // Private constructor to enforce singleton pattern.
    private SessionManager() {}

    // Get the one and only instance of SessionManager.
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Save the current user session in memory.
     *
     * @param user  The logged in User object.
     * @param token The authentication token.
     */
    public void saveSession(User user, String token) {

        if (user != null)
            this.currentUser = user;

        if (token != null)
            this.token = token;
    }

    /**
     * Retrieve the current user object.
     *
     * @return The current user, or null if no session is active.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Retrieve the authentication token.
     *
     * @return The token string, or null if not set.
     */
    public String getToken() {
        return token;
    }

    /**
     * Check if a session is active.
     *
     * @return true if both user and token are available; false otherwise.
     */
    public boolean isLoggedIn() {
        return token != null && currentUser != null;
    }

    /**
     * Clear the session in memory.
     */
    public void clearSession() {
        currentUser = null;
        token = null;
    }
}
