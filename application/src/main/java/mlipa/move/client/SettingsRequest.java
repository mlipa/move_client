package mlipa.move.client;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SettingsRequest extends StringRequest {
    private static final String SETTINGS_URL = "http://move-p.herokuapp.com/m_settings";

    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";

    private Map<String, String> params;

    public SettingsRequest(int method, String classifierId, Response.Listener<String> listener) {
        super(method, SETTINGS_URL, listener, null);

        if (method == Method.POST) {
            params = new HashMap<>();
            params.put(CLIENT_CLASSIFIER_ID_KEY, classifierId);
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
