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
    private Context context;

    public ProfileRequest(Context context, Response.Listener<String> listener) {
        super(Method.GET, context.getString(R.string.url_profile), listener, null);

        this.context = context;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Cookie.checkCookieSession(context, response.headers);

        return super.parseNetworkResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.<String, String>emptyMap())) {
            headers = new HashMap<>();
        }

        Cookie.addCookieSession(context, headers);

        return headers;
    }
}
