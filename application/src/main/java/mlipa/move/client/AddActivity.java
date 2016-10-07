package mlipa.move.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = AddActivity.class.toString();

    private Context context;
    private SharedPreferences settingsPreferences;
    private SharedPreferences profilePreferences;
    private SQLiteDatabase database;
    private MediaPlayer player;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Boolean accelerometerActive;

    private Integer chronometerTime;
    private Integer delayTime;
    private Integer chronometerMinutes;
    private Integer chronometerSeconds;
    private Integer delayMinutes;
    private Integer delaySeconds;

    private CountDownTimer chronometer;
    private CountDownTimer delay;

    private SimpleDateFormat dateFormatter;
    private Integer activityId;
    private Integer userId;
    private Integer insertedRows;

    private ImageView ivActivity;
    private RadioGroup rgActivities;
    private RadioButton rbActivityLie;
    private TextView tvChronometerDelay;
    private Button bStartStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        context = getApplicationContext();
        settingsPreferences = getSharedPreferences(getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE);
        profilePreferences = getSharedPreferences(getString(R.string.shared_preferences_profile), Context.MODE_PRIVATE);
        database = SplashActivity.databaseHandler.getWritableDatabase();
        player = MediaPlayer.create(context, R.raw.notify);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerActive = false;

        Double settingsPreferencesChronometerTime = Double.longBitsToDouble(settingsPreferences.getLong(getString(R.string.shared_preferences_settings_chronometer_time), Double.doubleToLongBits(Constants.DEFAULT_CHRONOMETER_TIME))) * 1000.0;
        Double settingsPreferencesDelayTime = Double.longBitsToDouble(settingsPreferences.getLong(getString(R.string.shared_preferences_settings_delay_time), Double.doubleToLongBits(Constants.DEFAULT_DELAY_TIME))) * 1000.0;
        chronometerTime = settingsPreferencesChronometerTime.intValue();
        delayTime = settingsPreferencesDelayTime.intValue();
        chronometerMinutes = (chronometerTime / 1000) / 60;
        chronometerSeconds = (chronometerTime / 1000) % 60;
        delayMinutes = (delayTime / 1000) / 60;
        delaySeconds = (delayTime / 1000) % 60;

        chronometer = new CountDownTimer(chronometerTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Long minutes = (millisUntilFinished / 1000) / 60;
                Long seconds = (millisUntilFinished / 1000) % 60;

                tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), minutes, seconds));
            }

            @Override
            public void onFinish() {
                player.start();

                sensorManager.unregisterListener(AddActivity.this, accelerometer);
                accelerometerActive = false;

                for (int i = 0; i < rgActivities.getChildCount(); i++) {
                    rgActivities.getChildAt(i).setEnabled(true);
                }

                tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), chronometerMinutes, chronometerSeconds));

                bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));
                bStartStop.setText(getString(R.string.add_activity_start));

                SettingsActivity.addActivityFlag = true;

                Log.v(TAG, "[chronometer.onFinish()] " + String.valueOf(insertedRows) + " row(s) inserted successfully!");

                insertedRows = 0;
            }
        };
        delay = new CountDownTimer(delayTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Long minutes = (millisUntilFinished / 1000) / 60;
                Long seconds = (millisUntilFinished / 1000) % 60;

                tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), minutes, seconds));
            }

            @Override
            public void onFinish() {
                player.start();

                sensorManager.registerListener(AddActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                accelerometerActive = true;

                chronometer.start();

                tvChronometerDelay.setTextColor(getColor(R.color.black));
                tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), chronometerMinutes, chronometerSeconds));
            }
        };

        dateFormatter = new SimpleDateFormat(getString(R.string.formatter_date));
        activityId = Constants.ACTIVITY_NOT_DETECTED_ID;
        userId = profilePreferences.getInt(getString(R.string.shared_preferences_profile_user_id), Constants.USER_NOT_DETECTED_ID);
        insertedRows = 0;

        ivActivity = (ImageView) findViewById(R.id.iv_activity);
        rgActivities = (RadioGroup) findViewById(R.id.rg_activities);
        rbActivityLie = (RadioButton) findViewById(R.id.rb_activity_lie);
        tvChronometerDelay = (TextView) findViewById(R.id.tv_delay_chronometer);
        bStartStop = (Button) findViewById(R.id.b_start_stop);

        rbActivityLie.toggle();
        onRadioButtonClicked(rbActivityLie);

        tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), chronometerMinutes, chronometerSeconds));

        bStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bStartStop.getText().equals(getString(R.string.add_activity_start))) {
                    delay.start();

                    for (int i = 0; i < rgActivities.getChildCount(); i++) {
                        rgActivities.getChildAt(i).setEnabled(false);
                    }

                    tvChronometerDelay.setTextColor(getColor(R.color.bootstrap_red));
                    tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), delayMinutes, delaySeconds));

                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_red));
                    bStartStop.setText(getString(R.string.add_activity_stop));
                } else {
                    if (accelerometerActive) {
                        player.start();

                        sensorManager.unregisterListener(AddActivity.this, accelerometer);
                        accelerometerActive = false;

                        SettingsActivity.addActivityFlag = true;

                        Log.v(TAG, "[bStartStop.onClick()] " + String.valueOf(insertedRows) + " row(s) inserted successfully!");
                    }

                    chronometer.cancel();
                    delay.cancel();

                    for (int i = 0; i < rgActivities.getChildCount(); i++) {
                        rgActivities.getChildAt(i).setEnabled(true);
                    }

                    tvChronometerDelay.setTextColor(getColor(R.color.black));
                    tvChronometerDelay.setText(String.format(getString(R.string.formatter_chronometer_delay), chronometerMinutes, chronometerSeconds));

                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));
                    bStartStop.setText(getString(R.string.add_activity_start));

                    insertedRows = 0;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (bStartStop.getText().equals(getString(R.string.add_activity_stop))) {
                    bStartStop.callOnClick();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (bStartStop.getText().equals(getString(R.string.add_activity_stop))) {
                    bStartStop.callOnClick();
                }
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

        ContentValues values = new ContentValues();

        values.put(RawsContract.TIMESTAMP, dateFormatter.format(new Date()));
        values.put(RawsContract.ACTIVITY_ID, activityId);
        values.put(RawsContract.USER_ID, userId);
        values.put(RawsContract.GRAVITY_X, gravity.get(0));
        values.put(RawsContract.GRAVITY_Y, gravity.get(1));
        values.put(RawsContract.GRAVITY_Z, gravity.get(2));
        values.put(RawsContract.ACCELERATION_X, acceleration.get(0));
        values.put(RawsContract.ACCELERATION_Y, acceleration.get(1));
        values.put(RawsContract.ACCELERATION_Z, acceleration.get(2));

        database.insert(RawsContract.TABLE_NAME, null, values);

        insertedRows++;
    }

    public void onRadioButtonClicked(View view) {
        Boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rb_activity_lie:
                if (checked) {
                    activityId = Constants.ACTIVITY_LIE_ID;

                    ivActivity.setImageResource(R.drawable.activity_lie);
                }

                break;
            case R.id.rb_activity_sit:
                if (checked) {
                    activityId = Constants.ACTIVITY_SIT_ID;

                    ivActivity.setImageResource(R.drawable.activity_sit);
                }

                break;
            case R.id.rb_activity_stand:
                if (checked) {
                    activityId = Constants.ACTIVITY_STAND_ID;

                    ivActivity.setImageResource(R.drawable.activity_stand);
                }

                break;
            case R.id.rb_activity_walk:
                if (checked) {
                    activityId = Constants.ACTIVITY_WALK_ID;

                    ivActivity.setImageResource(R.drawable.activity_walk);
                }

                break;
            case R.id.rb_activity_run:
                if (checked) {
                    activityId = Constants.ACTIVITY_RUN_ID;

                    ivActivity.setImageResource(R.drawable.activity_run);
                }

                break;
            case R.id.rb_activity_cycling:
                if (checked) {
                    activityId = Constants.ACTIVITY_CYCLING_ID;

                    ivActivity.setImageResource(R.drawable.activity_cycling);
                }

                break;
            default:
                break;
        }
    }
}
