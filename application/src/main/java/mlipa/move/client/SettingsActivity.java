package mlipa.move.client;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.toString();

    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";

    private static final Integer INPUT_NEURONS = 3;
    private static final Integer HIDDEN_LAYERS = 1;
    private static final Integer HIDDEN_NEURONS = 3;
    private static final Integer OUTPUT_NEURON = 4;
    private static final Double DESIRED_ACTIVITY_WEIGHT = 0.65;
    private static final Double UNDESIRED_ACTIVITY_WEIGHT = 0.0;
    private static final Integer TIME_WINDOW_LENGTH = 2560;
    private static final Integer LEARNING_ITERATIONS = 10000;

    private Context context;

    private SQLiteDatabase database;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Date timestampStart;
    private Date timestampStop;
    private ArrayList<Date> timestampStartArray;
    private ArrayList<Date> timestampStopArray;
    private ArrayList<ArrayList<Integer>> activitiesIdArray;
    private Integer activitiesIdArrayCounter;
    private ArrayList<Integer> activitiesId;

    private Random random;

    private SettingsFragment settingsFragment;
    private Bundle args;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        context = getApplicationContext();

        database = LogInActivity.databaseHandler.getWritableDatabase();

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        timestampStart = new Date();
        timestampStop = new Date();
        timestampStartArray = new ArrayList<>();
        timestampStopArray = new ArrayList<>();
        activitiesIdArray = new ArrayList<>();
        activitiesIdArrayCounter = 0;
        activitiesId = new ArrayList<>();

        random = new Random();

        settingsFragment = new SettingsFragment();

        args = new Bundle();

        args.putString(CLIENT_CLASSIFIER_ID_KEY, "1");

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
                            String deleteFeaturesRows = "DELETE FROM " + FeaturesContract.Features.TABLE_NAME + ";";

                            database.execSQL(deleteFeaturesRows);

                            timestampStartArray.clear();
                            timestampStopArray.clear();
                            activitiesIdArray.clear();

                            String[] taProjection = {
                                    RawContract.Raws.TIMESTAMP,
                                    RawContract.Raws.ACTIVITY_ID
                            };

                            Cursor taCursor = database.query(
                                    RawContract.Raws.TABLE_NAME,
                                    taProjection,
                                    null, null, null, null, null
                            );

                            taCursor.moveToFirst();

                            timestampStart = dateFormat.parse(taCursor.getString(taCursor.getColumnIndex(RawContract.Raws.TIMESTAMP)));
                            calendar.setTime(timestampStart);
                            calendar.add(Calendar.MILLISECOND, TIME_WINDOW_LENGTH);
                            timestampStop = calendar.getTime();

                            timestampStartArray.add(timestampStart);
                            timestampStopArray.add(timestampStop);
                            activitiesIdArrayCounter = 0;
                            activitiesIdArray.add(new ArrayList<Integer>());
                            activitiesIdArray.get(activitiesIdArrayCounter).add(taCursor.getInt(taCursor.getColumnIndex(RawContract.Raws.ACTIVITY_ID)));

                            taCursor.moveToNext();

                            Integer count = taCursor.getCount();

                            for (int i = 1; i < count; i++) {
                                Date timestamp = dateFormat.parse(taCursor.getString(taCursor.getColumnIndex(RawContract.Raws.TIMESTAMP)));
                                Integer activityId = taCursor.getInt(taCursor.getColumnIndex(RawContract.Raws.ACTIVITY_ID));

                                if (timestamp.compareTo(timestampStop) == 1) {
                                    timestampStart = timestamp;
                                    calendar.setTime(timestampStart);
                                    calendar.add(Calendar.MILLISECOND, TIME_WINDOW_LENGTH);
                                    timestampStop = calendar.getTime();

                                    timestampStartArray.add(timestampStart);
                                    timestampStopArray.add(timestampStop);

                                    activitiesIdArray.add(new ArrayList<Integer>());
                                    activitiesIdArrayCounter++;
                                    activitiesIdArray.get(activitiesIdArrayCounter).add(activityId);
                                }

                                if (!activitiesIdArray.get(activitiesIdArrayCounter).contains(activityId)) {
                                    activitiesIdArray.get(activitiesIdArrayCounter).add(activityId);
                                }

                                taCursor.moveToNext();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Log.v(TAG, String.valueOf(timestampStartArray.size()) + " time frame(s) created successfully!");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage(getString(R.string.features_message));
                            }
                        });

                        String[] xyzProjection = {
                                RawContract.Raws.GRAVITY_X,
                                RawContract.Raws.GRAVITY_Y,
                                RawContract.Raws.GRAVITY_Z,
                                RawContract.Raws.ACCELERATION_X,
                                RawContract.Raws.ACCELERATION_Y,
                                RawContract.Raws.ACCELERATION_Z
                        };
                        String xyzSelection = RawContract.Raws.TIMESTAMP + " >= ? AND " + RawContract.Raws.TIMESTAMP + " <= ? AND " + RawContract.Raws.ACTIVITY_ID + " = ?";

                        while (timestampStartArray.size() > 0) {
                            timestampStart = timestampStartArray.get(0);
                            timestampStartArray.remove(0);

                            timestampStop = timestampStopArray.get(0);
                            timestampStopArray.remove(0);

                            activitiesId = activitiesIdArray.get(0);
                            activitiesIdArray.remove(0);

                            while (activitiesId.size() > 0) {
                                Integer activityId = activitiesId.get(0);
                                activitiesId.remove(0);

                                String[] xyzSelectionArgs = {
                                        dateFormat.format(timestampStart),
                                        dateFormat.format(timestampStop),
                                        String.valueOf(activityId)
                                };

                                Cursor xyzCursor = database.query(
                                        RawContract.Raws.TABLE_NAME,
                                        xyzProjection,
                                        xyzSelection,
                                        xyzSelectionArgs,
                                        null, null, null
                                );

                                xyzCursor.moveToFirst();

                                Integer count = xyzCursor.getCount();

                                ArrayList<Double> min = new ArrayList<>(6);
                                ArrayList<Double> max = new ArrayList<>(6);
                                ArrayList<Double> sum = new ArrayList<>(6);
                                ArrayList<Double> squareSum = new ArrayList<>(6);
                                ArrayList<Double> gravityXArray = new ArrayList<>();
                                ArrayList<Double> gravityYArray = new ArrayList<>();
                                ArrayList<Double> gravityZArray = new ArrayList<>();
                                ArrayList<Double> accelerationXArray = new ArrayList<>();
                                ArrayList<Double> accelerationYArray = new ArrayList<>();
                                ArrayList<Double> accelerationZArray = new ArrayList<>();


                                for (int i = 0; i < 6; i++) {
                                    min.add(100.0);
                                    max.add(-100.0);
                                    sum.add(0.0);
                                    squareSum.add(0.0);
                                }

                                for (int i = 0; i < count; i++) {
                                    Double gravityX = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.GRAVITY_X));
                                    Double gravityY = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.GRAVITY_Y));
                                    Double gravityZ = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.GRAVITY_Z));
                                    Double accelerationX = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.ACCELERATION_X));
                                    Double accelerationY = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.ACCELERATION_Y));
                                    Double accelerationZ = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.ACCELERATION_Z));

                                    if (gravityX < min.get(0)) {
                                        min.set(0, gravityX);
                                    }
                                    if (gravityY < min.get(1)) {
                                        min.set(1, gravityY);
                                    }
                                    if (gravityZ < min.get(2)) {
                                        min.set(2, gravityZ);
                                    }
                                    if (accelerationX < min.get(3)) {
                                        min.set(3, accelerationX);
                                    }
                                    if (accelerationY < min.get(4)) {
                                        min.set(4, accelerationY);
                                    }
                                    if (accelerationZ < min.get(5)) {
                                        min.set(5, accelerationZ);
                                    }

                                    if (gravityX > max.get(0)) {
                                        max.set(0, gravityX);
                                    }
                                    if (gravityY > max.get(1)) {
                                        max.set(1, gravityY);
                                    }
                                    if (gravityZ > max.get(2)) {
                                        max.set(2, gravityZ);
                                    }
                                    if (accelerationX > max.get(3)) {
                                        max.set(3, accelerationX);
                                    }
                                    if (accelerationY > max.get(4)) {
                                        max.set(4, accelerationY);
                                    }
                                    if (accelerationZ > min.get(5)) {
                                        max.set(5, accelerationZ);
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

                                    gravityXArray.add(gravityX);
                                    gravityYArray.add(gravityY);
                                    gravityZArray.add(gravityZ);
                                    accelerationXArray.add(accelerationX);
                                    accelerationYArray.add(accelerationY);
                                    accelerationZArray.add(accelerationZ);

                                    xyzCursor.moveToNext();
                                }

                                ArrayList<Double> mean = new ArrayList<>(6);
                                ArrayList<Double> energy = new ArrayList<>(6);

                                for (int i = 0; i < 6; i++) {
                                    mean.add(sum.get(i) / count);
                                    energy.add(squareSum.get(i) / count);
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

                                if (count % 2 == 0) {
                                    medianGravityX = (sortGravityX[count / 2 - 1] + sortGravityX[count / 2]) / 2;
                                    medianGravityY = (sortGravityY[count / 2 - 1] + sortGravityY[count / 2]) / 2;
                                    medianGravityZ = (sortGravityZ[count / 2 - 1] + sortGravityZ[count / 2]) / 2;
                                    medianAccelerationX = (sortAccelerationX[count / 2 - 1] + sortAccelerationX[count / 2]) / 2;
                                    medianAccelerationY = (sortAccelerationY[count / 2 - 1] + sortAccelerationY[count / 2]) / 2;
                                    medianAccelerationZ = (sortAccelerationZ[count / 2 - 1] + sortAccelerationZ[count / 2]) / 2;

                                } else {
                                    medianGravityX = sortGravityX[(count - 1) / 2];
                                    medianGravityY = sortGravityY[(count - 1) / 2];
                                    medianGravityZ = sortGravityZ[(count - 1) / 2];
                                    medianAccelerationX = sortAccelerationX[(count - 1) / 2];
                                    medianAccelerationY = sortAccelerationY[(count - 1) / 2];
                                    medianAccelerationZ = sortAccelerationZ[(count - 1) / 2];
                                }

                                xyzCursor.moveToFirst();

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

                                for (int i = 0; i < count; i++) {
                                    Double gravityX = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.GRAVITY_X));
                                    Double gravityY = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.GRAVITY_Y));
                                    Double gravityZ = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.GRAVITY_Z));
                                    Double accelerationX = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.ACCELERATION_X));
                                    Double accelerationY = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.ACCELERATION_Y));
                                    Double accelerationZ = xyzCursor.getDouble(xyzCursor.getColumnIndex(RawContract.Raws.ACCELERATION_Z));

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

                                    xyzCursor.moveToNext();
                                }

                                ArrayList<Double> standardDeviation = new ArrayList<>(6);

                                for (int i = 0; i < 6; i++) {
                                    standardDeviation.add(Math.sqrt(deviationSum.get(i) / count));
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

                                if (count % 2 == 0) {
                                    absoluteMedianGravityX = (sortAbsoluteMedianGravityX[count / 2 - 1] + sortAbsoluteMedianGravityX[count / 2]) / 2;
                                    absoluteMedianGravityY = (sortAbsoluteMedianGravityY[count / 2 - 1] + sortAbsoluteMedianGravityY[count / 2]) / 2;
                                    absoluteMedianGravityZ = (sortAbsoluteMedianGravityZ[count / 2 - 1] + sortAbsoluteMedianGravityZ[count / 2]) / 2;
                                    absoluteMedianAccelerationX = (sortAbsoluteMedianAccelerationX[count / 2 - 1] + sortAbsoluteMedianAccelerationX[count / 2]) / 2;
                                    absoluteMedianAccelerationY = (sortAbsoluteMedianAccelerationY[count / 2 - 1] + sortAbsoluteMedianAccelerationY[count / 2]) / 2;
                                    absoluteMedianAccelerationZ = (sortAbsoluteMedianAccelerationZ[count / 2 - 1] + sortAbsoluteMedianAccelerationZ[count / 2]) / 2;
                                } else {
                                    absoluteMedianGravityX = sortAbsoluteMedianGravityX[(count - 1) / 2];
                                    absoluteMedianGravityY = sortAbsoluteMedianGravityY[(count - 1) / 2];
                                    absoluteMedianGravityZ = sortAbsoluteMedianGravityZ[(count - 1) / 2];
                                    absoluteMedianAccelerationX = sortAbsoluteMedianAccelerationX[(count - 1) / 2];
                                    absoluteMedianAccelerationY = sortAbsoluteMedianAccelerationY[(count - 1) / 2];
                                    absoluteMedianAccelerationZ = sortAbsoluteMedianAccelerationZ[(count - 1) / 2];
                                }

                                ContentValues values = new ContentValues();

                                values.put(FeaturesContract.Features.TIMESTAMP_START, dateFormat.format(timestampStart));
                                values.put(FeaturesContract.Features.TIMESTAMP_STOP, dateFormat.format(timestampStop));
                                values.put(FeaturesContract.Features.ACTIVITY_ID, String.valueOf(activityId));
                                values.put(FeaturesContract.Features.GRAVITY_X_MIN, min.get(0));
                                values.put(FeaturesContract.Features.GRAVITY_Y_MIN, min.get(1));
                                values.put(FeaturesContract.Features.GRAVITY_Z_MIN, min.get(2));
                                values.put(FeaturesContract.Features.ACCELERATION_X_MIN, min.get(3));
                                values.put(FeaturesContract.Features.ACCELERATION_Y_MIN, min.get(4));
                                values.put(FeaturesContract.Features.ACCELERATION_Z_MIN, min.get(5));
                                values.put(FeaturesContract.Features.GRAVITY_X_MAX, max.get(0));
                                values.put(FeaturesContract.Features.GRAVITY_Y_MAX, max.get(1));
                                values.put(FeaturesContract.Features.GRAVITY_Z_MAX, max.get(2));
                                values.put(FeaturesContract.Features.ACCELERATION_X_MAX, max.get(3));
                                values.put(FeaturesContract.Features.ACCELERATION_Y_MAX, max.get(4));
                                values.put(FeaturesContract.Features.ACCELERATION_Z_MAX, max.get(5));
                                values.put(FeaturesContract.Features.GRAVITY_X_MEAN, mean.get(0));
                                values.put(FeaturesContract.Features.GRAVITY_Y_MEAN, mean.get(1));
                                values.put(FeaturesContract.Features.GRAVITY_Z_MEAN, mean.get(2));
                                values.put(FeaturesContract.Features.ACCELERATION_X_MEAN, mean.get(3));
                                values.put(FeaturesContract.Features.ACCELERATION_Y_MEAN, mean.get(4));
                                values.put(FeaturesContract.Features.ACCELERATION_Z_MEAN, mean.get(5));
                                values.put(FeaturesContract.Features.GRAVITY_X_STANDARD_DEVIATION, standardDeviation.get(0));
                                values.put(FeaturesContract.Features.GRAVITY_Y_STANDARD_DEVIATION, standardDeviation.get(1));
                                values.put(FeaturesContract.Features.GRAVITY_Z_STANDARD_DEVIATION, standardDeviation.get(2));
                                values.put(FeaturesContract.Features.ACCELERATION_X_STANDARD_DEVIATION, standardDeviation.get(3));
                                values.put(FeaturesContract.Features.ACCELERATION_Y_STANDARD_DEVIATION, standardDeviation.get(4));
                                values.put(FeaturesContract.Features.ACCELERATION_Z_STANDARD_DEVIATION, standardDeviation.get(5));
                                values.put(FeaturesContract.Features.GRAVITY_X_ABSOLUTE_MEDIAN, absoluteMedianGravityX);
                                values.put(FeaturesContract.Features.GRAVITY_Y_ABSOLUTE_MEDIAN, absoluteMedianGravityY);
                                values.put(FeaturesContract.Features.GRAVITY_Z_ABSOLUTE_MEDIAN, absoluteMedianGravityZ);
                                values.put(FeaturesContract.Features.ACCELERATION_X_ABSOLUTE_MEDIAN, absoluteMedianAccelerationX);
                                values.put(FeaturesContract.Features.ACCELERATION_Y_ABSOLUTE_MEDIAN, absoluteMedianAccelerationY);
                                values.put(FeaturesContract.Features.ACCELERATION_Z_ABSOLUTE_MEDIAN, absoluteMedianAccelerationZ);
                                values.put(FeaturesContract.Features.GRAVITY_X_ENERGY, energy.get(0));
                                values.put(FeaturesContract.Features.GRAVITY_Y_ENERGY, energy.get(1));
                                values.put(FeaturesContract.Features.GRAVITY_Z_ENERGY, energy.get(2));
                                values.put(FeaturesContract.Features.ACCELERATION_X_ENERGY, energy.get(3));
                                values.put(FeaturesContract.Features.ACCELERATION_Y_ENERGY, energy.get(4));
                                values.put(FeaturesContract.Features.ACCELERATION_Z_ENERGY, energy.get(5));

                                database.insert(FeaturesContract.Features.TABLE_NAME, null, values);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage(getString(R.string.learning_message));
                            }
                        });

                        String[] fProjection = {
                                FeaturesContract.Features.ACTIVITY_ID,
                                FeaturesContract.Features.GRAVITY_X_MIN,
                                FeaturesContract.Features.GRAVITY_Y_MIN,
                                FeaturesContract.Features.GRAVITY_Z_MIN,
                                FeaturesContract.Features.ACCELERATION_X_MIN,
                                FeaturesContract.Features.ACCELERATION_Y_MIN,
                                FeaturesContract.Features.ACCELERATION_Z_MIN,
                                FeaturesContract.Features.GRAVITY_X_MAX,
                                FeaturesContract.Features.GRAVITY_Y_MAX,
                                FeaturesContract.Features.GRAVITY_Z_MAX,
                                FeaturesContract.Features.ACCELERATION_X_MAX,
                                FeaturesContract.Features.ACCELERATION_Y_MAX,
                                FeaturesContract.Features.ACCELERATION_Z_MAX,
                                FeaturesContract.Features.GRAVITY_X_MEAN,
                                FeaturesContract.Features.GRAVITY_Y_MEAN,
                                FeaturesContract.Features.GRAVITY_Z_MEAN,
                                FeaturesContract.Features.ACCELERATION_X_MEAN,
                                FeaturesContract.Features.ACCELERATION_Y_MEAN,
                                FeaturesContract.Features.ACCELERATION_Z_MEAN,
                                FeaturesContract.Features.GRAVITY_X_STANDARD_DEVIATION,
                                FeaturesContract.Features.GRAVITY_Y_STANDARD_DEVIATION,
                                FeaturesContract.Features.GRAVITY_Z_STANDARD_DEVIATION,
                                FeaturesContract.Features.ACCELERATION_X_STANDARD_DEVIATION,
                                FeaturesContract.Features.ACCELERATION_Y_STANDARD_DEVIATION,
                                FeaturesContract.Features.ACCELERATION_Z_STANDARD_DEVIATION,
                                FeaturesContract.Features.GRAVITY_X_ABSOLUTE_MEDIAN,
                                FeaturesContract.Features.GRAVITY_Y_ABSOLUTE_MEDIAN,
                                FeaturesContract.Features.GRAVITY_Z_ABSOLUTE_MEDIAN,
                                FeaturesContract.Features.ACCELERATION_X_ABSOLUTE_MEDIAN,
                                FeaturesContract.Features.ACCELERATION_Y_ABSOLUTE_MEDIAN,
                                FeaturesContract.Features.ACCELERATION_Z_ABSOLUTE_MEDIAN,
                                FeaturesContract.Features.GRAVITY_X_ENERGY,
                                FeaturesContract.Features.GRAVITY_Y_ENERGY,
                                FeaturesContract.Features.GRAVITY_Z_ENERGY,
                                FeaturesContract.Features.ACCELERATION_X_ENERGY,
                                FeaturesContract.Features.ACCELERATION_Y_ENERGY,
                                FeaturesContract.Features.ACCELERATION_Z_ENERGY
                        };

                        Cursor fCursor = database.query(
                                FeaturesContract.Features.TABLE_NAME,
                                fProjection,
                                null, null, null, null, null
                        );

                        fCursor.moveToFirst();

                        Integer count = fCursor.getCount();

                        LogInActivity.neuralNetwork = new NeuralNetwork(INPUT_NEURONS, HIDDEN_LAYERS, HIDDEN_NEURONS, OUTPUT_NEURON);

                        ArrayList<ArrayList<Double>> activities = new ArrayList<>(count);
                        ArrayList<ArrayList<Double>> features = new ArrayList<>(count);

                        for (int i = 0; i < count; i++) {
                            activities.add(new ArrayList<Double>());

                            for (int j = 1; j <= OUTPUT_NEURON; j++) {
                                if (j == fCursor.getDouble(fCursor.getColumnIndex(FeaturesContract.Features.ACTIVITY_ID))) {
                                    activities.get(i).add(DESIRED_ACTIVITY_WEIGHT);
                                } else {
                                    activities.get(i).add(UNDESIRED_ACTIVITY_WEIGHT);
                                }
                            }

                            features.add(new ArrayList<Double>());

                            for (int j = 1; j <= INPUT_NEURONS; j++) {
                                features.get(i).add(fCursor.getDouble(j));
                            }

                            fCursor.moveToNext();
                        }

                        for (int i = 0; i < LEARNING_ITERATIONS; i++) {
                            Integer r = random.nextInt(count);

                            LogInActivity.neuralNetwork.trainNetwork(features.get(r), activities.get(r));
                        }

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
