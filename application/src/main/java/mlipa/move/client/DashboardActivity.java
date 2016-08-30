package mlipa.move.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = DashboardActivity.class.toString();

    private Context context;
    private RequestQueue queue;

    private Intent addIntent;
    private Intent settingsIntent;
    private Intent profileIntent;
    private Intent logInIntent;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private ImageView ivCurrentActivity;
    private ImageView ivFirstActivity;
    private ImageView ivSecondActivity;
    private ImageView ivThirdActivity;
    private ImageView ivFourthActivity;
    private ImageView ivFifthActivity;
    private TextView tvXAccelerometer;
    private TextView tvYAccelerometer;
    private TextView tvZAccelerometer;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        context = getApplicationContext();
        queue = Volley.newRequestQueue(DashboardActivity.this);

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);

        addIntent = new Intent(DashboardActivity.this, AddActivity.class);
        settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
        profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
        logInIntent = new Intent(DashboardActivity.this, LogInActivity.class);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(DashboardActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        ivCurrentActivity = (ImageView) findViewById(R.id.iv_current_activity);
        ivFirstActivity = (ImageView) findViewById(R.id.iv_first_activity);
        ivSecondActivity = (ImageView) findViewById(R.id.iv_second_activity);
        ivThirdActivity = (ImageView) findViewById(R.id.iv_third_activity);
        ivFourthActivity = (ImageView) findViewById(R.id.iv_fourth_activity);
        ivFifthActivity = (ImageView) findViewById(R.id.iv_fifth_activity);
        tvXAccelerometer = (TextView) findViewById(R.id.tv_x_accelerometer);
        tvYAccelerometer = (TextView) findViewById(R.id.tv_y_accelerometer);
        tvZAccelerometer = (TextView) findViewById(R.id.tv_z_accelerometer);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // TODO: CALCULATE CURRENT ACTIVITY FROM ALGORITHM
        // TODO: ADD LAST FIVE ACTIVITIES ICONS FROM DATABASE

        fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.bootstrap_green)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(addIntent);
            }
        });
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
                Response.Listener<String> settingsListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean("success")) {
                                String classifierId = jsonResponse.getString("classifier_id");
                                String classifierName = jsonResponse.getString("classifier_name");

                                Log.v(TAG, "classifier_id = " + classifierId);
                                Log.v(TAG, "classifier_name = " + classifierName);

                                settingsIntent.putExtra("classifierId", classifierId);

                                startActivity(settingsIntent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                queue.add(new SettingsRequest(Request.Method.GET, null, settingsListener));

                return true;
            case R.id.profile:
                Response.Listener<String> profileListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean("success")) {
                                String name = jsonResponse.getString("name");
                                String username = jsonResponse.getString("username");
                                String email = jsonResponse.getString("email");
                                Boolean avatar = jsonResponse.getBoolean("avatar");

                                Log.v(TAG, "name = " + name);
                                Log.v(TAG, "username = " + username);
                                Log.v(TAG, "email = " + email);
                                Log.v(TAG, "avatar = " + avatar.toString());

                                profileIntent.putExtra("name", name);
                                profileIntent.putExtra("username", username);
                                profileIntent.putExtra("email", email);

                                if (avatar) {
                                    String filename = jsonResponse.getString("filename");

                                    Log.v(TAG, "filename = " + filename);

                                    Response.Listener<Bitmap> avatarListener = new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            ProfileActivity.civAvatar.setImageBitmap(response);
                                        }
                                    };

                                    queue.add(new AvatarRequest(filename, avatarListener));
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
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

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

                                if (jsonResponse.getBoolean("success")) {
                                    String message = jsonResponse.getString("message");

                                    Log.v(TAG, message);

                                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

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
            builder.setNegativeButton(R.string.no, null);
            builder.create().show();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(DashboardActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(DashboardActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final double alpha = 0.8;

        double[] gravity = new double[3];
        double[] linearAcceleration = new double[3];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linearAcceleration[0] = event.values[0] - gravity[0];
        linearAcceleration[1] = event.values[1] - gravity[1];
        linearAcceleration[2] = event.values[2] - gravity[2];

        tvXAccelerometer.setText("");
        tvYAccelerometer.setText("");
        tvZAccelerometer.setText("");

        tvXAccelerometer.setText(" " + String.format("%f", linearAcceleration[0]));
        tvYAccelerometer.setText(" " + String.format("%f", linearAcceleration[1]));
        tvZAccelerometer.setText(" " + String.format("%f", linearAcceleration[2]));
    }
}
