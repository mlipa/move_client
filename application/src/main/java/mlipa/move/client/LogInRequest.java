package mlipa.move.client;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;

public class LogInRequest extends StringRequest {
    private static final String LOG_IN_URL = "http://move-d.herokuapp.com/m_log_in";
    private HashMap<String, String> params;

    public LogInRequest(String username, String password, Response.Listener<String> listener) {
        super(Method.POST, LOG_IN_URL, listener, null);

        params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
    }

    @Override
    public HashMap<String, String> getParams() {
        return params;
    }
}
