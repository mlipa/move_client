package mlipa.move.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = DashboardActivity.class.toString();

    private Context context;
    private RequestQueue queue;
    private SharedPreferences settingsPreferences;
    private SharedPreferences profilePreferences;
    private SQLiteDatabase database;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Integer windowLength;
    private Calendar calendar;
    private Date timestampStop;
    private ArrayList<ArrayList<Double>> gravities;
    private ArrayList<ArrayList<Double>> accelerations;
    private Boolean predictionActive;
    private Integer lastPredicted;
    private ArrayList<Integer> activityDrawables;

    private SimpleDateFormat dateFormatter;
    private String classifierId;
    private Integer userId;

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

                if (jsonResponse.getBoolean(getString(R.string.server_success))) {
                    String message = jsonResponse.getString(getString(R.string.server_message));

                    Log.v(TAG, "[logOutListener.onResponse()] " + getString(R.string.server_message) + " = " + message);

                    logInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(logInIntent);

                    sensorManager.unregisterListener(DashboardActivity.this, accelerometer);

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
        settingsPreferences = getSharedPreferences(getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE);
        profilePreferences = getSharedPreferences(getString(R.string.shared_preferences_profile), Context.MODE_PRIVATE);
        database = SplashActivity.databaseHandler.getWritableDatabase();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Double settingsPreferencesWindowLength = Double.longBitsToDouble(settingsPreferences.getLong(getString(R.string.shared_preferences_settings_window_length), Double.doubleToLongBits(Constants.DEFAULT_WINDOW_LENGTH))) * 1000.0;
        windowLength = settingsPreferencesWindowLength.intValue();
        calendar = Calendar.getInstance();
        getTimestampStop();
        gravities = new ArrayList<>();
        accelerations = new ArrayList<>();
        predictionActive = false;
        lastPredicted = Constants.ACTIVITY_NOT_DETECTED_ID;
        activityDrawables = new ArrayList<>();

        dateFormatter = new SimpleDateFormat(getString(R.string.formatter_date));
        classifierId = settingsPreferences.getString(getString(R.string.shared_preferences_settings_classifier_id), Constants.DEFAULT_CLASSIFIER_ID);
        userId = profilePreferences.getInt(getString(R.string.shared_preferences_profile_user_id), Constants.USER_NOT_DETECTED_ID);

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(addIntent);

                sensorManager.unregisterListener(DashboardActivity.this, accelerometer);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        sensorManager.registerListener(DashboardActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        String[] predictionProjection = {
                PredictionsContract.ACTIVITY_ID
        };
        String predictionSelection = PredictionsContract.USER_ID + " = ?";
        String[] predictionSelectionArgs = {String.valueOf(userId)};

        Cursor predictionCursor = database.query(
                PredictionsContract.TABLE_NAME,
                predictionProjection,
                predictionSelection,
                predictionSelectionArgs,
                null, null, null
        );

        Integer predictionCount = predictionCursor.getCount();

        if (predictionCount > 0) {
            predictionCursor.moveToLast();

            Integer count = (predictionCount > 6) ? 6 : predictionCount;

            activityDrawables.clear();

            lastPredicted = predictionCursor.getInt(predictionCursor.getColumnIndex(PredictionsContract.ACTIVITY_ID));

            for (int i = 0; i < count; i++) {
                switch (predictionCursor.getInt(predictionCursor.getColumnIndex(PredictionsContract.ACTIVITY_ID))) {
                    case 1:
                        activityDrawables.add(0, R.drawable.activity_lie);

                        break;
                    case 2:
                        activityDrawables.add(0, R.drawable.activity_sit);

                        break;
                    case 3:
                        activityDrawables.add(0, R.drawable.activity_stand);

                        break;
                    case 4:
                        activityDrawables.add(0, R.drawable.activity_walk);

                        break;
                    default:
                        break;
                }

                predictionCursor.moveToPrevious();
            }

            showActivityDrawables();
        }

        String[] rawsProjection = {
                RawsContract._ID
        };

        Cursor rawsCursor = database.query(
                RawsContract.TABLE_NAME,
                rawsProjection,
                null, null, null, null, null
        );

        if (rawsCursor.getCount() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

            builder.setTitle(R.string.title_first_things_first);
            builder.setMessage(R.string.message_first_things_first);
            builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(addIntent);

                    sensorManager.unregisterListener(DashboardActivity.this, accelerometer);
                }
            });
            builder.setNegativeButton(R.string.title_log_out, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            queue.add(new LogOutRequest(context, logOutListener));
                        }
                    }).start();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else if (!SplashActivity.artificialNeuralNetworkActive) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

            builder.setTitle(R.string.title_one_last_step);
            builder.setMessage(R.string.message_one_last_step);
            builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(settingsIntent);

                    sensorManager.unregisterListener(DashboardActivity.this, accelerometer);
                }
            });
            builder.setNegativeButton(R.string.title_log_out, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            queue.add(new LogOutRequest(context, logOutListener));
                        }
                    }).start();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else {
            getTimestampStop();
        }
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

                sensorManager.unregisterListener(DashboardActivity.this, accelerometer);

                return true;
            case R.id.profile:
                Response.Listener<String> profileListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean(getString(R.string.server_success))) {
                                String name = jsonResponse.getString(getString(R.string.server_name));
                                String username = jsonResponse.getString(getString(R.string.server_username));
                                String email = jsonResponse.getString(getString(R.string.server_email));

                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_name) + " = " + name);
                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_username) + " = " + username);
                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_email) + " = " + email);

                                SharedPreferences.Editor editor = profilePreferences.edit();

                                editor.putString(getString(R.string.shared_preferences_profile_name), name);
                                editor.putString(getString(R.string.shared_preferences_profile_username), username);
                                editor.putString(getString(R.string.shared_preferences_profile_email), email);
                                editor.apply();

                                Boolean avatar = jsonResponse.getBoolean(getString(R.string.server_avatar));

                                Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_avatar) + " = " + avatar.toString());

                                if (avatar) {
                                    String filename = jsonResponse.getString(getString(R.string.server_filename));

                                    Log.v(TAG, "[profileListener.onResponse()] " + getString(R.string.server_filename) + " = " + filename);

                                    Response.Listener<Bitmap> avatarListener = new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            ProfileActivity.civAvatar.setImageBitmap(response);
                                        }
                                    };

                                    queue.add(new AvatarRequest(context, filename, avatarListener));
                                }

                                startActivity(profileIntent);

                                sensorManager.unregisterListener(DashboardActivity.this, accelerometer);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

                builder.setTitle(R.string.title_log_out);
                builder.setMessage(R.string.message_log_out);
                builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                queue.add(new LogOutRequest(context, logOutListener));
                            }
                        }).start();
                    }
                });
                builder.setNegativeButton(R.string.button_no, null);
                builder.create().show();
            default:
                return super.onKeyDown(keyCode, event);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ArrayList<Double> gravity = new ArrayList<>(3);
        ArrayList<Double> acceleration = new ArrayList<>(3);

        for (int i = 0; i < 3; i++) {
            gravity.add(0.0);
            gravity.set(i, Constants.ACCELERATION_ALPHA * gravity.get(i) + (1 - Constants.ACCELERATION_ALPHA) * event.values[i]);

            acceleration.add(0.0);
            acceleration.set(i, event.values[i] - gravity.get(i));
        }

        tvGravityX.setText(String.format(getString(R.string.formatter_gravity_acceleration), gravity.get(0)));
        tvGravityY.setText(String.format(getString(R.string.formatter_gravity_acceleration), gravity.get(1)));
        tvGravityZ.setText(String.format(getString(R.string.formatter_gravity_acceleration), gravity.get(2)));
        tvAccelerometerX.setText(String.format(getString(R.string.formatter_gravity_acceleration), acceleration.get(0)));
        tvAccelerometerY.setText(String.format(getString(R.string.formatter_gravity_acceleration), acceleration.get(1)));
        tvAccelerometerZ.setText(String.format(getString(R.string.formatter_gravity_acceleration), acceleration.get(2)));

        if (SplashActivity.artificialNeuralNetworkActive) {
            if (new Date().compareTo(timestampStop) >= 0) {
                gravities.add(gravity);
                accelerations.add(acceleration);
            }

            if (!predictionActive) {
                predictionActive = true;

                if (new Date().compareTo(timestampStop) == 1) {
                    ArrayList<Double> gravityXArray = new ArrayList<>();
                    ArrayList<Double> gravityYArray = new ArrayList<>();
                    ArrayList<Double> gravityZArray = new ArrayList<>();
                    ArrayList<Double> accelerationXArray = new ArrayList<>();
                    ArrayList<Double> accelerationYArray = new ArrayList<>();
                    ArrayList<Double> accelerationZArray = new ArrayList<>();
                    ArrayList<Double> minimum = new ArrayList<>(6);
                    ArrayList<Double> maximum = new ArrayList<>(6);
                    ArrayList<Double> sum = new ArrayList<>(6);
                    ArrayList<Double> squareSum = new ArrayList<>(6);

                    for (int i = 0; i < 6; i++) {
                        minimum.add(100.0);
                        maximum.add(-100.0);
                        sum.add(0.0);
                        squareSum.add(0.0);
                    }

                    Integer rawsSize = gravities.size();

                    for (int i = 0; i < rawsSize; i++) {
                        Double gravityX = gravities.get(i).get(0);
                        Double gravityY = gravities.get(i).get(1);
                        Double gravityZ = gravities.get(i).get(2);
                        Double accelerationX = accelerations.get(i).get(0);
                        Double accelerationY = accelerations.get(i).get(1);
                        Double accelerationZ = accelerations.get(i).get(2);

                        gravityXArray.add(gravityX);
                        gravityYArray.add(gravityY);
                        gravityZArray.add(gravityZ);
                        accelerationXArray.add(accelerationX);
                        accelerationYArray.add(accelerationY);
                        accelerationZArray.add(accelerationZ);

                        if (gravityX < minimum.get(0)) {
                            minimum.set(0, gravityX);
                        }
                        if (gravityY < minimum.get(1)) {
                            minimum.set(1, gravityY);
                        }
                        if (gravityZ < minimum.get(2)) {
                            minimum.set(2, gravityZ);
                        }
                        if (accelerationX < minimum.get(3)) {
                            minimum.set(3, accelerationX);
                        }
                        if (accelerationY < minimum.get(4)) {
                            minimum.set(4, accelerationY);
                        }
                        if (accelerationZ < minimum.get(5)) {
                            minimum.set(5, accelerationZ);
                        }

                        if (gravityX > maximum.get(0)) {
                            maximum.set(0, gravityX);
                        }
                        if (gravityY > maximum.get(1)) {
                            maximum.set(1, gravityY);
                        }
                        if (gravityZ > maximum.get(2)) {
                            maximum.set(2, gravityZ);
                        }
                        if (accelerationX > maximum.get(3)) {
                            maximum.set(3, accelerationX);
                        }
                        if (accelerationY > maximum.get(4)) {
                            maximum.set(4, accelerationY);
                        }
                        if (accelerationZ > minimum.get(5)) {
                            maximum.set(5, accelerationZ);
                        }

                        sum.set(0, sum.get(0) + gravityX);
                        sum.set(1, sum.get(1) + gravityY);
                        sum.set(2, sum.get(2) + gravityZ);
                        sum.set(3, sum.get(3) + accelerationX);
                        sum.set(4, sum.get(4) + accelerationY);
                        sum.set(5, sum.get(5) + accelerationZ);

                        squareSum.set(0, squareSum.get(0) + Math.pow(gravityX, 2));
                        squareSum.set(1, squareSum.get(1) + Math.pow(gravityY, 2));
                        squareSum.set(2, squareSum.get(2) + Math.pow(gravityZ, 2));
                        squareSum.set(3, squareSum.get(3) + Math.pow(accelerationX, 2));
                        squareSum.set(4, squareSum.get(4) + Math.pow(accelerationY, 2));
                        squareSum.set(5, squareSum.get(5) + Math.pow(accelerationZ, 2));
                    }

                    ArrayList<Double> mean = new ArrayList<>(6);
                    ArrayList<Double> energy = new ArrayList<>(6);

                    for (int i = 0; i < 6; i++) {
                        mean.add(sum.get(i) / rawsSize);
                        energy.add(squareSum.get(i) / rawsSize);
                    }

                    Double[] sortGravityX = gravityXArray.toArray(new Double[gravityXArray.size()]);
                    Double[] sortGravityY = gravityYArray.toArray(new Double[gravityYArray.size()]);
                    Double[] sortGravityZ = gravityZArray.toArray(new Double[gravityZArray.size()]);
                    Double[] sortAccelerationX = accelerationXArray.toArray(new Double[accelerationXArray.size()]);
                    Double[] sortAccelerationY = accelerationYArray.toArray(new Double[accelerationYArray.size()]);
                    Double[] sortAccelerationZ = accelerationZArray.toArray(new Double[accelerationZArray.size()]);

                    Arrays.sort(sortGravityX);
                    Arrays.sort(sortGravityY);
                    Arrays.sort(sortGravityZ);
                    Arrays.sort(sortAccelerationX);
                    Arrays.sort(sortAccelerationY);
                    Arrays.sort(sortAccelerationZ);

                    Double medianGravityX;
                    Double medianGravityY;
                    Double medianGravityZ;
                    Double medianAccelerationX;
                    Double medianAccelerationY;
                    Double medianAccelerationZ;

                    if (rawsSize % 2 == 0) {
                        medianGravityX = (sortGravityX[rawsSize / 2 - 1] + sortGravityX[rawsSize / 2]) / 2;
                        medianGravityY = (sortGravityY[rawsSize / 2 - 1] + sortGravityY[rawsSize / 2]) / 2;
                        medianGravityZ = (sortGravityZ[rawsSize / 2 - 1] + sortGravityZ[rawsSize / 2]) / 2;
                        medianAccelerationX = (sortAccelerationX[rawsSize / 2 - 1] + sortAccelerationX[rawsSize / 2]) / 2;
                        medianAccelerationY = (sortAccelerationY[rawsSize / 2 - 1] + sortAccelerationY[rawsSize / 2]) / 2;
                        medianAccelerationZ = (sortAccelerationZ[rawsSize / 2 - 1] + sortAccelerationZ[rawsSize / 2]) / 2;

                    } else {
                        medianGravityX = sortGravityX[(rawsSize - 1) / 2];
                        medianGravityY = sortGravityY[(rawsSize - 1) / 2];
                        medianGravityZ = sortGravityZ[(rawsSize - 1) / 2];
                        medianAccelerationX = sortAccelerationX[(rawsSize - 1) / 2];
                        medianAccelerationY = sortAccelerationY[(rawsSize - 1) / 2];
                        medianAccelerationZ = sortAccelerationZ[(rawsSize - 1) / 2];
                    }

                    ArrayList<Double> deviationSum = new ArrayList<>(6);
                    ArrayList<Double> absoluteMedianGravityXArray = new ArrayList<>();
                    ArrayList<Double> absoluteMedianGravityYArray = new ArrayList<>();
                    ArrayList<Double> absoluteMedianGravityZArray = new ArrayList<>();
                    ArrayList<Double> absoluteMedianAccelerationXArray = new ArrayList<>();
                    ArrayList<Double> absoluteMedianAccelerationYArray = new ArrayList<>();
                    ArrayList<Double> absoluteMedianAccelerationZArray = new ArrayList<>();

                    for (int i = 0; i < 6; i++) {
                        deviationSum.add(0.0);
                    }

                    for (int i = 0; i < rawsSize; i++) {
                        Double gravityX = gravities.get(i).get(0);
                        Double gravityY = gravities.get(i).get(1);
                        Double gravityZ = gravities.get(i).get(2);
                        Double accelerationX = accelerations.get(i).get(0);
                        Double accelerationY = accelerations.get(i).get(1);
                        Double accelerationZ = accelerations.get(i).get(2);

                        ArrayList<Double> deviation = new ArrayList<>(6);

                        deviation.add(Math.pow(gravityX - mean.get(0), 2));
                        deviation.add(Math.pow(gravityY - mean.get(1), 2));
                        deviation.add(Math.pow(gravityZ - mean.get(2), 2));
                        deviation.add(Math.pow(accelerationX - mean.get(3), 2));
                        deviation.add(Math.pow(accelerationY - mean.get(4), 2));
                        deviation.add(Math.pow(accelerationZ - mean.get(5), 2));

                        for (int j = 0; j < 6; j++) {
                            deviationSum.set(j, deviationSum.get(j) + deviation.get(j));
                        }

                        absoluteMedianGravityXArray.add(Math.abs(gravityX - medianGravityX));
                        absoluteMedianGravityYArray.add(Math.abs(gravityY - medianGravityY));
                        absoluteMedianGravityZArray.add(Math.abs(gravityZ - medianGravityZ));
                        absoluteMedianAccelerationXArray.add(Math.abs(accelerationX - medianAccelerationX));
                        absoluteMedianAccelerationYArray.add(Math.abs(accelerationY - medianAccelerationY));
                        absoluteMedianAccelerationZArray.add(Math.abs(accelerationZ - medianAccelerationZ));
                    }

                    ArrayList<Double> standardDeviation = new ArrayList<>(6);

                    for (int i = 0; i < 6; i++) {
                        standardDeviation.add(Math.sqrt(deviationSum.get(i) / rawsSize));
                    }

                    Double[] sortAbsoluteMedianGravityX = absoluteMedianGravityXArray.toArray(new Double[absoluteMedianGravityXArray.size()]);
                    Double[] sortAbsoluteMedianGravityY = absoluteMedianGravityYArray.toArray(new Double[absoluteMedianGravityYArray.size()]);
                    Double[] sortAbsoluteMedianGravityZ = absoluteMedianGravityZArray.toArray(new Double[absoluteMedianGravityZArray.size()]);
                    Double[] sortAbsoluteMedianAccelerationX = absoluteMedianAccelerationXArray.toArray(new Double[absoluteMedianAccelerationXArray.size()]);
                    Double[] sortAbsoluteMedianAccelerationY = absoluteMedianAccelerationYArray.toArray(new Double[absoluteMedianAccelerationYArray.size()]);
                    Double[] sortAbsoluteMedianAccelerationZ = absoluteMedianAccelerationZArray.toArray(new Double[absoluteMedianAccelerationZArray.size()]);

                    Arrays.sort(sortAbsoluteMedianGravityX);
                    Arrays.sort(sortAbsoluteMedianGravityY);
                    Arrays.sort(sortAbsoluteMedianGravityZ);
                    Arrays.sort(sortAbsoluteMedianAccelerationX);
                    Arrays.sort(sortAbsoluteMedianAccelerationY);
                    Arrays.sort(sortAbsoluteMedianAccelerationZ);

                    Double absoluteMedianGravityX;
                    Double absoluteMedianGravityY;
                    Double absoluteMedianGravityZ;
                    Double absoluteMedianAccelerationX;
                    Double absoluteMedianAccelerationY;
                    Double absoluteMedianAccelerationZ;

                    if (rawsSize % 2 == 0) {
                        absoluteMedianGravityX = (sortAbsoluteMedianGravityX[rawsSize / 2 - 1] + sortAbsoluteMedianGravityX[rawsSize / 2]) / 2;
                        absoluteMedianGravityY = (sortAbsoluteMedianGravityY[rawsSize / 2 - 1] + sortAbsoluteMedianGravityY[rawsSize / 2]) / 2;
                        absoluteMedianGravityZ = (sortAbsoluteMedianGravityZ[rawsSize / 2 - 1] + sortAbsoluteMedianGravityZ[rawsSize / 2]) / 2;
                        absoluteMedianAccelerationX = (sortAbsoluteMedianAccelerationX[rawsSize / 2 - 1] + sortAbsoluteMedianAccelerationX[rawsSize / 2]) / 2;
                        absoluteMedianAccelerationY = (sortAbsoluteMedianAccelerationY[rawsSize / 2 - 1] + sortAbsoluteMedianAccelerationY[rawsSize / 2]) / 2;
                        absoluteMedianAccelerationZ = (sortAbsoluteMedianAccelerationZ[rawsSize / 2 - 1] + sortAbsoluteMedianAccelerationZ[rawsSize / 2]) / 2;
                    } else {
                        absoluteMedianGravityX = sortAbsoluteMedianGravityX[(rawsSize - 1) / 2];
                        absoluteMedianGravityY = sortAbsoluteMedianGravityY[(rawsSize - 1) / 2];
                        absoluteMedianGravityZ = sortAbsoluteMedianGravityZ[(rawsSize - 1) / 2];
                        absoluteMedianAccelerationX = sortAbsoluteMedianAccelerationX[(rawsSize - 1) / 2];
                        absoluteMedianAccelerationY = sortAbsoluteMedianAccelerationY[(rawsSize - 1) / 2];
                        absoluteMedianAccelerationZ = sortAbsoluteMedianAccelerationZ[(rawsSize - 1) / 2];
                    }

                    ArrayList<Double> features = new ArrayList<>();

                    features.addAll(minimum);
                    features.addAll(maximum);
                    features.addAll(mean);
                    features.addAll(energy);
                    features.addAll(standardDeviation);
                    features.add(absoluteMedianGravityX);
                    features.add(absoluteMedianGravityY);
                    features.add(absoluteMedianGravityZ);
                    features.add(absoluteMedianAccelerationX);
                    features.add(absoluteMedianAccelerationY);
                    features.add(absoluteMedianAccelerationZ);

                    ArrayList<Double> output = SplashActivity.artificialNeuralNetwork.run(features);

                    Integer predictedActivity = 0;

                    for (int i = 0; i < Constants.OUTPUT_NEURONS; i++) {
                        if (output.get(i) > output.get(predictedActivity)) {
                            predictedActivity = i;
                        }
                    }

                    predictedActivity++;

                    if (!predictedActivity.equals(lastPredicted)) {
                        lastPredicted = predictedActivity;

                        ContentValues values = new ContentValues();

                        String timestamp = dateFormatter.format(new Date());

                        values.put(PredictionsContract.TIMESTAMP, timestamp);
                        values.put(PredictionsContract.ACTIVITY_ID, predictedActivity);
                        values.put(PredictionsContract.CLASSIFIER_ID, classifierId);
                        values.put(PredictionsContract.USER_ID, userId);

                        database.insert(PredictionsContract.TABLE_NAME, null, values);

                        Response.Listener<String> predictionListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);

                                    if (jsonResponse.getBoolean(getString(R.string.server_success))) {
                                        String predictionId = jsonResponse.getString(getString(R.string.server_prediction_id));
                                        String timestamp = jsonResponse.getString(getString(R.string.server_timestamp));
                                        String activityId = jsonResponse.getString(getString(R.string.server_activity_id));
                                        String classifierId = jsonResponse.getString(getString(R.string.server_classifier_id));
                                        String user_id = jsonResponse.getString(getString(R.string.server_user_id));

                                        Log.v(TAG, "[predictionListener.onResponse()] " + getString(R.string.server_prediction_id) + " = " + predictionId);
                                        Log.v(TAG, "[predictionListener.onResponse()] " + getString(R.string.server_timestamp) + " = " + timestamp);
                                        Log.v(TAG, "[predictionListener.onResponse()] " + getString(R.string.server_activity_id) + " = " + activityId);
                                        Log.v(TAG, "[predictionListener.onResponse()] " + getString(R.string.server_classifier_id) + " = " + classifierId);
                                        Log.v(TAG, "[predictionListener.onResponse()] " + getString(R.string.server_user_id) + " = " + user_id);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        queue.add(new PredictionRequest(context, timestamp, String.valueOf(predictedActivity), classifierId, String.valueOf(userId), predictionListener));

                        switch (predictedActivity) {
                            case 1:
                                activityDrawables.add(R.drawable.activity_lie);

                                break;
                            case 2:
                                activityDrawables.add(R.drawable.activity_sit);

                                break;
                            case 3:
                                activityDrawables.add(R.drawable.activity_stand);

                                break;
                            case 4:
                                activityDrawables.add(R.drawable.activity_walk);

                                break;
                            default:
                                break;
                        }

                        if (activityDrawables.size() >= 7) {
                            activityDrawables.remove(0);
                        }

                        showActivityDrawables();
                    }

                    gravities.clear();
                    accelerations.clear();

                    getTimestampStop();
                }

                predictionActive = false;
            }
        }
    }

    private void getTimestampStop() {
        calendar.setTime(new Date());
        calendar.add(Calendar.MILLISECOND, windowLength);
        timestampStop = calendar.getTime();
    }

    private void showActivityDrawables() {
        Integer activityDrawablesSize = activityDrawables.size();

        switch (activityDrawablesSize) {
            case 6:
                ivFifthActivity.setImageResource(activityDrawables.get(activityDrawablesSize - 6));
            case 5:
                ivFourthActivity.setImageResource(activityDrawables.get(activityDrawablesSize - 5));
            case 4:
                ivThirdActivity.setImageResource(activityDrawables.get(activityDrawablesSize - 4));
            case 3:
                ivSecondActivity.setImageResource(activityDrawables.get(activityDrawablesSize - 3));
            case 2:
                ivFirstActivity.setImageResource(activityDrawables.get(activityDrawablesSize - 2));
            case 1:
                ivCurrentActivity.setImageResource(activityDrawables.get(activityDrawablesSize - 1));
            default:
                break;
        }
    }
}
