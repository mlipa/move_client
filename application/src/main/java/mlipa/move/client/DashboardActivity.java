package mlipa.move.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity {
    private Context context;
    private Intent settingsIntent;
    private Intent profileIntent;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        context = getApplicationContext();
        queue = Volley.newRequestQueue(DashboardActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_dashboard, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);

                startActivity(settingsIntent);

                return true;
            case R.id.profile:
                profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);

                final Response.Listener<String> profileListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean("success")) {
                                profileIntent.putExtra("name", jsonResponse.getString("name"));
                                profileIntent.putExtra("username", jsonResponse.getString("username"));
                                profileIntent.putExtra("email", jsonResponse.getString("email"));

                                if (jsonResponse.getBoolean("avatar")) {
                                    Response.Listener<Bitmap> avatarListener = new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            ProfileActivity.civAvatar.setImageBitmap(response);
                                        }
                                    };

                                    queue.add(new AvatarRequest(jsonResponse.getString("filename"), avatarListener));
                                }

                                startActivity(profileIntent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                queue.add(new ProfileRequest(profileListener));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Response.Listener<String> logOutListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getBoolean("success")) {
                            Toast toast = Toast.makeText(context, jsonResponse.getString("message"), Toast.LENGTH_LONG);

                            toast.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            queue.add(new LogOutRequest(logOutListener));
        }

        return super.onKeyDown(keyCode, event);
    }
}
