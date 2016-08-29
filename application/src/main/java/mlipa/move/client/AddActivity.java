package mlipa.move.client;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddActivity extends AppCompatActivity {
    private CountDownTimer chronometer;

    private TextView tvChronometer;
    private Button bStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        tvChronometer = (TextView) findViewById(R.id.tv_chronometer);
        bStart = (Button) findViewById(R.id.b_start_stop);

        chronometer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                tvChronometer.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }

            @Override
            public void onFinish() {
                tvChronometer.setText(getString(R.string.chronometer_origin));

                bStart.setText(getString(R.string.start));
                bStart.setBackgroundColor(getColor(R.color.bootstrap_blue));
            }
        };

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bStart.getText().equals("Start")) {
                    chronometer.start();

                    bStart.setText(getString(R.string.stop));
                    bStart.setBackgroundColor(getColor(R.color.bootstrap_red));
                } else {
                    chronometer.cancel();

                    tvChronometer.setText(getString(R.string.chronometer_origin));

                    bStart.setText(getString(R.string.start));
                    bStart.setBackgroundColor(getColor(R.color.bootstrap_blue));
                }
            }
        });
    }
}
