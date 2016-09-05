package mlipa.move.client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHandler.class.toString();

    public static final String DATABASE_NAME = "m_move.db";
    public static final Integer DATABASE_VERSION = 3;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createActivitiesTable = "CREATE TABLE " + ActivitiesContract.Activities.TABLE_NAME + " (" +
                ActivitiesContract.Activities._ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL, " +
                ActivitiesContract.Activities.NAME + " VARCHAR(32) UNIQUE NOT NULL);";

        String createClassifiersTable = "CREATE TABLE " + ClassifiersContract.Classifiers.TABLE_NAME + " (" +
                ClassifiersContract.Classifiers._ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL, " +
                ClassifiersContract.Classifiers.NAME + " VARCHAR(32) UNIQUE NOT NULL);";

        String createUsersTable = "CREATE TABLE " + UsersContract.Users.TABLE_NAME + " (" +
                UsersContract.Users._ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL, " +
                UsersContract.Users.USERNAME + " VARCHAR(32) UNIQUE NOT NULL);";

        String createRawsTable = "CREATE TABLE " + RawContract.Raws.TABLE_NAME + " (" +
                RawContract.Raws._ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL, " +
                RawContract.Raws.TIMESTAMP + " DATETIME NOT NULL, " +
                RawContract.Raws.ACTIVITY_ID + " INTEGER NOT NULL, " +
                RawContract.Raws.USER_ID + " INTEGER NOT NULL, " +
                RawContract.Raws.GRAVITY_X + " DOUBLE NOT NULL, " +
                RawContract.Raws.GRAVITY_Y + " DOUBLE NOT NULL, " +
                RawContract.Raws.GRAVITY_Z + " DOUBLE NOT NULL, " +
                RawContract.Raws.ACCELERATION_X + " DOUBLE NOT NULL, " +
                RawContract.Raws.ACCELERATION_Y + " DOUBLE NOT NULL, " +
                RawContract.Raws.ACCELERATION_Z + " DOUBLE NOT NULL, " +
                "FOREIGN KEY (" + RawContract.Raws.ACTIVITY_ID + ") REFERENCES " + ActivitiesContract.Activities.TABLE_NAME + "(" + ActivitiesContract.Activities._ID + "), " +
                "FOREIGN KEY (" + RawContract.Raws.USER_ID + ") REFERENCES " + UsersContract.Users.TABLE_NAME + "(" + UsersContract.Users._ID + "));";

        String createFeaturesTable = "CREATE TABLE " + FeaturesContract.Features.TABLE_NAME + " (" +
                FeaturesContract.Features._ID + "  INTEGER PRIMARY KEY UNIQUE NOT NULL, " +
                FeaturesContract.Features.TIMESTAMP_START + " DATETIME NOT NULL, " +
                FeaturesContract.Features.TIMESTAMP_STOP + " DATETIME NOT NULL, " +
                FeaturesContract.Features.ACTIVITY_ID + " INTEGER NOT NULL, " +
                FeaturesContract.Features.GRAVITY_X_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Y_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Z_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_X_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Y_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Z_MIN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_X_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Y_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Z_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_X_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Y_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Z_MAX + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_X_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Y_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Z_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_X_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Y_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Z_MEAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_X_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Y_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Z_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_X_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Y_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Z_STANDARD_DEVIATION + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_X_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Y_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Z_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_X_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Y_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Z_ABSOLUTE_MEDIAN + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_X_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Y_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.GRAVITY_Z_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_X_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Y_ENERGY + " DOUBLE NOT NULL, " +
                FeaturesContract.Features.ACCELERATION_Z_ENERGY + " DOUBLE NOT NULL, ";

        createFeaturesTable += "FOREIGN KEY (" + FeaturesContract.Features.ACTIVITY_ID + ") REFERENCES " + ActivitiesContract.Activities.TABLE_NAME + "(" + ActivitiesContract.Activities._ID + "));";

        String createPredictionsTable = "CREATE TABLE " + PredictionsContract.Predictions.TABLE_NAME + " (" +
                PredictionsContract.Predictions._ID + "  INTEGER PRIMARY KEY UNIQUE NOT NULL, " +
                PredictionsContract.Predictions.TIMESTAMP + " DATETIME NOT NULL, " +
                PredictionsContract.Predictions.ACTIVITY_ID + " INTEGER NOT NULL, " +
                PredictionsContract.Predictions.CLASSIFIER_ID + " INTEGER NOT NULL, " +
                PredictionsContract.Predictions.USER_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + PredictionsContract.Predictions.ACTIVITY_ID + ") REFERENCES " + ActivitiesContract.Activities.TABLE_NAME + "(" + ActivitiesContract.Activities._ID + "), " +
                "FOREIGN KEY (" + PredictionsContract.Predictions.CLASSIFIER_ID + ") REFERENCES " + ClassifiersContract.Classifiers.TABLE_NAME + "(" + ClassifiersContract.Classifiers._ID + "), " +
                "FOREIGN KEY (" + PredictionsContract.Predictions.USER_ID + ") REFERENCES " + UsersContract.Users.TABLE_NAME + "(" + UsersContract.Users._ID + "));";

        database.execSQL(createActivitiesTable);
        database.execSQL(createClassifiersTable);
        database.execSQL(createUsersTable);
        database.execSQL(createRawsTable);
        database.execSQL(createFeaturesTable);
        database.execSQL(createPredictionsTable);

        String insertActivities = "INSERT INTO " + ActivitiesContract.Activities.TABLE_NAME + " (" +
                ActivitiesContract.Activities.NAME + ") VALUES " +
                "('Lie'), " +
                "('Sit'), " +
                "('Stand'), " +
                "('Walk'), " +
                "('Not detected');";

        String insertClassifiers = "INSERT INTO " + ClassifiersContract.Classifiers.TABLE_NAME + "(" +
                ClassifiersContract.Classifiers.NAME + ") VALUES " +
                "('Artificial neural network');";

        database.execSQL(insertActivities);
        database.execSQL(insertClassifiers);

        Log.v(TAG, DATABASE_NAME + " (version " + DATABASE_VERSION + ") created successfully!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion != oldVersion) {
            String dropActivitiesTable = "DROP TABLE IF EXISTS " + ActivitiesContract.Activities.TABLE_NAME + ";";
            String dropClassifiersTable = "DROP TABLE IF EXISTS " + ClassifiersContract.Classifiers.TABLE_NAME + ";";
            String dropUsersTable = "DROP TABLE IF EXISTS " + UsersContract.Users.TABLE_NAME + ";";
            String dropRawsTable = "DROP TABLE IF EXISTS " + RawContract.Raws.TABLE_NAME + ";";
            String dropFeaturesTable = "DROP TABLE IF EXISTS " + FeaturesContract.Features.TABLE_NAME + ";";
            String dropPredictionsTable = "DROP TABLE IF EXISTS " + PredictionsContract.Predictions.TABLE_NAME + ";";

            database.execSQL(dropPredictionsTable);
            database.execSQL(dropFeaturesTable);
            database.execSQL(dropRawsTable);
            database.execSQL(dropUsersTable);
            database.execSQL(dropClassifiersTable);
            database.execSQL(dropActivitiesTable);

            Log.v(TAG, DATABASE_NAME + " (version " + DATABASE_VERSION + ") dropped successfully!");

            onCreate(database);
        }
    }
}
