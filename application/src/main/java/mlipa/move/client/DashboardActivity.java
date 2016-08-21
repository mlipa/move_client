package mlipa.move.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_dashboard, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);

                startActivity(settingsIntent);

                return true;
            case R.id.profile:
                Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);

                startActivity(profileIntent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
