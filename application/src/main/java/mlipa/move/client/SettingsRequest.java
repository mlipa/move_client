package mlipa.move.client;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SettingsRequest extends StringRequest {
    // TODO: CHANGE TO PRODUCTION SERVER
    private static final String SETTINGS_URL = "http://192.168.1.5:5000/m_settings"; // "http://move-d.herokuapp.com/m_settings";
    private Map<String, String> params;

    public SettingsRequest(int method, String classifierId, Response.Listener<String> listener) {
        super(method, SETTINGS_URL, listener, null);

        if (method == Method.POST) {
            params = new HashMap<>();
            params.put("classifierId", classifierId);
        }
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
