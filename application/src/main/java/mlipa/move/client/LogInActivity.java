package mlipa.move.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    EditText etUsername;
    EditText etPassword;
    Button bLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        etUsername = (EditText) findViewById(R.id.username);
        etPassword = (EditText) findViewById(R.id.password);
        bLogIn = (Button) findViewById(R.id.log_in);

        bLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                if (username.trim().equals("")) {
                    etUsername.setError("Please fill out this field.");
                } else if (password.trim().equals("")) {
                    etPassword.setError("Please fill out this field.");
                } else {
                    Response.Listener<String> listener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Context context = getApplicationContext();
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                String message = jsonResponse.getString("message");
                                Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

                                if (success) {
                                    Intent intent = new Intent(LogInActivity.this, DashboardActivity.class);
                                    LogInActivity.this.startActivity(intent);
                                }

                                toast.show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    LogInRequest request = new LogInRequest(username, password, listener);
                    RequestQueue queue = Volley.newRequestQueue(LogInActivity.this);
                    queue.add(request);
                }
            }
        });
    }
}
