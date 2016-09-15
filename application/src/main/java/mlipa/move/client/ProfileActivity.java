package mlipa.move.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private final String TAG = ProfileActivity.class.toString();

    private Context context;
    private RequestQueue queue;
    private SharedPreferences preferences;

    private Intent logInIntent;

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
        queue = Volley.newRequestQueue(context);
        preferences = getSharedPreferences(getString(R.string.shared_preferences_profile), Context.MODE_PRIVATE);

        logInIntent = new Intent(context, LogInActivity.class);

        civAvatar = (CircularImageView) findViewById(R.id.civ_avatar);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        bLogOut = (Button) findViewById(R.id.b_log_out);

        tvName.setText(preferences.getString(getString(R.string.shared_preferences_profile_name), getString(R.string.nothing)));
        tvUsername.setText(preferences.getString(getString(R.string.shared_preferences_profile_username), getString(R.string.nothing)));
        tvEmail.setText(preferences.getString(getString(R.string.shared_preferences_profile_email), getString(R.string.nothing)));

        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

                builder.setTitle(R.string.title_log_out);
                builder.setMessage(R.string.message_log_out);
                builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Response.Listener<String> logOutListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonResponse = new JSONObject(response);

                                            if (jsonResponse.getBoolean(getString(R.string.server_success))) {
                                                String message = jsonResponse.getString(getString(R.string.server_message));

                                                Log.v(TAG, "[bLogOut.onResponse()] " + getString(R.string.server_message) + " = " + message);

                                                logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(logInIntent);

                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                queue.add(new LogOutRequest(context, logOutListener));
                            }
                        }).start();
                    }
                });
                builder.setNegativeButton(R.string.button_no, null);
                builder.create().show();
            }
        });
    }
}
