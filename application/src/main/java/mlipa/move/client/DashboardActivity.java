package mlipa.move.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = DashboardActivity.class.toString();

    private Context context;
    private RequestQueue queue;
    private SQLiteDatabase database;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Intent addIntent;
    private Intent settingsIntent;
    private Intent profileIntent;
    private Intent logInIntent;

    private ImageView ivCurrentActivity;
    private ImageView ivFirstActivity;
    private ImageView ivSecondActivity;
    private ImageView ivThirdActivity;
    private ImageView ivFourthActivity;
    private ImageView ivFifthActivity;
    private TextView tvGravityX;
    private TextView tvGravityY;
    private TextView tvGravityZ;
    private TextView tvAccelerometerX;
    private TextView tvAccelerometerY;
    private TextView tvAccelerometerZ;
    private FloatingActionButton fab;

    private Response.Listener<String> logOutListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.getBoolean(getString(R.string.server_success_key))) {
                    String message = jsonResponse.getString(getString(R.string.server_message_key));

                    Log.v(TAG, "[logOutListener.onResponse()] " + getString(R.string.server_message_key) + " = " + message);

                    logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logInIntent);

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        context = getApplicationContext();
        queue = Volley.newRequestQueue(context);
        database = SplashActivity.databaseHandler.getWritableDatabase();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(DashboardActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        addIntent = new Intent(context, AddActivity.class);
        settingsIntent = new Intent(context, SettingsActivity.class);
        profileIntent = new Intent(context, ProfileActivity.class);
        logInIntent = new Intent(context, LogInActivity.class);

        ivCurrentActivity = (ImageView) findViewById(R.id.iv_current_activity);
        ivFirstActivity = (ImageView) findViewById(R.id.iv_first_activity);
        ivSecondActivity = (ImageView) findViewById(R.id.iv_second_activity);
        ivThirdActivity = (ImageView) findViewById(R.id.iv_third_activity);
        ivFourthActivity = (ImageView) findViewById(R.id.iv_fourth_activity);
        ivFifthActivity = (ImageView) findViewById(R.id.iv_fifth_activity);
        tvGravityX = (TextView) findViewById(R.id.tv_gravity_x);
        tvGravityY = (TextView) findViewById(R.id.tv_gravity_y);
        tvGravityZ = (TextView) findViewById(R.id.tv_gravity_z);
        tvAccelerometerX = (TextView) findViewById(R.id.tv_accelerometer_x);
        tvAccelerometerY = (TextView) findViewById(R.id.tv_accelerometer_y);
        tvAccelerometerZ = (TextView) findViewById(R.id.tv_accelerometer_z);
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
                startActivity(settingsIntent);

                return true;
            case R.id.profile:
                Response.Listener<String> profileListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean(getString(R.string.server_success_key))) {
                                String name = jsonResponse.getString(getString(R.string.server_name_key));
                                String username = jsonResponse.getString(getString(R.string.server_username_key));
                                String email = jsonResponse.getString(getString(R.string.server_email_key));
                                Boolean avatar = jsonResponse.getBoolean(getString(R.string.server_avatar_key));

                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_name_key) + " = " + name);
                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_username_key) + " = " + username);
                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_email_key) + " = " + email);
                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_avatar_key) + " = " + avatar.toString());

                                profileIntent.putExtra(getString(R.string.client_name_key), name);
                                profileIntent.putExtra(getString(R.string.client_username_key), username);
                                profileIntent.putExtra(getString(R.string.client_email_key), email);

                                if (avatar) {
                                    String filename = jsonResponse.getString(getString(R.string.server_filename_key));

                                    Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_filename_key) + " = " + filename);

                                    Response.Listener<Bitmap> avatarListener = new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            ProfileActivity.civAvatar.setImageBitmap(response);
                                        }
                                    };

                                    queue.add(new AvatarRequest(context, filename, avatarListener));
                                }

                                startActivity(profileIntent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                queue.add(new ProfileRequest(context, profileListener));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        String[] iProjection = {
                RawsContract._ID
        };

        Cursor iCursor = database.query(
                RawsContract.TABLE_NAME,
                iProjection,
                null, null, null, null, null
        );

        if (iCursor.getCount() <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

            builder.setTitle(R.string.first_things_first);
            builder.setMessage(R.string.first_things_first_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(addIntent);
                }
            });
            builder.setNegativeButton(R.string.log_out, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    queue.add(new LogOutRequest(context, logOutListener));
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else if (!SplashActivity.neuralNetworkActive) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

            builder.setTitle(R.string.one_last_step);
            builder.setMessage(R.string.one_last_step_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SettingsActivity.fab.callOnClick();
                }
            });
            builder.setNegativeButton(R.string.log_out, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    queue.add(new LogOutRequest(context, logOutListener));
                }
            });
            builder.setCancelable(false);
            builder.create().show();
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
                    queue.add(new LogOutRequest(context, logOutListener));
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

        sensorManager.unregisterListener(DashboardActivity.this, accelerometer);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(DashboardActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        sensorManager.unregisterListener(DashboardActivity.this, accelerometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final Double alpha = 0.8;

        ArrayList<Double> gravity = new ArrayList<>(3);
        ArrayList<Double> acceleration = new ArrayList<>(3);

        for (int i = 0; i < 3; i++) {
            gravity.add(0.0);
            acceleration.add(0.0);

            gravity.set(i, alpha * gravity.get(i) + (1 - alpha) * event.values[i]);
            acceleration.set(i, event.values[i] - gravity.get(i));
        }

        tvGravityX.setText("");
        tvGravityY.setText("");
        tvGravityZ.setText("");
        tvAccelerometerX.setText("");
        tvAccelerometerY.setText("");
        tvAccelerometerZ.setText("");

        tvGravityX.setText(" " + String.format("%.8f", gravity.get(0)) + " ");
        tvGravityY.setText(" " + String.format("%.8f", gravity.get(1)) + " ");
        tvGravityZ.setText(" " + String.format("%.8f", gravity.get(2)) + " ");
        tvAccelerometerX.setText(" " + String.format("%.8f", acceleration.get(0)) + " ");
        tvAccelerometerY.setText(" " + String.format("%.8f", acceleration.get(1)) + " ");
        tvAccelerometerZ.setText(" " + String.format("%.8f", acceleration.get(2)) + " ");
    }
}
