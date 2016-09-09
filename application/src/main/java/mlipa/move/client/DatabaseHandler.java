package mlipa.move.client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
    private final String TAG = DatabaseHandler.class.toString();

    public static final String DATABASE_NAME = "m_move.db";
    public static final Integer DATABASE_VERSION = 3;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createActivitiesTable = "CREATE TABLE " + ActivitiesContract.TABLE_NAME + " (" +
                ActivitiesContract._ID + " INTEGER PRIMARY KEY, " +
                ActivitiesContract.NAME + " VARCHAR(32) UNIQUE NOT NULL);";

        String createClassifiersTable = "CREATE TABLE " + ClassifiersContract.TABLE_NAME + " (" +
                ClassifiersContract._ID + " INTEGER PRIMARY KEY, " +
                ClassifiersContract.NAME + " VARCHAR(32) UNIQUE NOT NULL);";

        String createUsersTable = "CREATE TABLE " + UsersContract.TABLE_NAME + " (" +
                UsersContract._ID + " INTEGER PRIMARY KEY, " +
                UsersContract.USERNAME + " VARCHAR(32) UNIQUE NOT NULL);";

        String createRawsTable = "CREATE TABLE " + RawsContract.TABLE_NAME + " (" +
                RawsContract._ID + " INTEGER PRIMARY KEY, " +
                RawsContract.TIMESTAMP + " DATETIME NOT NULL, " +
                RawsContract.ACTIVITY_ID + " INTEGER NOT NULL, " +
                RawsContract.USER_ID + " INTEGER NOT NULL, " +
                RawsContract.GRAVITY_X + " DOUBLE NOT NULL, " +
                RawsContract.GRAVITY_Y + " DOUBLE NOT NULL, " +
                RawsContract.GRAVITY_Z + " DOUBLE NOT NULL, " +
                RawsContract.ACCELERATION_X + " DOUBLE NOT NULL, " +
                RawsContract.ACCELERATION_Y + " DOUBLE NOT NULL, " +
                RawsContract.ACCELERATION_Z + " DOUBLE NOT NULL, " +
                "FOREIGN KEY (" + RawsContract.ACTIVITY_ID + ") REFERENCES " + ActivitiesContract.TABLE_NAME + "(" + ActivitiesContract._ID + "), " +
                "FOREIGN KEY (" + RawsContract.USER_ID + ") REFERENCES " + UsersContract.TABLE_NAME + "(" + UsersContract._ID + "));";

        String createFeaturesTable = "CREATE TABLE " + FeaturesContract.TABLE_NAME + " (" +
                FeaturesContract._ID + "  INTEGER PRIMARY KEY, " +
                FeaturesContract.TIMESTAMP_START + " DATETIME NOT NULL, " +
                FeaturesContract.TIMESTAMP_STOP + " DATETIME NOT NULL, " +
                FeaturesContract.ACTIVITY_ID + " INTEGER NOT NULL, " +
                FeaturesContract.GRAVITY_X_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Y_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Z_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_X_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Y_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Z_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_X_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Y_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Z_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_X_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Y_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Z_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_X_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Y_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Z_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_X_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Y_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Z_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_X_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Y_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Z_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_X_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Y_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Z_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_X_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Y_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Z_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_X_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Y_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Z_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_X_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Y_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.GRAVITY_Z_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_X_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Y_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.ACCELERATION_Z_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, ";

        createFeaturesTable += "FOREIGN KEY (" + FeaturesContract.ACTIVITY_ID + ") REFERENCES " + ActivitiesContract.TABLE_NAME + "(" + ActivitiesContract._ID + "));";

        String createPredictionsTable = "CREATE TABLE " + PredictionsContract.TABLE_NAME + " (" +
                PredictionsContract._ID + "  INTEGER PRIMARY KEY, " +
                PredictionsContract.TIMESTAMP + " DATETIME NOT NULL, " +
                PredictionsContract.ACTIVITY_ID + " INTEGER NOT NULL, " +
                PredictionsContract.CLASSIFIER_ID + " INTEGER NOT NULL, " +
                PredictionsContract.USER_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + PredictionsContract.ACTIVITY_ID + ") REFERENCES " + ActivitiesContract.TABLE_NAME + "(" + ActivitiesContract._ID + "), " +
                "FOREIGN KEY (" + PredictionsContract.CLASSIFIER_ID + ") REFERENCES " + ClassifiersContract.TABLE_NAME + "(" + ClassifiersContract._ID + "), " +
                "FOREIGN KEY (" + PredictionsContract.USER_ID + ") REFERENCES " + UsersContract.TABLE_NAME + "(" + UsersContract._ID + "));";

        database.execSQL(createActivitiesTable);
        database.execSQL(createClassifiersTable);
        database.execSQL(createUsersTable);
        database.execSQL(createRawsTable);
        database.execSQL(createFeaturesTable);
        database.execSQL(createPredictionsTable);

        String insertActivities = "INSERT INTO " + ActivitiesContract.TABLE_NAME + " (" +
                ActivitiesContract.NAME + ") VALUES " +
                "('Lie'), " +
                "('Sit'), " +
                "('Stand'), " +
                "('Walk'), " +
                "('Not detected');";

        String insertClassifiers = "INSERT INTO " + ClassifiersContract.TABLE_NAME + "(" +
                ClassifiersContract.NAME + ") VALUES " +
                "('Artificial neural network');";

        database.execSQL(insertActivities);
        database.execSQL(insertClassifiers);

        Log.v(TAG, "[DatabaseHandler.onCreate()] " + DATABASE_NAME + " (version " + DATABASE_VERSION + ") created successfully!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion != oldVersion) {
            String dropActivitiesTable = "DROP TABLE IF EXISTS " + ActivitiesContract.TABLE_NAME + ";";
            String dropClassifiersTable = "DROP TABLE IF EXISTS " + ClassifiersContract.TABLE_NAME + ";";
            String dropUsersTable = "DROP TABLE IF EXISTS " + UsersContract.TABLE_NAME + ";";
            String dropRawsTable = "DROP TABLE IF EXISTS " + RawsContract.TABLE_NAME + ";";
            String dropFeaturesTable = "DROP TABLE IF EXISTS " + FeaturesContract.TABLE_NAME + ";";
            String dropPredictionsTable = "DROP TABLE IF EXISTS " + PredictionsContract.TABLE_NAME + ";";

            database.execSQL(dropPredictionsTable);
            database.execSQL(dropFeaturesTable);
            database.execSQL(dropRawsTable);
            database.execSQL(dropUsersTable);
            database.execSQL(dropClassifiersTable);
            database.execSQL(dropActivitiesTable);

            Log.v(TAG, "[DatabaseHandler.onUpgrade()] " + DATABASE_NAME + " (version " + DATABASE_VERSION + ") upgraded successfully!");

            onCreate(database);
        }
    }
}
