package mlipa.move.client;

import android.provider.BaseColumns;

public class ClassifiersContract {
    public static class Classifiers implements BaseColumns {
        public static final String TABLE_NAME = "classifiers";
        public static final String NAME = "name";
    }

    private ClassifiersContract() {
    }
}
