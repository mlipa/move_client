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
    private SharedPreferences sharedPreferences;
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
        sharedPreferences = getSharedPreferences(getString(R.string.cookie_shared_preferences_key), Context.MODE_PRIVATE);
        database = SplashActivity.databaseHandler.getWritableDatabase();

        dashboardIntent = new Intent(context, DashboardActivity.class);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        bLogIn = (Button) findViewById(R.id.b_log_in);

        bLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                if (username.trim().length() == 0) {
                    etUsername.setError(getString(R.string.required_field_message));
                } else if (password.trim().length() == 0) {
                    etPassword.setError(getString(R.string.required_field_message));
                } else {
                    final ProgressDialog dialog = new ProgressDialog(LogInActivity.this);

                    dialog.setTitle(getString(R.string.log_in));
                    dialog.setMessage(getString(R.string.log_in_message));
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
                                        String message = jsonResponse.getString(getString(R.string.server_message_key));

                                        if (jsonResponse.getBoolean(getString(R.string.server_success_key))) {
                                            etUsername.setText("");
                                            etUsername.clearFocus();
                                            etPassword.setText("");
                                            etPassword.clearFocus();

                                            String userId = jsonResponse.getString(getString(R.string.server_user_id_key));

                                            String[] iuProjection = {
                                                    UsersContract._ID,
                                                    UsersContract.USERNAME
                                            };
                                            String iuSelection = UsersContract._ID + " = ?";
                                            String[] iuSelectionArgs = {userId};

                                            Cursor iuCursor = database.query(
                                                    UsersContract.TABLE_NAME,
                                                    iuProjection,
                                                    iuSelection,
                                                    iuSelectionArgs,
                                                    null, null, null
                                            );

                                            if (iuCursor.getCount() == 0) {
                                                ContentValues values = new ContentValues();
                                                String username = jsonResponse.getString(getString(R.string.server_username_key));

                                                values.put(UsersContract._ID, userId);
                                                values.put(UsersContract.USERNAME, username);

                                                Long id = database.insert(UsersContract.TABLE_NAME, null, values);

                                                Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_user_id_key) + " = " + userId);
                                                Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_username_key) + " = " + username);
                                                Log.v(TAG, "[logInListener.onResponse()] Row inserted successfully!");
                                            } else if (iuCursor.getCount() == 1) {
                                                iuCursor.moveToFirst();

                                                if (!iuCursor.getString(iuCursor.getColumnIndex(UsersContract.USERNAME)).equals(username)) {
                                                    ContentValues values = new ContentValues();

                                                    values.put(UsersContract.USERNAME, username);

                                                    Integer updatedRows = database.update(
                                                            UsersContract.TABLE_NAME,
                                                            values,
                                                            iuSelection,
                                                            iuSelectionArgs);

                                                    Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_user_id_key) + " = " + userId);
                                                    Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_username_key) + " = " + username);
                                                    Log.v(TAG, "[logInListener.onResponse()] " + String.valueOf(updatedRows) + " row(s) updated successfully!");
                                                }
                                            }

                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            editor.putInt(getString(R.string.client_user_id_key), Integer.parseInt(userId));
                                            editor.apply();

                                            dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(dashboardIntent);
                                        }

                                        dialog.dismiss();

                                        Log.v(TAG, "[logInListener.onResponse()] " + getString(R.string.server_message_key) + " = " + message);

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
