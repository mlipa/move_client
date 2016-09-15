package mlipa.move.client;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class Cookie {
    public static void checkCookieSession(Context context, Map<String, String> headers) {
        if (headers.containsKey(context.getString(R.string.shared_preferences_cookie_set_cookie)) && headers.get(context.getString(R.string.shared_preferences_cookie_set_cookie)).startsWith(context.getString(R.string.shared_preferences_cookie_session))) {
            String cookie = headers.get(context.getString(R.string.shared_preferences_cookie_set_cookie));

            if (cookie.trim().length() > 0) {
                SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_cookie), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(context.getString(R.string.shared_preferences_cookie_session), cookie.split(";")[0]);
                editor.apply();
            }
        }
    }

    public static void addCookieSession(Context context, Map<String, String> headers) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_cookie), Context.MODE_PRIVATE);
        String session = preferences.getString(context.getString(R.string.shared_preferences_cookie_session), "");

        if (session.trim().length() > 0) {
            StringBuilder builder = new StringBuilder();

            builder.append(session);

            if (headers.containsKey(context.getString(R.string.shared_preferences_cookie_cookie))) {
                builder.append("; ");
                builder.append(headers.get(context.getString(R.string.shared_preferences_cookie_cookie)));
            }

            headers.put(context.getString(R.string.shared_preferences_cookie_cookie), builder.toString());
        }
    }
}
