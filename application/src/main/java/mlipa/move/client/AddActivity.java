package mlipa.move.client;

import android.content.ContentValues;
import android.content.Context;
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
    private static final String TAG = AddActivity.class.toString();

    private static final String CLIENT_USER_ID_KEY = "userId";

    private Context context;
    private MediaPlayer player;

    private SQLiteDatabase database;
    private Integer insertedRows;

    private SimpleDateFormat dateFormat;
    private Integer activityId;
    private Integer userId;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Boolean accelerometerActive;

    private CountDownTimer delay;
    private CountDownTimer chronometer;

    private ImageView ivActivity;
    private RadioGroup rgActivity;
    private RadioButton rbActivityLie;
    private TextView tvChronometer;
    private Button bStartStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        context = getApplicationContext();
        player = MediaPlayer.create(context, R.raw.notify);

        database = SplashActivity.databaseHandler.getWritableDatabase();
        insertedRows = 0;

        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        activityId = 5;
        userId = Cookie.preferences.getInt(CLIENT_USER_ID_KEY, -1);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerActive = false;

        chronometer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Long minutes = (millisUntilFinished / 1000) / 60;
                Long seconds = (millisUntilFinished / 1000) % 60;

                tvChronometer.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }

            @Override
            public void onFinish() {
                for (int i = 0; i < rgActivity.getChildCount(); i++) {
                    rgActivity.getChildAt(i).setEnabled(true);
                }

                tvChronometer.setText(getString(R.string.chronometer_base_time));

                bStartStop.setText(getString(R.string.start));
                bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));

                player.start();

                sensorManager.unregisterListener(AddActivity.this, accelerometer);
                accelerometerActive = false;

                Log.v(TAG, "[chronometer.onFinish()] Accelerometer unregistered successfully!");
                Log.v(TAG, String.valueOf(insertedRows) + " row(s) inserted successfully!");

                insertedRows = 0;
            }
        };

        delay = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Long minutes = (millisUntilFinished / 1000) / 60;
                Long seconds = (millisUntilFinished / 1000) % 60;

                tvChronometer.setTextColor(getColor(R.color.bootstrap_red));
                tvChronometer.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }

            @Override
            public void onFinish() {
                tvChronometer.setTextColor(getColor(R.color.black));
                tvChronometer.setText(getString(R.string.chronometer_base_time));

                player.start();

                chronometer.start();

                sensorManager.registerListener(AddActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                accelerometerActive = true;

                Log.v(TAG, "[delay.onFinish()] Accelerometer registered successfully!");
            }
        };

        ivActivity = (ImageView) findViewById(R.id.iv_activity);
        rgActivity = (RadioGroup) findViewById(R.id.rg_activity);
        rbActivityLie = (RadioButton) findViewById(R.id.rb_activity_lie);
        tvChronometer = (TextView) findViewById(R.id.tv_chronometer);
        bStartStop = (Button) findViewById(R.id.b_start_stop);

        rbActivityLie.toggle();
        onRadioButtonClicked(rbActivityLie);

        bStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bStartStop.getText().equals(getString(R.string.start))) {
                    delay.start();

                    for (int i = 0; i < rgActivity.getChildCount(); i++) {
                        rgActivity.getChildAt(i).setEnabled(false);
                    }

                    bStartStop.setText(getString(R.string.stop));
                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_red));
                } else {
                    delay.cancel();
                    chronometer.cancel();

                    for (int i = 0; i < rgActivity.getChildCount(); i++) {
                        rgActivity.getChildAt(i).setEnabled(true);
                    }

                    tvChronometer.setTextColor(getColor(R.color.black));
                    tvChronometer.setText(getString(R.string.chronometer_base_time));

                    bStartStop.setText(getString(R.string.start));
                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));

                    sensorManager.unregisterListener(AddActivity.this, accelerometer);
                    accelerometerActive = false;

                    Log.v(TAG, "[bStartStop.onClick('Stop')] Accelerometer unregistered successfully!");
                    Log.v(TAG, String.valueOf(insertedRows) + " row(s) inserted successfully!");

                    insertedRows = 0;
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        Boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rb_activity_lie:
                if (checked) {
                    activityId = 1;

                    ivActivity.setImageResource(R.drawable.activity_lie);
                }

                break;
            case R.id.rb_activity_sit:
                if (checked) {
                    activityId = 2;

                    ivActivity.setImageResource(R.drawable.activity_sit);
                }

                break;
            case R.id.rb_activity_stand:
                if (checked) {
                    activityId = 3;

                    ivActivity.setImageResource(R.drawable.activity_stand);
                }

                break;
            case R.id.rb_activity_walk:
                if (checked) {
                    activityId = 4;

                    ivActivity.setImageResource(R.drawable.activity_walk);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (accelerometerActive) {
            sensorManager.unregisterListener(AddActivity.this, accelerometer);
            accelerometerActive = false;

            Log.v(TAG, "[onPause()] Accelerometer unregistered successfully!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (accelerometerActive) {
            sensorManager.unregisterListener(AddActivity.this, accelerometer);
            accelerometerActive = false;

            Log.v(TAG, "[onStop()] Accelerometer unregistered successfully!");
            Log.v(TAG, String.valueOf(insertedRows) + " row(s) inserted successfully!");
        }
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

        ContentValues values = new ContentValues();

        values.put(RawContract.Raws.TIMESTAMP, dateFormat.format(new Date()));
        values.put(RawContract.Raws.ACTIVITY_ID, activityId);
        values.put(RawContract.Raws.USER_ID, userId);
        values.put(RawContract.Raws.GRAVITY_X, gravity.get(0));
        values.put(RawContract.Raws.GRAVITY_Y, gravity.get(1));
        values.put(RawContract.Raws.GRAVITY_Z, gravity.get(2));
        values.put(RawContract.Raws.ACCELERATION_X, acceleration.get(0));
        values.put(RawContract.Raws.ACCELERATION_Y, acceleration.get(1));
        values.put(RawContract.Raws.ACCELERATION_Z, acceleration.get(2));

        database.insert(RawContract.Raws.TABLE_NAME, null, values);

        insertedRows++;
    }
}
