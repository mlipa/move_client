package mlipa.move.client;

import android.provider.BaseColumns;

public class RawContract {
    public static class Raws implements BaseColumns {
        public static final String TABLE_NAME = "raws";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_GRAVITY_X = "gravity_x";
        public static final String COLUMN_GRAVITY_Y = "gravity_y";
        public static final String COLUMN_GRAVITY_Z = "gravity_z";
        public static final String COLUMN_ACCELERATION_X = "acceleration_x";
        public static final String COLUMN_ACCELERATION_Y = "acceleration_y";
        public static final String COLUMN_ACCELERATION_Z = "acceleration_z";
    }

    private RawContract() {
    }
}
