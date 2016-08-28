package mlipa.move.client;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SettingsActivity extends AppCompatActivity {
    private Intent intent;
    public static RequestQueue queue;

    private SettingsFragment settingsFragment;
    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        intent = getIntent();
        queue = Volley.newRequestQueue(SettingsActivity.this);

        Cookie.preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

        settingsFragment = new SettingsFragment();

        args = new Bundle();

        args.putString("classifierId", intent.getStringExtra("classifierId"));

        settingsFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(R.id.layout, settingsFragment).commit();
    }
}
