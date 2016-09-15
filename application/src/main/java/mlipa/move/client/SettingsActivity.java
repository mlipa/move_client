package mlipa.move.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private Context context;
    private SettingsFragment settingsFragment;

    public static Boolean addActivityFlag = false;
    public static Boolean windowLengthChanged;
    public static Boolean artificialNeuralNetworkChanged;

    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        context = getApplicationContext();
        settingsFragment = new SettingsFragment();

        windowLengthChanged = false;
        artificialNeuralNetworkChanged = false;

        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (addActivityFlag) {
            fab.show();
        } else {
            fab.hide();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);

                dialog.setTitle(getString(R.string.title_learning));
                dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (addActivityFlag || windowLengthChanged) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setMessage(getString(R.string.message_time_windows));
                                }
                            });

                            SplashActivity.createTimeWindows();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setMessage(getString(R.string.message_features));
                                }
                            });

                            SplashActivity.createFeatures();
                        }

                        if (addActivityFlag || windowLengthChanged || artificialNeuralNetworkChanged) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.setMessage(getString(R.string.message_learn));
                                }
                            });

                            SplashActivity.learnArtificialNeuralNetwork();
                        }

                        if (addActivityFlag) {
                            addActivityFlag = false;
                        }
                        if (windowLengthChanged) {
                            windowLengthChanged = false;
                        }
                        if (artificialNeuralNetworkChanged) {
                            artificialNeuralNetworkChanged = false;
                        }

                        dialog.dismiss();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, getString(R.string.toast_learn), Toast.LENGTH_LONG).show();
                            }
                        });

                        fab.hide();
                    }
                }).start();
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.layout, settingsFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (windowLengthChanged || artificialNeuralNetworkChanged) {
                    showReLearnDialog();

                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (windowLengthChanged || artificialNeuralNetworkChanged) {
                    showReLearnDialog();
                }
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void showReLearnDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

        builder.setTitle(R.string.title_re_learn);
        builder.setMessage(R.string.message_re_learn);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.setCancelable(true);
        builder.create().show();
    }
}
