package mlipa.move.client;

import android.provider.BaseColumns;

public class NeuronConnectionsContract {
    public static class NeuronConnections implements BaseColumns {
        public static final String TABLE_NAME = "neuron_connections";
        public static final String NEURON_CONNECTION_ID = "neuron_connection_id";
        public static final String SOURCE_NEURON_ID = "source_neuron_id";
        public static final String DESTINATION_NEURON_ID = "destination_neuron_id";
        public static final String WEIGHT = "weight";
    }

    private NeuronConnectionsContract() {
    }
}
