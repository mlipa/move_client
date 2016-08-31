package mlipa.move.client;

import android.provider.BaseColumns;

public class UsersContract {
    public static class Users implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_USERNAME = "username";
    }

    private UsersContract() {
    }
}
