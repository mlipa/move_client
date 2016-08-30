package mlipa.move.client;

import android.provider.BaseColumns;

public class ClassifiersContract {
    private ClassifiersContract() {
    }

    public static class Classifiers implements BaseColumns {
        public static final String TABLE_NAME = "classifiers";
        public static final String COLUMN_NAME_NAME = "name";
    }
}
