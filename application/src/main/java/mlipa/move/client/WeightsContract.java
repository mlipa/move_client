package mlipa.move.client;

import android.provider.BaseColumns;

import java.util.ArrayList;

public class WeightsContract {
    public static class Weights implements BaseColumns {
        // TODO: CHANGE WEIGHTS NUMBER TO EQUAL QUALITIES NUMBER
        public static int WEIGHTS_NUMBER = 100;
        public static final String TABLE_NAME = "weights";
        public static final String COLUMN_NAME_CLASSIFIER_ID = "classifier_id";
        public static final ArrayList<String> COLUMN_NAME_WEIGHT = new ArrayList<>(WEIGHTS_NUMBER);

        public Weights() {
            for (int i = 0; i < WEIGHTS_NUMBER; i++) {
                COLUMN_NAME_WEIGHT.add("weight_" + String.valueOf(i));
            }
        }
    }

    private WeightsContract() {
    }
}
