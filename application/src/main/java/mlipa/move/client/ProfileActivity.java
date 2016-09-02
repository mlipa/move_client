package mlipa.move.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private static final String TAG = ProfileActivity.class.toString();

    private static final String SERVER_SUCCESS_KEY = "success";
    private static final String SERVER_MESSAGE_KEY = "message";
    private static final String CLIENT_NAME_KEY = "name";
    private static final String CLIENT_USERNAME_KEY = "username";
    private static final String CLIENT_EMAIL_KEY = "email";

    private Context context;
    private Intent intent;
    private RequestQueue queue;

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
        intent = getIntent();
        queue = Volley.newRequestQueue(context);

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        logInIntent = new Intent(context, LogInActivity.class);

        civAvatar = (CircularImageView) findViewById(R.id.civ_avatar);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        bLogOut = (Button) findViewById(R.id.b_log_out);

        tvName.setText(intent.getStringExtra(CLIENT_NAME_KEY));
        tvUsername.setText(intent.getStringExtra(CLIENT_USERNAME_KEY));
        tvEmail.setText(intent.getStringExtra(CLIENT_EMAIL_KEY));

        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);

                builder.setTitle(R.string.log_out);
                builder.setMessage(R.string.log_out_message);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Response.Listener<String> logOutListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);

                                    if (jsonResponse.getBoolean(SERVER_SUCCESS_KEY)) {
                                        String message = jsonResponse.getString(SERVER_MESSAGE_KEY);

                                        Log.v(TAG, SERVER_MESSAGE_KEY + " = " + message);

                                        logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(logInIntent);

                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        queue.add(new LogOutRequest(logOutListener));
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });
    }
}
