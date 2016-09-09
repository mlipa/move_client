package mlipa.move.client;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LogInRequest extends StringRequest {
    private static final String LOG_IN_URL = "http://move-p.herokuapp.com/m_log_in";

    private Context context;
    private Map<String, String> params;

    public LogInRequest(Context context, String username, String password, Response.Listener<String> listener) {
        super(Method.POST, LOG_IN_URL, listener, null);

        this.context = context;

        params = new HashMap<>();
        params.put(context.getString(R.string.client_username_key), username);
        params.put(context.getString(R.string.client_password_key), password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Cookie.checkSessionCookie(context, response.headers);

        return super.parseNetworkResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.<String, String>emptyMap())) {
            headers = new HashMap<>();
        }

        Cookie.addSessionCookie(context, headers);

        return headers;
    }
}
