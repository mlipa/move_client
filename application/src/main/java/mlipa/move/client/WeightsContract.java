package mlipa.move.client;

import android.provider.BaseColumns;

public class WeightsContract {
    public static class Weights implements BaseColumns {
        public static final String TABLE_NAME = "weights";
        public static final String CLASSIFIER_ID = "classifier_id";
        public static final String NEURON_CONNECTION_ID = "neuron_connection_id";
        public static final String SOURCE_NEURON_ID = "source_neuron_id";
        public static final String DESTINATION_NEURON_ID = "destination_neuron_id";
        public static final String WEIGHT = "weight";
    }

    private WeightsContract() {
    }
}
