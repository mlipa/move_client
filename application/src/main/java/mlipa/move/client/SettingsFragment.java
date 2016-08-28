package mlipa.move.client;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = SettingsFragment.class.toString();

    private ListPreference classifier;
    private Preference.OnPreferenceChangeListener preferenceChangedListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ListPreference classifier = (ListPreference) preference;

            classifier.setValue(newValue.toString());
            classifier.setSummary(classifier.getEntry());

            Response.Listener<String> settingsListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getBoolean("success")) {
                            String classifierId = jsonResponse.getString("classifier_id");
                            String classifierName = jsonResponse.getString("classifier_name");

                            Log.v(TAG, "classifier_id = " + classifierId);
                            Log.v(TAG, "classifier_name = " + classifierName);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            SettingsActivity.queue.add(new SettingsRequest(Request.Method.POST, classifier.getValue(), settingsListener));

            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        classifier = (ListPreference) findPreference("classifier");

        classifier.setValue(getArguments().getString("classifierId"));
        classifier.setSummary(classifier.getEntry());

        changeSummaryToValue(classifier);
    }

    private void changeSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(preferenceChangedListener);
    }
}
