package mlipa.move.client;

import android.provider.BaseColumns;

public class RawContract {
    public static class Raws implements BaseColumns {
        public static final String TABLE_NAME = "raws";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
        public static final String COLUMN_NAME_Z = "z";
    }

    private RawContract() {
    }
}
