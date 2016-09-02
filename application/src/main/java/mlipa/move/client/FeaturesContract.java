package mlipa.move.client;

import android.provider.BaseColumns;

import java.util.ArrayList;

public class FeaturesContract {
    public static class Features implements BaseColumns {
        // TODO: CHANGE FEATURES NUMBER TO EQUAL WEIGHTS NUMBER
        public static final int FEATURES_NUMBER = 100;
        public static final String TABLE_NAME = "features";
        public static final String COLUMN_NAME_TIMESTAMP_START = "timestamp_start";
        public static final String COLUMN_NAME_TIMESTAMP_STOP = "timestamp_stop";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final ArrayList<String> COLUMN_NAME_FEATURES = new ArrayList<>(FEATURES_NUMBER);

        public Features() {
            for (int i = 0; i < FEATURES_NUMBER; i++) {
                COLUMN_NAME_FEATURES.add("feature_" + String.valueOf(i));
            }
        }
    }

    private FeaturesContract() {
    }
}
