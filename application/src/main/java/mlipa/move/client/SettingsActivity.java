package mlipa.move.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SettingsActivity extends AppCompatActivity {
    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";

    private Context context;
    private Intent intent;
    public static RequestQueue queue;

    private SettingsFragment settingsFragment;
    private Bundle args;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        context = getApplicationContext();
        intent = getIntent();
        queue = Volley.newRequestQueue(context);

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        settingsFragment = new SettingsFragment();

        args = new Bundle();

        args.putString(CLIENT_CLASSIFIER_ID_KEY, intent.getStringExtra(CLIENT_CLASSIFIER_ID_KEY));

        settingsFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(R.id.layout, settingsFragment).commit();

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.bootstrap_green)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);

                dialog.setTitle(getString(R.string.learning));
                dialog.setMessage(getString(R.string.learning_message));
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setProgress(0);
                // dialog.setMax();
                dialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: TRAIN AND RE-TRAIN ALGORITHM
                    }
                }).start();
            }
        });
    }
}
