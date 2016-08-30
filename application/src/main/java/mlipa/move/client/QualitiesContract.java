package mlipa.move.client;

import android.provider.BaseColumns;

public class QualitiesContract {
    private QualitiesContract() {
    }

    public static class Qualities implements BaseColumns {
        // TODO: CHANGE QUALITIES NUMBER TO EQUAL WEIGHTS NUMBER
        private final int QUALITIES_NUMBER = 100;
        public static final String TABLE_NAME = "qualities";
        public static final String COLUMN_NAME_TIMESTAMP_START = "timestamp_start";
        public static final String COLUMN_NAME_TIMESTAMP_STOP = "timestamp_stop";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String[] COLUMN_NAME_QUALITIES = new String[QUALITIES_NUMBER];

        private Qualities() {
            for (int i = 0; i < QUALITIES_NUMBER; i++) {
                COLUMN_NAME_QUALITIES[i] = "quality_" + String.valueOf(i);
            }
        }
    }
}
