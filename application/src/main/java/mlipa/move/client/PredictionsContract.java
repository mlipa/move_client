package mlipa.move.client;

import android.provider.BaseColumns;

public class PredictionsContract implements BaseColumns {
    public static final String TABLE_NAME = "predictions";
    public static final String TIMESTAMP = "timestamp";
    public static final String ACTIVITY_ID = "activity_id";
    public static final String CLASSIFIER_ID = "classifier_id";
    public static final String USER_ID = "user_id";

    private PredictionsContract() {
    }
}
