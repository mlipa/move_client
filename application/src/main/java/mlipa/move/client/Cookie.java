package mlipa.move.client;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class Cookie {
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_KEY = "session";

    public static void checkSessionCookie(Context context, Map<String, String> headers) {
        if (headers.containsKey(SET_COOKIE_KEY) && headers.get(SET_COOKIE_KEY).startsWith(SESSION_KEY)) {
            String cookie = headers.get(SET_COOKIE_KEY);

            if (cookie.trim().length() > 0) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.cookie_shared_preferences_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(SESSION_KEY, cookie.split(";")[0]);
                editor.apply();
            }
        }
    }

    public static void addSessionCookie(Context context, Map<String, String> headers) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.cookie_shared_preferences_key), Context.MODE_PRIVATE);
        String session = sharedPreferences.getString(SESSION_KEY, "");

        if (session.trim().length() > 0) {
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
