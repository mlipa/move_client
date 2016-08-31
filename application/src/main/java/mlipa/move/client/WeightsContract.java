package mlipa.move.client;

import android.provider.BaseColumns;

public class WeightsContract {
    public static class Weights implements BaseColumns {
        // TODO: CHANGE WEIGHTS NUMBER TO EQUAL QUALITIES NUMBER
        private final int WEIGHTS_NUMBER = 100;
        public static final String TABLE_NAME = "weights";
        public static final String COLUMN_NAME_CLASSIFIER_ID = "classifier_id";
        public static final String[] COLUMN_NAME_WEIGHT = new String[WEIGHTS_NUMBER];

        private Weights() {
            for (int i = 0; i < WEIGHTS_NUMBER; i++) {
                COLUMN_NAME_WEIGHT[i] = "weight_" + String.valueOf(i);
            }
        }
    }

    private WeightsContract() {
    }
}
