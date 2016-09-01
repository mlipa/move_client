package mlipa.move.client;

import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AvatarRequest extends ImageRequest {
    private static final String AVATAR_URL = "http://move-p.herokuapp.com/m_avatar/";

    public AvatarRequest(String filename, Response.Listener<Bitmap> listener) {
        super(AVATAR_URL + filename, listener, 0, 0, null, null);
    }

    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
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
