package mlipa.move.client;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {
    private Context context;
    private RequestQueue queue;
    private ProfileRequest request;

    private TextView tvName;
    private TextView tvUsername;
    private TextView tvEmail;
    private Button bLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        context = getApplicationContext();
        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
        queue = Volley.newRequestQueue(ProfileActivity.this);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        bLogOut = (Button) findViewById(R.id.b_log_out);

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success) {
                        tvName.setText(jsonResponse.getString("name"));
                        tvUsername.setText(jsonResponse.getString("username"));
                        tvEmail.setText(jsonResponse.getString("email"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        request = new ProfileRequest(listener);

        queue.add(request);
    }
}
