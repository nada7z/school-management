package schoolmanagement.smproject.common;

import schoolmanagement.smproject.auth.entity.User;

/**
 * Holds the currently logged-in user for the entire desktop session.
 * JavaFX runs on a single UI thread, so a static variable is safe here.
 */
public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}