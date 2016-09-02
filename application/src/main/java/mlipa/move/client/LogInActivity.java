package mlipa.move.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private static final String TAG = LogInActivity.class.toString();

    private static final String SERVER_SUCCESS_KEY = "success";
    private static final String SERVER_MESSAGE_KEY = "message";
    private static final String SERVER_USER_ID_KEY = "user_id";
    private static final String SERVER_USER_USERNAME_KEY = "user_username";
    private static final String CLIENT_USER_ID_KEY = "userId";

    private Context context;
    private RequestQueue queue;

    public static DatabaseHandler databaseHandler;
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

        databaseHandler = new DatabaseHandler(context);
        database = databaseHandler.getWritableDatabase();

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        dashboardIntent = new Intent(context, DashboardActivity.class);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        bLogIn = (Button) findViewById(R.id.b_log_in);

        bLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.trim().length() == 0) {
                    etUsername.setError(getString(R.string.required_field_message));
                } else if (password.trim().length() == 0) {
                    etPassword.setError(getString(R.string.required_field_message));
                } else {
                    Response.Listener<String> logInListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String message = jsonResponse.getString(SERVER_MESSAGE_KEY);

                                if (jsonResponse.getBoolean(SERVER_SUCCESS_KEY)) {
                                    etUsername.setText("");
                                    etUsername.clearFocus();
                                    etPassword.setText("");
                                    etPassword.clearFocus();

                                    String[] iuProjection = {
                                            UsersContract.Users._ID,
                                            UsersContract.Users.COLUMN_NAME_USERNAME
                                    };
                                    String iuSelection = UsersContract.Users._ID + " = ?";
                                    String[] iuSelectionArgs = {jsonResponse.getString(SERVER_USER_ID_KEY)};

                                    Cursor iuCursor = database.query(
                                            UsersContract.Users.TABLE_NAME,
                                            iuProjection,
                                            iuSelection,
                                            iuSelectionArgs,
                                            null, null, null
                                    );

                                    if (iuCursor.getCount() == 0) {
                                        ContentValues values = new ContentValues();

                                        values.put(UsersContract.Users._ID, jsonResponse.getString(SERVER_USER_ID_KEY));
                                        values.put(UsersContract.Users.COLUMN_NAME_USERNAME, jsonResponse.getString(SERVER_USER_USERNAME_KEY));

                                        Long id = database.insert(UsersContract.Users.TABLE_NAME, null, values);

                                        Log.v(TAG, "Row (id " + String.valueOf(id) + ") created successfully!");
                                    } else if (iuCursor.getCount() == 1) {
                                        iuCursor.moveToFirst();

                                        if (!iuCursor.getString(iuCursor.getColumnIndex(UsersContract.Users.COLUMN_NAME_USERNAME)).equals(jsonResponse.getString(SERVER_USER_USERNAME_KEY))) {
                                            ContentValues values = new ContentValues();

                                            values.put(UsersContract.Users.COLUMN_NAME_USERNAME, jsonResponse.getString(SERVER_USER_USERNAME_KEY));

                                            Integer count = database.update(
                                                    UsersContract.Users.TABLE_NAME,
                                                    values,
                                                    iuSelection,
                                                    iuSelectionArgs);

                                            Log.v(TAG, String.valueOf(count) + " row(s) updated successfully!");
                                        }
                                    }

                                    SharedPreferences.Editor editor = Cookie.preferences.edit();

                                    editor.putInt(CLIENT_USER_ID_KEY, Integer.parseInt(jsonResponse.getString(SERVER_USER_ID_KEY)));
                                    editor.commit();

                                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(dashboardIntent);
                                }

                                Log.v(TAG, SERVER_MESSAGE_KEY + " = " + message);

                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    queue.add(new LogInRequest(username, password, logInListener));
                }
            }
        });
    }
}
