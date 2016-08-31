package mlipa.move.client;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AddActivity extends AppCompatActivity {
    private Context context;
    private MediaPlayer player;

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

                // TODO: STOP RECORD ACTIVITY
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

                    // TODO: START RECORD ACTIVITY
                } else {
                    chronometer.cancel();

                    for (int i = 0; i < rgActivity.getChildCount(); i++) {
                        rgActivity.getChildAt(i).setEnabled(true);
                    }

                    tvChronometer.setText(getString(R.string.chronometer_origin));

                    bStartStop.setText(getString(R.string.start));
                    bStartStop.setBackgroundColor(getColor(R.color.bootstrap_blue));

                    // TODO: STOP RECORD ACTIVITY
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
}
