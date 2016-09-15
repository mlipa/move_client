package mlipa.move.client;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PredictionRequest extends StringRequest {
    private Context context;
    private Map<String, String> params;

    public PredictionRequest(Context context, String timestamp, String activityId, String classifierId, String userId, Response.Listener<String> listener) {
        super(Method.POST, context.getString(R.string.url_prediction), listener, null);

        this.context = context;
        params = new HashMap<>();

        params.put(context.getString(R.string.client_timestamp), timestamp);
        params.put(context.getString(R.string.client_activity_id), activityId);
        params.put(context.getString(R.string.client_classifier_id), classifierId);
        params.put(context.getString(R.string.client_user_id), userId);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
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
