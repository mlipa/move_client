package mlipa.move.client;

import android.provider.BaseColumns;

public class RawContract {
    public static class Raws implements BaseColumns {
        public static final String TABLE_NAME = "raws";
        public static final String TIMESTAMP = "timestamp";
        public static final String ACTIVITY_ID = "activity_id";
        public static final String USER_ID = "user_id";
        public static final String GRAVITY_X = "gravity_x";
        public static final String GRAVITY_Y = "gravity_y";
        public static final String GRAVITY_Z = "gravity_z";
        public static final String ACCELERATION_X = "acceleration_x";
        public static final String ACCELERATION_Y = "acceleration_y";
        public static final String ACCELERATION_Z = "acceleration_z";
    }

    private RawContract() {
    }
}
