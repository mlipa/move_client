package mlipa.move.client;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    private static final String CLIENT_CLASSIFIER_ID_KEY = "classifierId";
    private static final String PREFERENCE_CLASSIFIER_KEY = "classifier";

    private ListPreference classifier;
    private Preference.OnPreferenceChangeListener preferenceChangedListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ListPreference classifier = (ListPreference) preference;

            classifier.setValue(newValue.toString());
            classifier.setSummary(classifier.getEntry());

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
