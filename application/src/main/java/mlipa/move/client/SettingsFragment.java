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

    private static final String SERVER_SUCCESS_KEY = "success";
    private static final String SERVER_CLASSIFIER_ID_KEY = "classifier_id";
    private static final String SERVER_CLASSIFIER_NAME_KEY = "classifier_name";
    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";
    private static final String PREFERENCE_CLASSIFIER_KEY = "classifier";

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

                        if (jsonResponse.getBoolean(SERVER_SUCCESS_KEY)) {
                            String classifierId = jsonResponse.getString(SERVER_CLASSIFIER_ID_KEY);
                            String classifierName = jsonResponse.getString(SERVER_CLASSIFIER_NAME_KEY);

                            Log.v(TAG, SERVER_CLASSIFIER_ID_KEY + " = " + classifierId);
                            Log.v(TAG, SERVER_CLASSIFIER_NAME_KEY + " = " + classifierName);
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

        classifier = (ListPreference) findPreference(PREFERENCE_CLASSIFIER_KEY);

        classifier.setValue(getArguments().getString(CLIENT_CLASSIFIER_ID_KEY));
        classifier.setSummary(classifier.getEntry());

        changeSummaryToValue(classifier);
    }

    private void changeSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(preferenceChangedListener);
    }
}
