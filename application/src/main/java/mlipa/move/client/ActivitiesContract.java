package mlipa.move.client;

import android.provider.BaseColumns;

public class ActivitiesContract {
    private ActivitiesContract() {
    }

    public static class Activities implements BaseColumns {
        public static final String TABLE_NAME = "activities";
        public static final String COLUMN_NAME_NAME = "name";
    }
}
