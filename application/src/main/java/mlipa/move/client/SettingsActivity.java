package mlipa.move.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.toString();

    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";

    private static final Integer TIME_WINDOW = 2560;

    private Context context;
    private Intent intent;
    public static RequestQueue queue;

    private SQLiteDatabase database;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Date timestampStart;
    private Date timestampStop;
    private ArrayList<Date> timestampStartArray;
    private ArrayList<Date> timestampStopArray;
    private ArrayList<ArrayList<Integer>> activitiesIdArray;
    private Integer activitiesIdArrayCounter;

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

        database = LogInActivity.databaseHandler.getWritableDatabase();

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        timestampStart = new Date();
        timestampStop = new Date();
        timestampStartArray = new ArrayList<>();
        timestampStopArray = new ArrayList<>();
        activitiesIdArray = new ArrayList<>();
        activitiesIdArrayCounter = 0;

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
                final ProgressDialog dialog = new ProgressDialog(SettingsActivity.this);

                dialog.setTitle(getString(R.string.learning));
                dialog.setMessage(getString(R.string.time_windows_message));
                dialog.setProgress(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);
                dialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            timestampStartArray.clear();
                            timestampStopArray.clear();
                            activitiesIdArray.clear();

                            String[] taProjection = {
                                    RawContract.Raws.COLUMN_NAME_TIMESTAMP,
                                    RawContract.Raws.COLUMN_NAME_ACTIVITY_ID
                            };

                            Cursor taCursor = database.query(
                                    RawContract.Raws.TABLE_NAME,
                                    taProjection,
                                    null, null, null, null, null
                            );

                            taCursor.moveToFirst();

                            timestampStart = dateFormat.parse(taCursor.getString(taCursor.getColumnIndex(RawContract.Raws.COLUMN_NAME_TIMESTAMP)));
                            calendar.setTime(timestampStart);
                            calendar.add(Calendar.MILLISECOND, TIME_WINDOW);
                            timestampStop = calendar.getTime();

                            timestampStartArray.add(timestampStart);
                            timestampStopArray.add(timestampStop);
                            activitiesIdArray.add(new ArrayList<Integer>());
                            activitiesIdArray.get(activitiesIdArrayCounter).add(taCursor.getInt(taCursor.getColumnIndex(RawContract.Raws.COLUMN_NAME_ACTIVITY_ID)));

                            taCursor.moveToNext();

                            for (int i = 1; i < taCursor.getCount(); i++) {
                                Date timestamp = dateFormat.parse(taCursor.getString(taCursor.getColumnIndex(RawContract.Raws.COLUMN_NAME_TIMESTAMP)));
                                Integer activity = taCursor.getInt(taCursor.getColumnIndex(RawContract.Raws.COLUMN_NAME_ACTIVITY_ID));

                                if (timestamp.compareTo(timestampStop) == 1) {
                                    timestampStart = timestamp;
                                    calendar.setTime(timestampStart);
                                    calendar.add(Calendar.MILLISECOND, TIME_WINDOW);
                                    timestampStop = calendar.getTime();

                                    timestampStartArray.add(timestampStart);
                                    timestampStopArray.add(timestampStop);

                                    activitiesIdArray.add(new ArrayList<Integer>());
                                    activitiesIdArrayCounter++;
                                    activitiesIdArray.get(activitiesIdArrayCounter).add(activity);
                                }

                                if (!activitiesIdArray.get(activitiesIdArrayCounter).contains(activity)) {
                                    activitiesIdArray.get(activitiesIdArrayCounter).add(activity);
                                }

                                taCursor.moveToNext();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Log.v(TAG, String.valueOf(timestampStartArray.size()) + " time frame(s) created successfully!");

                        // TODO: CALCULATE FEATURES AND ADD TO DATABASE

                        dialog.dismiss();
                    }
                }).start();
            }
        });
    }
}
