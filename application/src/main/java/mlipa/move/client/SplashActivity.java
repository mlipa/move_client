package mlipa.move.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

    private static Context context;
    private static SharedPreferences preferences;
    public static DatabaseHandler databaseHandler;
    private static SQLiteDatabase database;

    public static ArtificialNeuralNetwork artificialNeuralNetwork;
    public static Boolean artificialNeuralNetworkActive;

    private static SimpleDateFormat dateFormatter;
    private static Calendar calendar;
    private static ArrayList<Date> timestampStartArray;
    private static ArrayList<Date> timestampStopArray;
    private static ArrayList<ArrayList<Integer>> activitiesIdArray;

    private Intent logInIntent;

    private TextView tv_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        context = getApplicationContext();
        preferences = getSharedPreferences(getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(context);
        database = databaseHandler.getWritableDatabase();

        artificialNeuralNetworkActive = false;

        dateFormatter = new SimpleDateFormat(getString(R.string.formatter_date));
        calendar = Calendar.getInstance();
        timestampStartArray = new ArrayList<>();
        timestampStopArray = new ArrayList<>();
        activitiesIdArray = new ArrayList<>();

        logInIntent = new Intent(context, LogInActivity.class);

        tv_message = (TextView) findViewById(R.id.tv_message);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] rawsProjection = {
                        RawsContract._ID
                };

                Cursor rawsCursor = database.query(
                        RawsContract.TABLE_NAME,
                        rawsProjection,
                        null, null, null, null, null
                );

                Integer rawsCount = rawsCursor.getCount();

                if (rawsCount > 0) {
                    String[] featuresProjection = {
                            FeaturesContract._ID
                    };

                    Cursor featuresCursor = database.query(
                            FeaturesContract.TABLE_NAME,
                            featuresProjection,
                            null, null, null, null, null
                    );

                    Integer featuresCount = featuresCursor.getCount();

                    switch (featuresCount) {
                        case 0:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_message.setText(getString(R.string.message_time_windows));
                                }
                            });

                            createTimeWindows();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_message.setText(getString(R.string.message_features));
                                }
                            });

                            createFeatures();
                        default:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_message.setText(getString(R.string.message_learn));
                                }
                            });

                            learnArtificialNeuralNetwork();
                    }
                }

                startActivity(logInIntent);

                finish();
            }
        }).start();
    }

    public static void createTimeWindows() {
        try {
            Double settingsPreferencesWindowLength = Double.longBitsToDouble(preferences.getLong(context.getString(R.string.shared_preferences_settings_window_length), Double.doubleToLongBits(Constants.DEFAULT_WINDOW_LENGTH))) * 1000.0;
            Integer windowLength = settingsPreferencesWindowLength.intValue();

            timestampStartArray.clear();
            timestampStopArray.clear();
            activitiesIdArray.clear();

            String[] rawsProjection = {
                    RawsContract.TIMESTAMP,
                    RawsContract.ACTIVITY_ID
            };

            Cursor rawsCursor = database.query(
                    RawsContract.TABLE_NAME,
                    rawsProjection,
                    null, null, null, null, null
            );

            rawsCursor.moveToFirst();

            Date timestampStart = dateFormatter.parse(rawsCursor.getString(rawsCursor.getColumnIndex(RawsContract.TIMESTAMP)));
            calendar.setTime(timestampStart);
            calendar.add(Calendar.MILLISECOND, windowLength);
            Date timestampStop = calendar.getTime();

            timestampStartArray.add(timestampStart);
            timestampStopArray.add(timestampStop);

            activitiesIdArray.add(new ArrayList<Integer>());
            Integer activitiesIdArrayCounter = 0;
            activitiesIdArray.get(activitiesIdArrayCounter).add(rawsCursor.getInt(rawsCursor.getColumnIndex(RawsContract.ACTIVITY_ID)));

            rawsCursor.moveToNext();

            for (int i = 1; i < rawsCursor.getCount(); i++) {
                Date timestamp = dateFormatter.parse(rawsCursor.getString(rawsCursor.getColumnIndex(RawsContract.TIMESTAMP)));
                Integer activityId = rawsCursor.getInt(rawsCursor.getColumnIndex(RawsContract.ACTIVITY_ID));

                if (timestamp.compareTo(timestampStop) == 1) {
                    timestampStart = timestamp;
                    calendar.setTime(timestampStart);
                    calendar.add(Calendar.MILLISECOND, windowLength);
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

                rawsCursor.moveToNext();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void createFeatures() {
        String deleteFeaturesRows = "DELETE FROM " + FeaturesContract.TABLE_NAME + ";";

        database.execSQL(deleteFeaturesRows);

        String[] rawsProjection = {
                RawsContract.GRAVITY_X,
                RawsContract.GRAVITY_Y,
                RawsContract.GRAVITY_Z,
                RawsContract.ACCELERATION_X,
                RawsContract.ACCELERATION_Y,
                RawsContract.ACCELERATION_Z
        };
        String rawsSelection = RawsContract.TIMESTAMP + " >= ? AND " + RawsContract.TIMESTAMP + " <= ? AND " + RawsContract.ACTIVITY_ID + " = ?";

        while (timestampStartArray.size() > 0) {
            Date timestampStart = timestampStartArray.get(0);
            timestampStartArray.remove(0);

            Date timestampStop = timestampStopArray.get(0);
            timestampStopArray.remove(0);

            ArrayList<Integer> activitiesId = activitiesIdArray.get(0);
            activitiesIdArray.remove(0);

            while (activitiesId.size() > 0) {
                Integer activityId = activitiesId.get(0);
                activitiesId.remove(0);

                String[] rawsSelectionArgs = {
                        dateFormatter.format(timestampStart),
                        dateFormatter.format(timestampStop),
                        String.valueOf(activityId)
                };

                Cursor rawsCursor = database.query(
                        RawsContract.TABLE_NAME,
                        rawsProjection,
                        rawsSelection,
                        rawsSelectionArgs,
                        null, null, null
                );

                ArrayList<Double> gravityXArray = new ArrayList<>();
                ArrayList<Double> gravityYArray = new ArrayList<>();
                ArrayList<Double> gravityZArray = new ArrayList<>();
                ArrayList<Double> accelerationXArray = new ArrayList<>();
                ArrayList<Double> accelerationYArray = new ArrayList<>();
                ArrayList<Double> accelerationZArray = new ArrayList<>();
                ArrayList<Double> minimum = new ArrayList<>(6);
                ArrayList<Double> maximum = new ArrayList<>(6);
                ArrayList<Double> sum = new ArrayList<>(6);
                ArrayList<Double> squareSum = new ArrayList<>(6);

                for (int i = 0; i < 6; i++) {
                    minimum.add(100.0);
                    maximum.add(-100.0);
                    sum.add(0.0);
                    squareSum.add(0.0);
                }

                rawsCursor.moveToFirst();

                Integer rawsCount = rawsCursor.getCount();

                for (int i = 0; i < rawsCount; i++) {
                    Double gravityX = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.GRAVITY_X));
                    Double gravityY = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.GRAVITY_Y));
                    Double gravityZ = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.GRAVITY_Z));
                    Double accelerationX = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.ACCELERATION_X));
                    Double accelerationY = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.ACCELERATION_Y));
                    Double accelerationZ = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.ACCELERATION_Z));

                    gravityXArray.add(gravityX);
                    gravityYArray.add(gravityY);
                    gravityZArray.add(gravityZ);
                    accelerationXArray.add(accelerationX);
                    accelerationYArray.add(accelerationY);
                    accelerationZArray.add(accelerationZ);

                    if (gravityX < minimum.get(0)) {
                        minimum.set(0, gravityX);
                    }
                    if (gravityY < minimum.get(1)) {
                        minimum.set(1, gravityY);
                    }
                    if (gravityZ < minimum.get(2)) {
                        minimum.set(2, gravityZ);
                    }
                    if (accelerationX < minimum.get(3)) {
                        minimum.set(3, accelerationX);
                    }
                    if (accelerationY < minimum.get(4)) {
                        minimum.set(4, accelerationY);
                    }
                    if (accelerationZ < minimum.get(5)) {
                        minimum.set(5, accelerationZ);
                    }

                    if (gravityX > maximum.get(0)) {
                        maximum.set(0, gravityX);
                    }
                    if (gravityY > maximum.get(1)) {
                        maximum.set(1, gravityY);
                    }
                    if (gravityZ > maximum.get(2)) {
                        maximum.set(2, gravityZ);
                    }
                    if (accelerationX > maximum.get(3)) {
                        maximum.set(3, accelerationX);
                    }
                    if (accelerationY > maximum.get(4)) {
                        maximum.set(4, accelerationY);
                    }
                    if (accelerationZ > minimum.get(5)) {
                        maximum.set(5, accelerationZ);
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

                    rawsCursor.moveToNext();
                }

                ArrayList<Double> mean = new ArrayList<>(6);
                ArrayList<Double> energy = new ArrayList<>(6);

                for (int i = 0; i < 6; i++) {
                    mean.add(sum.get(i) / rawsCount);
                    energy.add(squareSum.get(i) / rawsCount);
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

                if (rawsCount % 2 == 0) {
                    medianGravityX = (sortGravityX[rawsCount / 2 - 1] + sortGravityX[rawsCount / 2]) / 2;
                    medianGravityY = (sortGravityY[rawsCount / 2 - 1] + sortGravityY[rawsCount / 2]) / 2;
                    medianGravityZ = (sortGravityZ[rawsCount / 2 - 1] + sortGravityZ[rawsCount / 2]) / 2;
                    medianAccelerationX = (sortAccelerationX[rawsCount / 2 - 1] + sortAccelerationX[rawsCount / 2]) / 2;
                    medianAccelerationY = (sortAccelerationY[rawsCount / 2 - 1] + sortAccelerationY[rawsCount / 2]) / 2;
                    medianAccelerationZ = (sortAccelerationZ[rawsCount / 2 - 1] + sortAccelerationZ[rawsCount / 2]) / 2;

                } else {
                    medianGravityX = sortGravityX[(rawsCount - 1) / 2];
                    medianGravityY = sortGravityY[(rawsCount - 1) / 2];
                    medianGravityZ = sortGravityZ[(rawsCount - 1) / 2];
                    medianAccelerationX = sortAccelerationX[(rawsCount - 1) / 2];
                    medianAccelerationY = sortAccelerationY[(rawsCount - 1) / 2];
                    medianAccelerationZ = sortAccelerationZ[(rawsCount - 1) / 2];
                }

                rawsCursor.moveToFirst();

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

                for (int i = 0; i < rawsCount; i++) {
                    Double gravityX = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.GRAVITY_X));
                    Double gravityY = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.GRAVITY_Y));
                    Double gravityZ = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.GRAVITY_Z));
                    Double accelerationX = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.ACCELERATION_X));
                    Double accelerationY = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.ACCELERATION_Y));
                    Double accelerationZ = rawsCursor.getDouble(rawsCursor.getColumnIndex(RawsContract.ACCELERATION_Z));

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

                    rawsCursor.moveToNext();
                }

                ArrayList<Double> standardDeviation = new ArrayList<>(6);

                for (int i = 0; i < 6; i++) {
                    standardDeviation.add(Math.sqrt(deviationSum.get(i) / rawsCount));
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

                if (rawsCount % 2 == 0) {
                    absoluteMedianGravityX = (sortAbsoluteMedianGravityX[rawsCount / 2 - 1] + sortAbsoluteMedianGravityX[rawsCount / 2]) / 2;
                    absoluteMedianGravityY = (sortAbsoluteMedianGravityY[rawsCount / 2 - 1] + sortAbsoluteMedianGravityY[rawsCount / 2]) / 2;
                    absoluteMedianGravityZ = (sortAbsoluteMedianGravityZ[rawsCount / 2 - 1] + sortAbsoluteMedianGravityZ[rawsCount / 2]) / 2;
                    absoluteMedianAccelerationX = (sortAbsoluteMedianAccelerationX[rawsCount / 2 - 1] + sortAbsoluteMedianAccelerationX[rawsCount / 2]) / 2;
                    absoluteMedianAccelerationY = (sortAbsoluteMedianAccelerationY[rawsCount / 2 - 1] + sortAbsoluteMedianAccelerationY[rawsCount / 2]) / 2;
                    absoluteMedianAccelerationZ = (sortAbsoluteMedianAccelerationZ[rawsCount / 2 - 1] + sortAbsoluteMedianAccelerationZ[rawsCount / 2]) / 2;
                } else {
                    absoluteMedianGravityX = sortAbsoluteMedianGravityX[(rawsCount - 1) / 2];
                    absoluteMedianGravityY = sortAbsoluteMedianGravityY[(rawsCount - 1) / 2];
                    absoluteMedianGravityZ = sortAbsoluteMedianGravityZ[(rawsCount - 1) / 2];
                    absoluteMedianAccelerationX = sortAbsoluteMedianAccelerationX[(rawsCount - 1) / 2];
                    absoluteMedianAccelerationY = sortAbsoluteMedianAccelerationY[(rawsCount - 1) / 2];
                    absoluteMedianAccelerationZ = sortAbsoluteMedianAccelerationZ[(rawsCount - 1) / 2];
                }

                ContentValues values = new ContentValues();

                values.put(FeaturesContract.TIMESTAMP_START, dateFormatter.format(timestampStart));
                values.put(FeaturesContract.TIMESTAMP_STOP, dateFormatter.format(timestampStop));
                values.put(FeaturesContract.ACTIVITY_ID, String.valueOf(activityId));
                values.put(FeaturesContract.GRAVITY_X_MIN, minimum.get(0));
                values.put(FeaturesContract.GRAVITY_Y_MIN, minimum.get(1));
                values.put(FeaturesContract.GRAVITY_Z_MIN, minimum.get(2));
                values.put(FeaturesContract.ACCELERATION_X_MIN, minimum.get(3));
                values.put(FeaturesContract.ACCELERATION_Y_MIN, minimum.get(4));
                values.put(FeaturesContract.ACCELERATION_Z_MIN, minimum.get(5));
                values.put(FeaturesContract.GRAVITY_X_MAX, maximum.get(0));
                values.put(FeaturesContract.GRAVITY_Y_MAX, maximum.get(1));
                values.put(FeaturesContract.GRAVITY_Z_MAX, maximum.get(2));
                values.put(FeaturesContract.ACCELERATION_X_MAX, maximum.get(3));
                values.put(FeaturesContract.ACCELERATION_Y_MAX, maximum.get(4));
                values.put(FeaturesContract.ACCELERATION_Z_MAX, maximum.get(5));
                values.put(FeaturesContract.GRAVITY_X_MEAN, mean.get(0));
                values.put(FeaturesContract.GRAVITY_Y_MEAN, mean.get(1));
                values.put(FeaturesContract.GRAVITY_Z_MEAN, mean.get(2));
                values.put(FeaturesContract.ACCELERATION_X_MEAN, mean.get(3));
                values.put(FeaturesContract.ACCELERATION_Y_MEAN, mean.get(4));
                values.put(FeaturesContract.ACCELERATION_Z_MEAN, mean.get(5));
                values.put(FeaturesContract.GRAVITY_X_ENERGY, energy.get(0));
                values.put(FeaturesContract.GRAVITY_Y_ENERGY, energy.get(1));
                values.put(FeaturesContract.GRAVITY_Z_ENERGY, energy.get(2));
                values.put(FeaturesContract.ACCELERATION_X_ENERGY, energy.get(3));
                values.put(FeaturesContract.ACCELERATION_Y_ENERGY, energy.get(4));
                values.put(FeaturesContract.ACCELERATION_Z_ENERGY, energy.get(5));
                values.put(FeaturesContract.GRAVITY_X_STANDARD_DEVIATION, standardDeviation.get(0));
                values.put(FeaturesContract.GRAVITY_Y_STANDARD_DEVIATION, standardDeviation.get(1));
                values.put(FeaturesContract.GRAVITY_Z_STANDARD_DEVIATION, standardDeviation.get(2));
                values.put(FeaturesContract.ACCELERATION_X_STANDARD_DEVIATION, standardDeviation.get(3));
                values.put(FeaturesContract.ACCELERATION_Y_STANDARD_DEVIATION, standardDeviation.get(4));
                values.put(FeaturesContract.ACCELERATION_Z_STANDARD_DEVIATION, standardDeviation.get(5));
                values.put(FeaturesContract.GRAVITY_X_ABSOLUTE_MEDIAN, absoluteMedianGravityX);
                values.put(FeaturesContract.GRAVITY_Y_ABSOLUTE_MEDIAN, absoluteMedianGravityY);
                values.put(FeaturesContract.GRAVITY_Z_ABSOLUTE_MEDIAN, absoluteMedianGravityZ);
                values.put(FeaturesContract.ACCELERATION_X_ABSOLUTE_MEDIAN, absoluteMedianAccelerationX);
                values.put(FeaturesContract.ACCELERATION_Y_ABSOLUTE_MEDIAN, absoluteMedianAccelerationY);
                values.put(FeaturesContract.ACCELERATION_Z_ABSOLUTE_MEDIAN, absoluteMedianAccelerationZ);

                database.insert(FeaturesContract.TABLE_NAME, null, values);
            }
        }
    }

    public static void learnArtificialNeuralNetwork() {
        Integer inputNeurons = preferences.getInt(context.getString(R.string.shared_preferences_settings_input_neurons), Constants.DEFAULT_INPUT_NEURONS);
        Integer hiddenLayers = preferences.getInt(context.getString(R.string.shared_preferences_settings_hidden_layers), Constants.DEFAULT_HIDDEN_LAYERS);
        Integer hiddenNeurons = preferences.getInt(context.getString(R.string.shared_preferences_settings_hidden_neurons), Constants.DEFAULT_HIDDEN_NEURONS);

        artificialNeuralNetwork = new ArtificialNeuralNetwork(context, inputNeurons, hiddenLayers, hiddenNeurons, Constants.OUTPUT_NEURONS);
        artificialNeuralNetworkActive = true;

        String[] featuresProjection = {
                FeaturesContract.ACTIVITY_ID,
                FeaturesContract.GRAVITY_X_MIN,
                FeaturesContract.GRAVITY_Y_MIN,
                FeaturesContract.GRAVITY_Z_MIN,
                FeaturesContract.ACCELERATION_X_MIN,
                FeaturesContract.ACCELERATION_Y_MIN,
                FeaturesContract.ACCELERATION_Z_MIN,
                FeaturesContract.GRAVITY_X_MAX,
                FeaturesContract.GRAVITY_Y_MAX,
                FeaturesContract.GRAVITY_Z_MAX,
                FeaturesContract.ACCELERATION_X_MAX,
                FeaturesContract.ACCELERATION_Y_MAX,
                FeaturesContract.ACCELERATION_Z_MAX,
                FeaturesContract.GRAVITY_X_MEAN,
                FeaturesContract.GRAVITY_Y_MEAN,
                FeaturesContract.GRAVITY_Z_MEAN,
                FeaturesContract.ACCELERATION_X_MEAN,
                FeaturesContract.ACCELERATION_Y_MEAN,
                FeaturesContract.ACCELERATION_Z_MEAN,
                FeaturesContract.GRAVITY_X_ENERGY,
                FeaturesContract.GRAVITY_Y_ENERGY,
                FeaturesContract.GRAVITY_Z_ENERGY,
                FeaturesContract.ACCELERATION_X_ENERGY,
                FeaturesContract.ACCELERATION_Y_ENERGY,
                FeaturesContract.ACCELERATION_Z_ENERGY,
                FeaturesContract.GRAVITY_X_STANDARD_DEVIATION,
                FeaturesContract.GRAVITY_Y_STANDARD_DEVIATION,
                FeaturesContract.GRAVITY_Z_STANDARD_DEVIATION,
                FeaturesContract.ACCELERATION_X_STANDARD_DEVIATION,
                FeaturesContract.ACCELERATION_Y_STANDARD_DEVIATION,
                FeaturesContract.ACCELERATION_Z_STANDARD_DEVIATION,
                FeaturesContract.GRAVITY_X_ABSOLUTE_MEDIAN,
                FeaturesContract.GRAVITY_Y_ABSOLUTE_MEDIAN,
                FeaturesContract.GRAVITY_Z_ABSOLUTE_MEDIAN,
                FeaturesContract.ACCELERATION_X_ABSOLUTE_MEDIAN,
                FeaturesContract.ACCELERATION_Y_ABSOLUTE_MEDIAN,
                FeaturesContract.ACCELERATION_Z_ABSOLUTE_MEDIAN
        };

        Cursor featuresCursor = database.query(
                FeaturesContract.TABLE_NAME,
                featuresProjection,
                null, null, null, null, null
        );

        Integer featuresCount = featuresCursor.getCount();

        ArrayList<ArrayList<Double>> features = new ArrayList<>(featuresCount);
        ArrayList<ArrayList<Double>> activities = new ArrayList<>(featuresCount);

        Double desiredActivityWeight = Double.longBitsToDouble(preferences.getLong(context.getString(R.string.shared_preferences_settings_desired_activity_weight), Double.doubleToLongBits(Constants.DEFAULT_DESIRED_ACTIVITY_WEIGHT)));
        Double undesiredActivityWeight = Double.longBitsToDouble(preferences.getLong(context.getString(R.string.shared_preferences_settings_undesired_activity_weight), Double.doubleToLongBits(Constants.DEFAULT_UNDESIRED_ACTIVITY_WEIGHT)));

        featuresCursor.moveToFirst();

        for (int i = 0; i < featuresCount; i++) {
            features.add(new ArrayList<Double>());

            for (int j = 1; j <= inputNeurons; j++) {
                features.get(i).add(featuresCursor.getDouble(j));
            }

            activities.add(new ArrayList<Double>());

            for (int j = 1; j <= Constants.OUTPUT_NEURONS; j++) {
                if (j == featuresCursor.getInt(featuresCursor.getColumnIndex(FeaturesContract.ACTIVITY_ID))) {
                    activities.get(i).add(desiredActivityWeight);
                } else {
                    activities.get(i).add(undesiredActivityWeight);
                }
            }

            featuresCursor.moveToNext();
        }

        Random random = new Random();

        Integer learningIterations = preferences.getInt(context.getString(R.string.shared_preferences_settings_learning_iterations), Constants.DEFAULT_LEARNING_ITERATIONS);

        for (int i = 0; i < learningIterations; i++) {
            Integer index = random.nextInt(featuresCount);

            artificialNeuralNetwork.learn(features.get(index), activities.get(index));
        }

        ArrayList<ArrayList<Integer>> classificationTable = new ArrayList<>(Constants.OUTPUT_NEURONS);

        for (int i = 0; i < Constants.OUTPUT_NEURONS; i++) {
            classificationTable.add(new ArrayList<Integer>(Constants.OUTPUT_NEURONS));

            for (int j = 0; j < Constants.OUTPUT_NEURONS; j++) {
                classificationTable.get(i).add(0);
            }
        }

        Double correct = 0.0;
        Double incorrect = 0.0;

        for (int i = 0; i < Constants.TEST_ITERATIONS; i++) {
            Integer index = random.nextInt(featuresCount);

            ArrayList<Double> output = artificialNeuralNetwork.run(features.get(index));

            Integer desiredActivity = 0;
            Integer predictedActivity = 0;

            for (int j = 0; j < Constants.OUTPUT_NEURONS; j++) {
                if (activities.get(index).get(j) > activities.get(index).get(desiredActivity)) {
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

            classificationTable.get(predictedActivity).set(desiredActivity, classificationTable.get(predictedActivity).get(desiredActivity) + 1);
        }

        Log.v(TAG, "ARTIFICIAL NEURAL NETWORK (" + inputNeurons + " - " + hiddenLayers + " × " + hiddenNeurons + " - " + Constants.OUTPUT_NEURONS + ")");
        Log.v(TAG, "Accuracy: " + String.valueOf((correct * 100.0) / (correct + incorrect)) + "%");
        Log.v(TAG, "Activities: a - lie, b - sit, c - stand, d - walk");
        Log.v(TAG, "-------------------------");
        Log.v(TAG, "|  a  |  b  |  c  |  d  |");
        Log.v(TAG, "|-----+-----+-----+-----| classified as:");

        String activityLabels = "abcd";

        for (int i = 0; i < Constants.OUTPUT_NEURONS; i++) {
            StringBuilder builder = new StringBuilder();

            builder.append("| ");

            for (int j = 0; j < Constants.OUTPUT_NEURONS; j++) {
                Integer result = classificationTable.get(i).get(j);

                if (result / 100 > 0) {
                    builder.append(result.toString() + " | ");
                } else if (result / 10 > 0) {
                    builder.append(" " + result.toString() + " | ");
                } else {
                    builder.append("  " + result.toString() + " | ");
                }
            }

            builder.append(activityLabels.charAt(i));

            Log.v(TAG, builder.toString());
        }
        Log.v(TAG, "-------------------------");
    }
}
