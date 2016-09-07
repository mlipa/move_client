package mlipa.move.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.toString();

    private static final Integer INPUT_NEURONS = 3;
    private static final Integer HIDDEN_LAYERS = 1;
    private static final Integer HIDDEN_NEURONS = 3;
    private static final Integer OUTPUT_NEURON = 4;
    private static final Double DESIRED_ACTIVITY_WEIGHT = 1.0;
    private static final Double UNDESIRED_ACTIVITY_WEIGHT = 0.0;
    private static final Integer TIME_WINDOW_LENGTH = 2560;
    private static final Integer LEARNING_ITERATIONS = 10000;
    private static final Integer TEST_ITERATIONS = 1000;

    private Context context;

    public static DatabaseHandler databaseHandler;
    private static SQLiteDatabase database;

    private static Calendar calendar;
    private static SimpleDateFormat dateFormat;
    private static Date timestampStart;
    private static Date timestampStop;
    private static ArrayList<Date> timestampStartArray;
    private static ArrayList<Date> timestampStopArray;
    private static ArrayList<Integer> activitiesId;
    private static ArrayList<ArrayList<Integer>> activitiesIdArray;
    private static Integer activitiesIdArrayCounter;

    private static Random random;

    public static NeuralNetwork neuralNetwork;
    public static Boolean neuralNetworkActive;

    private Intent logInIntent;

    private TextView tv_message;

    private class SplashTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            databaseHandler = new DatabaseHandler(context);
            database = databaseHandler.getWritableDatabase();

            calendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
            timestampStart = new Date();
            timestampStop = new Date();
            timestampStartArray = new ArrayList<>();
            timestampStopArray = new ArrayList<>();
            activitiesId = new ArrayList<>();
            activitiesIdArray = new ArrayList<>();
            activitiesIdArrayCounter = 0;

            random = new Random();

            String[] taProjection = {
                    RawContract.Raws.TIMESTAMP,
                    RawContract.Raws.ACTIVITY_ID
            };

            Cursor taCursor = database.query(
                    RawContract.Raws.TABLE_NAME,
                    taProjection,
                    null, null, null, null, null
            );

            if (taCursor.getCount() > 0) {
                neuralNetwork = new NeuralNetwork(INPUT_NEURONS, HIDDEN_LAYERS, HIDDEN_NEURONS, OUTPUT_NEURON);
                neuralNetworkActive = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_message.setText(getString(R.string.time_windows_message));
                    }
                });

                createTimeWindows();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_message.setText(getString(R.string.features_message));
                    }
                });

                calculateFeatures();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_message.setText(getString(R.string.learning_message));
                    }
                });

                learnNeuralNetwork();
            } else {
                neuralNetworkActive = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            logInIntent = new Intent(context, LogInActivity.class);

            startActivity(logInIntent);

            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        context = getApplicationContext();

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        tv_message = (TextView) findViewById(R.id.tv_message);

        tv_message.setText(getString(R.string.database_message));

        new SplashTask().execute();
    }

    public static void createTimeWindows() {
        try {
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
            activitiesIdArray.add(new ArrayList<Integer>());
            activitiesIdArrayCounter = 0;
            activitiesIdArray.get(activitiesIdArrayCounter).add(taCursor.getInt(taCursor.getColumnIndex(RawContract.Raws.ACTIVITY_ID)));

            taCursor.moveToNext();

            Integer taCount = taCursor.getCount();

            for (int i = 1; i < taCount; i++) {
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
    }

    public static void calculateFeatures() {
        String deleteFeaturesRows = "DELETE FROM " + FeaturesContract.Features.TABLE_NAME + ";";

        database.execSQL(deleteFeaturesRows);

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

                xyzCursor.moveToFirst();

                Integer xyzCount = xyzCursor.getCount();

                for (int i = 0; i < xyzCount; i++) {
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
                    mean.add(sum.get(i) / xyzCount);
                    energy.add(squareSum.get(i) / xyzCount);
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

                if (xyzCount % 2 == 0) {
                    medianGravityX = (sortGravityX[xyzCount / 2 - 1] + sortGravityX[xyzCount / 2]) / 2;
                    medianGravityY = (sortGravityY[xyzCount / 2 - 1] + sortGravityY[xyzCount / 2]) / 2;
                    medianGravityZ = (sortGravityZ[xyzCount / 2 - 1] + sortGravityZ[xyzCount / 2]) / 2;
                    medianAccelerationX = (sortAccelerationX[xyzCount / 2 - 1] + sortAccelerationX[xyzCount / 2]) / 2;
                    medianAccelerationY = (sortAccelerationY[xyzCount / 2 - 1] + sortAccelerationY[xyzCount / 2]) / 2;
                    medianAccelerationZ = (sortAccelerationZ[xyzCount / 2 - 1] + sortAccelerationZ[xyzCount / 2]) / 2;

                } else {
                    medianGravityX = sortGravityX[(xyzCount - 1) / 2];
                    medianGravityY = sortGravityY[(xyzCount - 1) / 2];
                    medianGravityZ = sortGravityZ[(xyzCount - 1) / 2];
                    medianAccelerationX = sortAccelerationX[(xyzCount - 1) / 2];
                    medianAccelerationY = sortAccelerationY[(xyzCount - 1) / 2];
                    medianAccelerationZ = sortAccelerationZ[(xyzCount - 1) / 2];
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

                for (int i = 0; i < xyzCount; i++) {
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
                    standardDeviation.add(Math.sqrt(deviationSum.get(i) / xyzCount));
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

                if (xyzCount % 2 == 0) {
                    absoluteMedianGravityX = (sortAbsoluteMedianGravityX[xyzCount / 2 - 1] + sortAbsoluteMedianGravityX[xyzCount / 2]) / 2;
                    absoluteMedianGravityY = (sortAbsoluteMedianGravityY[xyzCount / 2 - 1] + sortAbsoluteMedianGravityY[xyzCount / 2]) / 2;
                    absoluteMedianGravityZ = (sortAbsoluteMedianGravityZ[xyzCount / 2 - 1] + sortAbsoluteMedianGravityZ[xyzCount / 2]) / 2;
                    absoluteMedianAccelerationX = (sortAbsoluteMedianAccelerationX[xyzCount / 2 - 1] + sortAbsoluteMedianAccelerationX[xyzCount / 2]) / 2;
                    absoluteMedianAccelerationY = (sortAbsoluteMedianAccelerationY[xyzCount / 2 - 1] + sortAbsoluteMedianAccelerationY[xyzCount / 2]) / 2;
                    absoluteMedianAccelerationZ = (sortAbsoluteMedianAccelerationZ[xyzCount / 2 - 1] + sortAbsoluteMedianAccelerationZ[xyzCount / 2]) / 2;
                } else {
                    absoluteMedianGravityX = sortAbsoluteMedianGravityX[(xyzCount - 1) / 2];
                    absoluteMedianGravityY = sortAbsoluteMedianGravityY[(xyzCount - 1) / 2];
                    absoluteMedianGravityZ = sortAbsoluteMedianGravityZ[(xyzCount - 1) / 2];
                    absoluteMedianAccelerationX = sortAbsoluteMedianAccelerationX[(xyzCount - 1) / 2];
                    absoluteMedianAccelerationY = sortAbsoluteMedianAccelerationY[(xyzCount - 1) / 2];
                    absoluteMedianAccelerationZ = sortAbsoluteMedianAccelerationZ[(xyzCount - 1) / 2];
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
    }

    public static void learnNeuralNetwork() {
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

        neuralNetwork = new NeuralNetwork(INPUT_NEURONS, HIDDEN_LAYERS, HIDDEN_NEURONS, OUTPUT_NEURON);

        Integer fCount = fCursor.getCount();

        ArrayList<ArrayList<Double>> activities = new ArrayList<>(fCount);
        ArrayList<ArrayList<Double>> features = new ArrayList<>(fCount);

        fCursor.moveToFirst();

        for (int i = 0; i < fCount; i++) {
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
            Integer r = random.nextInt(fCount);

            neuralNetwork.trainNetwork(features.get(r), activities.get(r));
        }

        Integer correct = 0;
        Integer incorrect = 0;
        ArrayList<ArrayList<Integer>> predictedActivitiesTable = new ArrayList<>(OUTPUT_NEURON);

        for (int i = 0; i < OUTPUT_NEURON; i++) {
            predictedActivitiesTable.add(new ArrayList<Integer>(OUTPUT_NEURON));

            for (int j = 0; j < OUTPUT_NEURON; j++) {
                predictedActivitiesTable.get(i).add(0);
            }
        }

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            Integer r = random.nextInt(fCount);

            ArrayList<Double> output = neuralNetwork.runNetwork(features.get(r));

            Integer desiredActivity = 0;
            Integer predictedActivity = 0;

            for (int j = 0; j < OUTPUT_NEURON; j++) {
                if (activities.get(r).get(j) > activities.get(r).get(desiredActivity)) {
                    desiredActivity = j;
                }

                if (output.get(j) > output.get(predictedActivity)) {
                    predictedActivity = j;
                }
            }

            if (desiredActivity == predictedActivity) {
                correct++;
            } else {
                incorrect++;
            }

            predictedActivitiesTable.get(predictedActivity).set(desiredActivity, predictedActivitiesTable.get(predictedActivity).get(desiredActivity) + 1);
        }

        Log.v(TAG, "Accuracy: " + String.valueOf((correct * 100) / (correct + incorrect)) + "%");
        Log.v(TAG, "Activities: a - lie, b - sit, c - stand, d - walk");
        Log.v(TAG, "-------------------------");
        Log.v(TAG, "|  a  |  b  |  c  |  d  |");
        Log.v(TAG, "|-----+-----+-----+-----| classify as:");

        for (int j = 0; j < OUTPUT_NEURON; j++) {
            String activityLabels = "abcd";
            StringBuilder builder = new StringBuilder();

            builder.append("| ");

            for (int k = 0; k < OUTPUT_NEURON; k++) {
                Integer result = predictedActivitiesTable.get(j).get(k);

                if (result / 100 > 0) {
                    builder.append(result + " | ");
                } else if (result / 10 > 0) {
                    builder.append(" " + result + " | ");
                } else {
                    builder.append("  " + result + " | ");
                }
            }

            builder.append(activityLabels.charAt(j));

            Log.v(TAG, builder.toString());
        }
        Log.v(TAG, "-------------------------");
    }
}
