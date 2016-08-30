package mlipa.move.client;

import android.provider.BaseColumns;

public class PredictionsContract {
    private PredictionsContract() {
    }

    public static class Predictions implements BaseColumns {
        public static final String TABLE_NAME = "predictions";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_CLASSIFIER_ID = "classifier_id";
    }
}
