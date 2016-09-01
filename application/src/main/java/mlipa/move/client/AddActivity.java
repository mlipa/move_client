package mlipa.move.client;

import android.content.Context;
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

public class AddActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = AddActivity.class.toString();

    private Context context;
    private MediaPlayer player;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean accelerometerActive;

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

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerActive = false;

        chronometer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                tvChronometer.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }

            @Override
            public void onFinish() {
                for (int i = 0; i < rgActivity.getChildCount(); i++) {
                    rgActivity.getChildAt(i).setEnabled(true);
                }

                tvChronometer.setText(getString(R.string.chronometer_origin));

                bStartStop.setText(getString(R.string.start));
                bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));

                player.start();

                sensorManager.unregisterListener(AddActivity.this, accelerometer);
                accelerometerActive = false;

                Log.v(TAG, "[onFinish()] Accelerometer unregistered successfully!");
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
                    chronometer.start();

                    for (int i = 0; i < rgActivity.getChildCount(); i++) {
                        rgActivity.getChildAt(i).setEnabled(false);
                    }

                    bStartStop.setText(getString(R.string.stop));
                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_red));

                    sensorManager.registerListener(AddActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                    accelerometerActive = true;

                    Log.v(TAG, "[onClick('Start')] Accelerometer registered successfully!");
                } else {
                    chronometer.cancel();

                    for (int i = 0; i < rgActivity.getChildCount(); i++) {
                        rgActivity.getChildAt(i).setEnabled(true);
                    }

                    tvChronometer.setText(getString(R.string.chronometer_origin));

                    bStartStop.setText(getString(R.string.start));
                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));

                    sensorManager.unregisterListener(AddActivity.this, accelerometer);
                    accelerometerActive = false;

                    Log.v(TAG, "[onClick('Stop')] Accelerometer unregistered successfully!");
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rb_activity_lie:
                if (checked) {
                    ivActivity.setImageResource(R.drawable.activity_lie);
                }

                break;
            case R.id.rb_activity_sit:
                if (checked) {
                    ivActivity.setImageResource(R.drawable.activity_sit);
                }

                break;
            case R.id.rb_activity_stand:
                if (checked) {
                    ivActivity.setImageResource(R.drawable.activity_stand);
                }

                break;
            case R.id.rb_activity_walk:
                if (checked) {
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
        }
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

        // TODO: WRITE DATA TO DATABASE
    }
}
