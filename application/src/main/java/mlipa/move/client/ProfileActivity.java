package mlipa.move.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
    private RequestQueue queue;
    private ProfileRequest profileRequest;
    private AvatarRequest avatarRequest;
    private LogOutRequest logOutRequest;

    private CircularImageView civAvatar;
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

        civAvatar = (CircularImageView) findViewById(R.id.civ_avatar);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvUsername = (TextView) findViewById(R.id.tv_username);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        bLogOut = (Button) findViewById(R.id.b_log_out);

        Response.Listener<String> profileListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    boolean avatar = jsonResponse.getBoolean("avatar");

                    if (success) {
                        tvName.setText(jsonResponse.getString("name"));
                        tvUsername.setText(jsonResponse.getString("username"));
                        tvEmail.setText(jsonResponse.getString("email"));

                        if (avatar) {
                            Response.Listener<Bitmap> avatarListener = new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    civAvatar.setImageBitmap(response);
                                }
                            };

                            avatarRequest = new AvatarRequest(jsonResponse.getString("filename"), avatarListener);

                            queue.add(avatarRequest);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        profileRequest = new ProfileRequest(profileListener);

        queue.add(profileRequest);

        bLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Response.Listener<String> logOutListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            Toast toast = Toast.makeText(context, jsonResponse.getString("message"), Toast.LENGTH_LONG);

                            if (success) {
                                Intent intent = new Intent(ProfileActivity.this, LogInActivity.class);

                                startActivity(intent);
                                finish();
                            }

                            toast.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                logOutRequest = new LogOutRequest(logOutListener);

                queue.add(logOutRequest);
            }
        });
    }
}
