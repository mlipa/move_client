package mlipa.move.client;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProfileRequest extends StringRequest {
    private static final String PROFILE_URL = "http://move-p.herokuapp.com/m_profile";

    private Context context;

    public ProfileRequest(Context context, Response.Listener<String> listener) {
        super(Method.GET, PROFILE_URL, listener, null);

        this.context = context;
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
