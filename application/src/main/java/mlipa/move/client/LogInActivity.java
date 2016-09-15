package mlipa.move.client;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LogInActivity extends AppCompatActivity {
    private final String TAG = LogInActivity.class.toString();

    private Context context;
    private RequestQueue queue;
    private SharedPreferences preferences;
    private SQLiteDatabase database;

    private Intent dashboardIntent;

    private EditText etUsername;
    private EditText etPassword;
    private Button bLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        context = getApplicationContext();
        queue = Volley.newRequestQueue(context);
        preferences = getSharedPreferences(getString(R.string.shared_preferences_profile), Context.MODE_PRIVATE);
        database = SplashActivity.databaseHandler.getWritableDatabase();

        dashboardIntent = new Intent(context, DashboardActivity.class);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        bLogIn = (Button) findViewById(R.id.b_log_in);

        bLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                if (username.trim().length() == 0) {
                    etUsername.setError(getString(R.string.message_required));
                } else if (password.trim().length() == 0) {
                    etPassword.setError(getString(R.string.message_required));
                } else {
                    final ProgressDialog dialog = new ProgressDialog(LogInActivity.this);

                    dialog.setTitle(getString(R.string.title_log_in));
                    dialog.setMessage(getString(R.string.message_log_in));
                    dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);
                    dialog.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Response.Listener<String> logInListener = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        String message = jsonResponse.getString(getString(R.string.server_message));

                                        if (jsonResponse.getBoolean(getString(R.string.server_success))) {
                                            etUsername.setText("");
                                            etUsername.clearFocus();
                                            etPassword.setText("");
                                            etPassword.clearFocus();

                                            String userId = jsonResponse.getString(getString(R.string.server_user_id));
                                            String username = jsonResponse.getString(getString(R.string.server_username));

                                            String[] usersProjection = {
                                                    UsersContract._ID,
                                                    UsersContract.USERNAME
                                            };
                                            String usersSelection = UsersContract._ID + " = ?";
                                            String[] usersSelectionArgs = {userId};

                                            Cursor usersCursor = database.query(
                                                    UsersContract.TABLE_NAME,
                                                    usersProjection,
                                                    usersSelection,
                                                    usersSelectionArgs,
                                                    null, null, null
                                            );

                                            if (usersCursor.getCount() == 0) {
                                                ContentValues values = new ContentValues();

                                                values.put(UsersContract._ID, userId);
                                                values.put(UsersContract.USERNAME, username);

                                                database.insert(UsersContract.TABLE_NAME, null, values);

                                                Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_user_id) + " = " + userId);
                                                Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_username) + " = " + username);
                                                Log.v(TAG, "[logInListener.onResponse()] Row inserted successfully!");
                                            } else if (usersCursor.getCount() == 1) {
                                                usersCursor.moveToFirst();

                                                if (!usersCursor.getString(usersCursor.getColumnIndex(UsersContract.USERNAME)).equals(username)) {
                                                    ContentValues values = new ContentValues();

                                                    values.put(UsersContract.USERNAME, username);

                                                    Integer updatedRows = database.update(
                                                            UsersContract.TABLE_NAME,
                                                            values,
                                                            usersSelection,
                                                            usersSelectionArgs);

                                                    Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_user_id) + " = " + userId);
                                                    Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_username) + " = " + username);
                                                    Log.v(TAG, "[logInListener.onResponse()] " + String.valueOf(updatedRows) + " row(s) updated successfully!");
                                                }
                                            }

                                            SharedPreferences.Editor editor = preferences.edit();

                                            editor.putInt(getString(R.string.shared_preferences_profile_user_id), Integer.parseInt(userId));
                                            editor.apply();

                                            startActivity(dashboardIntent);
                                        }

                                        dialog.dismiss();

                                        Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_message) + " = " + message);

                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            queue.add(new LogInRequest(context, username, password, logInListener));
                        }
                    }).start();
                }
            }
        });
    }
}
