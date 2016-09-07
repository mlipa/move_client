package mlipa.move.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";

    private Context context;

    private SettingsFragment settingsFragment;
    private Bundle args;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        context = getApplicationContext();

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        settingsFragment = new SettingsFragment();

        args = new Bundle();

        args.putString(CLIENT_CLASSIFIER_ID_KEY, Cookie.preferences.getString(CLIENT_CLASSIFIER_ID_KEY, "1"));

        settingsFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(R.id.layout, settingsFragment).commit();

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.bootstrap_green)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);

                dialog.setTitle(getString(R.string.learning));
                dialog.setMessage(getString(R.string.time_windows_message));
                dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SplashActivity.createTimeWindows();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage(getString(R.string.features_message));
                            }
                        });

                        SplashActivity.calculateFeatures();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage(getString(R.string.learning_message));
                            }
                        });

                        SplashActivity.learnNeuralNetwork();

                        dialog.dismiss();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, getString(R.string.learning_toast), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
