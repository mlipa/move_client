package mlipa.move.client;

import android.content.SharedPreferences;

import java.util.Map;

public class Cookie {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_KEY = "session";

    public static SharedPreferences preferences;

    public static void checkSessionCookie(Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY) && headers.get(SET_COOKIE_KEY).startsWith(SESSION_KEY)) {
            String cookie = headers.get(SET_COOKIE_KEY);

            if (cookie.length() > 0) {
                SharedPreferences.Editor editor = preferences.edit();

                cookie = cookie.split(";")[0];
                editor.putString(SESSION_KEY, cookie);
                editor.commit();
            }
        }
    }

    public static void addSessionCookie(Map<String, String> headers) {
        String session = preferences.getString(SESSION_KEY, "");

        if (session.length() > 0) {
            StringBuilder builder = new StringBuilder();

            builder.append(session);

            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }

            headers.put(COOKIE_KEY, builder.toString());
        }
    }
}
