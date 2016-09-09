package mlipa.move.client;

import android.provider.BaseColumns;

public class FeaturesContract implements BaseColumns {
    public static final String TABLE_NAME = "features";
    public static final String TIMESTAMP_START = "timestamp_start";
    public static final String TIMESTAMP_STOP = "timestamp_stop";
    public static final String ACTIVITY_ID = "activity_id";
    public static final String GRAVITY_X_MIN = "gravity_x_min";
    public static final String GRAVITY_Y_MIN = "gravity_y_min";
    public static final String GRAVITY_Z_MIN = "gravity_z_min";
    public static final String ACCELERATION_X_MIN = "acceleration_x_min";
    public static final String ACCELERATION_Y_MIN = "acceleration_y_min";
    public static final String ACCELERATION_Z_MIN = "acceleration_z_min";
    public static final String GRAVITY_X_MAX = "gravity_x_max";
    public static final String GRAVITY_Y_MAX = "gravity_y_max";
    public static final String GRAVITY_Z_MAX = "gravity_z_max";
    public static final String ACCELERATION_X_MAX = "acceleration_x_max";
    public static final String ACCELERATION_Y_MAX = "acceleration_y_max";
    public static final String ACCELERATION_Z_MAX = "acceleration_z_max";
    public static final String GRAVITY_X_MEAN = "gravity_x_mean";
    public static final String GRAVITY_Y_MEAN = "gravity_y_mean";
    public static final String GRAVITY_Z_MEAN = "gravity_z_mean";
    public static final String ACCELERATION_X_MEAN = "acceleration_x_mean";
    public static final String ACCELERATION_Y_MEAN = "acceleration_y_mean";
    public static final String ACCELERATION_Z_MEAN = "acceleration_z_mean";
    public static final String GRAVITY_X_ENERGY = "gravity_x_energy";
    public static final String GRAVITY_Y_ENERGY = "gravity_y_energy";
    public static final String GRAVITY_Z_ENERGY = "gravity_z_energy";
    public static final String ACCELERATION_X_ENERGY = "acceleration_x_energy";
    public static final String ACCELERATION_Y_ENERGY = "acceleration_y_energy";
    public static final String ACCELERATION_Z_ENERGY = "acceleration_z_energy";
    public static final String GRAVITY_X_STANDARD_DEVIATION = "gravity_x_standard_deviation";
    public static final String GRAVITY_Y_STANDARD_DEVIATION = "gravity_y_standard_deviation";
    public static final String GRAVITY_Z_STANDARD_DEVIATION = "gravity_z_standard_deviation";
    public static final String ACCELERATION_X_STANDARD_DEVIATION = "acceleration_x_standard_deviation";
    public static final String ACCELERATION_Y_STANDARD_DEVIATION = "acceleration_y_standard_deviation";
    public static final String ACCELERATION_Z_STANDARD_DEVIATION = "acceleration_z_standard_deviation";
    public static final String GRAVITY_X_ABSOLUTE_MEDIAN = "gravity_x_absolute_median";
    public static final String GRAVITY_Y_ABSOLUTE_MEDIAN = "gravity_y_absolute_median";
    public static final String GRAVITY_Z_ABSOLUTE_MEDIAN = "gravity_z_absolute_median";
    public static final String ACCELERATION_X_ABSOLUTE_MEDIAN = "acceleration_x_absolute_median";
    public static final String ACCELERATION_Y_ABSOLUTE_MEDIAN = "acceleration_y_absolute_median";
    public static final String ACCELERATION_Z_ABSOLUTE_MEDIAN = "acceleration_z_absolute_median";

    private FeaturesContract() {
    }
}
