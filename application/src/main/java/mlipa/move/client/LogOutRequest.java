package mlipa.move.client;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LogOutRequest extends StringRequest {
    // TODO: CHANGE TO PRODUCTION SERVER
    private static final String LOG_OUT_URL = "http://192.168.1.5:5000/m_log_out"; // "http://move-d.herokuapp.com/m_log_out";

    public LogOutRequest(Response.Listener<String> listener) {
        super(Method.GET, LOG_OUT_URL, listener, null);
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
