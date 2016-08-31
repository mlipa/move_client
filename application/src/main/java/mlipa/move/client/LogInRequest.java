package mlipa.move.client;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LogInRequest extends StringRequest {
    // TODO: CHANGE TO PRODUCTION SERVER
    private static final String LOG_IN_URL = "http://192.168.1.5:5000/m_log_in"; // "http://move-d.herokuapp.com/m_log_in";

    private static final String CLIENT_USERNAME_KEY = "username";
    private static final String CLIENT_PASSWORD_KEY = "password";

    private Map<String, String> params;

    public LogInRequest(String username, String password, Response.Listener<String> listener) {
        super(Method.POST, LOG_IN_URL, listener, null);

        params = new HashMap<>();
        params.put(CLIENT_USERNAME_KEY, username);
        params.put(CLIENT_PASSWORD_KEY, password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Cookie.checkSessionCookie(response.headers);

        return super.parseNetworkResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.<String, String>emptyMap())) {
            headers = new HashMap<>();
        }

        Cookie.addSessionCookie(headers);

        return headers;
    }
}
