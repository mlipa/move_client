package mlipa.move.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {
    private Context context;
    private Intent intent;
    private Intent logInIntent;
    private RequestQueue queue;

    public static CircularImageView civAvatar;
    private TextView tvName;
    private TextView tvUsername;
    private TextView tvEmail;
    private Button bLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        context = getApplicationContext();
        intent = getIntent();
        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
        queue = Volley.newRequestQueue(ProfileActivity.this);

        civAvatar = (CircularImageView) findViewById(R.id.civ_avatar);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        bLogOut = (Button) findViewById(R.id.b_log_out);

        tvName.setText(intent.getStringExtra("name"));
        tvUsername.setText(intent.getStringExtra("username"));
        tvEmail.setText(intent.getStringExtra("email"));

        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Response.Listener<String> logOutListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean("success")) {
                                logInIntent = new Intent(ProfileActivity.this, LogInActivity.class);
                                Toast toast = Toast.makeText(context, jsonResponse.getString("message"), Toast.LENGTH_LONG);

                                logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(logInIntent);

                                toast.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                queue.add(new LogOutRequest(logOutListener));
            }
        });
    }
}
