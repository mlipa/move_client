package mlipa.move.client;

import android.provider.BaseColumns;

public class ActivitiesContract {
    public static class Activities implements BaseColumns {
        public static final String TABLE_NAME = "activities";
        public static final String NAME = "name";
    }

    private ActivitiesContract() {
    }
}
