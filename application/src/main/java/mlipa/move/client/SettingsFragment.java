package mlipa.move.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {
    private Context context;
    private SharedPreferences preferences;

    private EditTextPreference etpChronometerTime;
    private EditTextPreference etpDelayTime;
    private EditTextPreference etpWindowLength;

    private ListPreference lpClassifier;

    private EditTextPreference etpInputNeurons;
    private EditTextPreference etpHiddenLayers;
    private EditTextPreference etpHiddenNeurons;
    private EditTextPreference etpInputDivisor;
    private EditTextPreference etpLearningConstant;
    private EditTextPreference etpLearningIterations;
    private EditTextPreference etpDesiredActivityWeight;
    private EditTextPreference etpUndesiredActivityWeight;

    private Preference.OnPreferenceChangeListener onTimeChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String key = editTextPreference.getKey();

            Double valueDouble = Double.parseDouble(editTextPreference.getText());
            Double newValueDouble = Double.parseDouble(newValue.toString());

            SharedPreferences.Editor editor = preferences.edit();

            if (newValueDouble != valueDouble) {
                if (key.equals(getString(R.string.settings_chronometer_time_key))) {
                    if (newValueDouble >= Constants.CHRONOMETER_TIME_MIN && newValueDouble <= Constants.CHRONOMETER_TIME_MAX) {
                        editTextPreference.setText(String.valueOf(newValueDouble));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_time), String.valueOf(newValueDouble), Constants.CHRONOMETER_TIME_MIN, Constants.CHRONOMETER_TIME_MAX));

                        editor.putLong(getString(R.string.shared_preferences_settings_chronometer_time), Double.doubleToLongBits(newValueDouble));
                        editor.apply();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(getString(R.string.settings_delay_time_key))) {
                    if (newValueDouble >= Constants.DELAY_TIME_MIN && newValueDouble <= Constants.DELAY_TIME_MAX) {
                        editTextPreference.setText(String.valueOf(newValueDouble));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_time), String.valueOf(newValueDouble), Constants.DELAY_TIME_MIN, Constants.DELAY_TIME_MAX));

                        editor.putLong(getString(R.string.shared_preferences_settings_delay_time), Double.doubleToLongBits(newValueDouble));
                        editor.apply();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(getString(R.string.settings_window_length_key))) {
                    if (newValueDouble >= Constants.WINDOW_LENGTH_MIN && newValueDouble <= Constants.WINDOW_LENGTH_MAX) {
                        editTextPreference.setText(String.valueOf(newValueDouble));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_time), String.valueOf(newValueDouble), Constants.WINDOW_LENGTH_MIN, Constants.WINDOW_LENGTH_MAX));

                        editor.putLong(getString(R.string.shared_preferences_settings_window_length), Double.doubleToLongBits(newValueDouble));
                        editor.apply();

                        SettingsActivity.windowLengthChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                }
            }

            return false;
        }
    };

    private Preference.OnPreferenceChangeListener onClassificationChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ListPreference listPreference = (ListPreference) preference;
            String key = listPreference.getKey();

            String valueString = listPreference.getValue();
            String newValueString = newValue.toString();

            SharedPreferences.Editor editor = preferences.edit();

            if (!newValueString.equals(valueString)) {
                if (key.equals(getString(R.string.settings_classifier_key))) {
                    listPreference.setValue(newValueString);
                    listPreference.setSummary(listPreference.getEntry());

                    editor.putString(getString(R.string.shared_preferences_settings_classifier_id), newValueString);
                    editor.apply();

                    Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                    return true;
                }
            }

            return false;
        }
    };

    private Preference.OnPreferenceChangeListener onArtificialNeuralNetworkNumberChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String key = editTextPreference.getKey();

            Integer valueInteger = Integer.parseInt(editTextPreference.getText());
            Integer newValueInteger = Integer.parseInt(newValue.toString());

            SharedPreferences.Editor editor = preferences.edit();

            if (newValueInteger != valueInteger) {
                if (key.equals(getString(R.string.settings_input_neurons_key))) {
                    if (newValueInteger >= Constants.INPUT_NEURONS_MIN && newValueInteger <= Constants.INPUT_NEURONS_MAX) {
                        editTextPreference.setText(String.valueOf(newValueInteger));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), String.valueOf(newValueInteger), Constants.INPUT_NEURONS_MIN, Constants.INPUT_NEURONS_MAX));

                        editor.putInt(getString(R.string.shared_preferences_settings_input_neurons), newValueInteger);
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(R.string.settings_hidden_layers_key)) {
                    if (newValueInteger >= Constants.HIDDEN_LAYERS_MIN && newValueInteger <= Constants.HIDDEN_LAYER_MAX) {
                        editTextPreference.setText(String.valueOf(newValueInteger));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), String.valueOf(newValueInteger), Constants.HIDDEN_LAYERS_MIN, Constants.HIDDEN_LAYER_MAX));

                        editor.putInt(getString(R.string.shared_preferences_settings_hidden_layers), newValueInteger);
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(R.string.settings_hidden_neurons_key)) {
                    if (newValueInteger >= Constants.HIDDEN_NEURONS_MIN && newValueInteger <= Constants.HIDDEN_NEURONS_MAX) {
                        editTextPreference.setText(String.valueOf(newValueInteger));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), String.valueOf(newValueInteger), Constants.HIDDEN_NEURONS_MIN, Constants.HIDDEN_NEURONS_MAX));

                        editor.putInt(getString(R.string.shared_preferences_settings_hidden_neurons), newValueInteger);
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(R.string.settings_input_divisor_key)) {
                    if (newValueInteger >= Constants.INPUT_DIVISOR_MIN && newValueInteger <= Constants.INPUT_DIVISOR_MAX) {
                        editTextPreference.setText(String.valueOf(newValueInteger));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), String.valueOf(newValueInteger), Constants.INPUT_DIVISOR_MIN, Constants.INPUT_DIVISOR_MAX));

                        editor.putInt(getString(R.string.shared_preferences_settings_input_divisor), newValueInteger);
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(R.string.settings_learning_iterations_key)) {
                    if (newValueInteger >= Constants.LEARNING_ITERATIONS_MIN && newValueInteger <= Constants.LEARNING_ITERATIONS_MAX) {
                        editTextPreference.setText(String.valueOf(newValueInteger));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), String.valueOf(newValueInteger), Constants.LEARNING_ITERATIONS_MIN, Constants.LEARNING_ITERATIONS_MAX));

                        editor.putInt(getString(R.string.shared_preferences_settings_learning_iterations), newValueInteger);
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                }
            }

            return false;
        }
    };

    private Preference.OnPreferenceChangeListener onArtificialNeuralNetworkNumberDecimalChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String key = editTextPreference.getKey();

            Double valueDouble = Double.parseDouble(editTextPreference.getText());
            Double newValueDouble = Double.parseDouble(newValue.toString());

            SharedPreferences.Editor editor = preferences.edit();

            if (newValueDouble != valueDouble) {
                if (key.equals(getString(R.string.settings_learning_constant_key))) {
                    if (newValueDouble >= Constants.LEARNING_CONSTANT_MIN && newValueDouble <= Constants.LEARNING_CONSTANT_MAX) {
                        editTextPreference.setText(String.valueOf(newValueDouble));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number_decimal), String.valueOf(newValueDouble), Constants.LEARNING_CONSTANT_MIN, Constants.LEARNING_CONSTANT_MAX));

                        editor.putLong(getString(R.string.shared_preferences_settings_learning_constant), Double.doubleToLongBits(newValueDouble));
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(getString(R.string.settings_desired_activity_weight_key))) {
                    if (newValueDouble >= Constants.DESIRED_ACTIVITY_WEIGHT_MIN && newValueDouble <= Constants.DESIRED_ACTIVITY_WEIGHT_MAX) {
                        editTextPreference.setText(String.valueOf(newValueDouble));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number_decimal), String.valueOf(newValueDouble), Constants.DESIRED_ACTIVITY_WEIGHT_MIN, Constants.DESIRED_ACTIVITY_WEIGHT_MAX));

                        editor.putLong(getString(R.string.shared_preferences_settings_desired_activity_weight), Double.doubleToLongBits(newValueDouble));
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                } else if (key.equals(getString(R.string.settings_undesired_activity_weight_key))) {
                    if (newValueDouble >= Constants.UNDESIRED_ACTIVITY_WEIGHT_MIN && newValueDouble <= Constants.UNDESIRED_ACTIVITY_WEIGHT_MAX) {
                        editTextPreference.setText(String.valueOf(newValueDouble));
                        editTextPreference.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number_decimal), String.valueOf(newValueDouble), Constants.UNDESIRED_ACTIVITY_WEIGHT_MIN, Constants.UNDESIRED_ACTIVITY_WEIGHT_MAX));

                        editor.putLong(getString(R.string.shared_preferences_settings_undesired_activity_weight), Double.doubleToLongBits(newValueDouble));
                        editor.apply();

                        SettingsActivity.artificialNeuralNetworkChanged = true;
                        SettingsActivity.fab.show();

                        Toast.makeText(context, getString(R.string.toast_settings), Toast.LENGTH_LONG).show();

                        return true;
                    } else {
                        Toast.makeText(context, getString(R.string.toast_requested), Toast.LENGTH_LONG).show();

                        return false;
                    }
                }
            }

            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        context = getContext();
        preferences = context.getSharedPreferences(getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE);

        etpChronometerTime = (EditTextPreference) findPreference(getString(R.string.settings_chronometer_time_key));
        etpDelayTime = (EditTextPreference) findPreference(getString(R.string.settings_delay_time_key));
        etpWindowLength = (EditTextPreference) findPreference(getString(R.string.settings_window_length_key));

        lpClassifier = (ListPreference) findPreference(getString(R.string.settings_classifier_key));

        etpInputNeurons = (EditTextPreference) findPreference(getString(R.string.settings_input_neurons_key));
        etpHiddenLayers = (EditTextPreference) findPreference(getString(R.string.settings_hidden_layers_key));
        etpHiddenNeurons = (EditTextPreference) findPreference(getString(R.string.settings_hidden_neurons_key));
        etpInputDivisor = (EditTextPreference) findPreference(getString(R.string.settings_input_divisor_key));
        etpLearningConstant = (EditTextPreference) findPreference(getString(R.string.settings_learning_constant_key));
        etpLearningIterations = (EditTextPreference) findPreference(getString(R.string.settings_learning_iterations_key));
        etpDesiredActivityWeight = (EditTextPreference) findPreference(getString(R.string.settings_desired_activity_weight_key));
        etpUndesiredActivityWeight = (EditTextPreference) findPreference(getString(R.string.settings_undesired_activity_weight_key));

        etpChronometerTime.setText(String.valueOf(Double.longBitsToDouble(preferences.getLong(getString(R.string.shared_preferences_settings_chronometer_time), Double.doubleToLongBits(Constants.DEFAULT_CHRONOMETER_TIME)))));
        etpChronometerTime.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_time), etpChronometerTime.getText(), Constants.CHRONOMETER_TIME_MIN, Constants.CHRONOMETER_TIME_MAX));
        etpChronometerTime.setOnPreferenceChangeListener(onTimeChangeListener);

        etpDelayTime.setText(String.valueOf(Double.longBitsToDouble(preferences.getLong(getString(R.string.shared_preferences_settings_delay_time), Double.doubleToLongBits(Constants.DEFAULT_DELAY_TIME)))));
        etpDelayTime.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_time), etpDelayTime.getText(), Constants.DELAY_TIME_MIN, Constants.DELAY_TIME_MAX));
        etpDelayTime.setOnPreferenceChangeListener(onTimeChangeListener);

        etpWindowLength.setText(String.valueOf(Double.longBitsToDouble(preferences.getLong(getString(R.string.shared_preferences_settings_window_length), Double.doubleToLongBits(Constants.DEFAULT_WINDOW_LENGTH)))));
        etpWindowLength.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_time), etpWindowLength.getText(), Constants.WINDOW_LENGTH_MIN, Constants.WINDOW_LENGTH_MAX));
        etpWindowLength.setOnPreferenceChangeListener(onTimeChangeListener);

        lpClassifier.setValue(preferences.getString(getString(R.string.shared_preferences_settings_classifier_id), Constants.DEFAULT_CLASSIFIER_ID));
        lpClassifier.setSummary(lpClassifier.getEntry());
        lpClassifier.setOnPreferenceChangeListener(onClassificationChangeListener);

        etpInputNeurons.setText(String.valueOf(preferences.getInt(getString(R.string.shared_preferences_settings_input_neurons), Constants.DEFAULT_INPUT_NEURONS)));
        etpInputNeurons.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), etpInputNeurons.getText(), Constants.INPUT_NEURONS_MIN, Constants.INPUT_NEURONS_MAX));
        etpInputNeurons.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberChangeListener);

        etpHiddenLayers.setText(String.valueOf(preferences.getInt(getString(R.string.shared_preferences_settings_hidden_layers), Constants.DEFAULT_HIDDEN_LAYERS)));
        etpHiddenLayers.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), etpHiddenLayers.getText(), Constants.HIDDEN_LAYERS_MIN, Constants.HIDDEN_LAYER_MAX));
        etpHiddenLayers.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberChangeListener);

        etpHiddenNeurons.setText(String.valueOf(preferences.getInt(getString(R.string.shared_preferences_settings_hidden_neurons), Constants.DEFAULT_HIDDEN_NEURONS)));
        etpHiddenNeurons.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), etpHiddenNeurons.getText(), Constants.HIDDEN_NEURONS_MIN, Constants.HIDDEN_NEURONS_MAX));
        etpHiddenNeurons.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberChangeListener);

        etpInputDivisor.setText(String.valueOf(preferences.getInt(getString(R.string.shared_preferences_settings_input_divisor), Constants.DEFAULT_INPUT_DIVISOR)));
        etpInputDivisor.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), etpInputDivisor.getText(), Constants.INPUT_DIVISOR_MIN, Constants.INPUT_DIVISOR_MAX));
        etpInputDivisor.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberChangeListener);

        etpLearningConstant.setText(String.valueOf(Double.longBitsToDouble(preferences.getLong(getString(R.string.shared_preferences_settings_learning_constant), Double.doubleToLongBits(Constants.DEFAULT_LEARNING_CONSTANT)))));
        etpLearningConstant.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number_decimal), etpLearningConstant.getText(), Constants.LEARNING_CONSTANT_MIN, Constants.LEARNING_CONSTANT_MAX));
        etpLearningConstant.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberDecimalChangeListener);

        etpLearningIterations.setText(String.valueOf(preferences.getInt(getString(R.string.shared_preferences_settings_learning_iterations), Constants.DEFAULT_LEARNING_ITERATIONS)));
        etpLearningIterations.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number), etpLearningIterations.getText(), Constants.LEARNING_ITERATIONS_MIN, Constants.LEARNING_ITERATIONS_MAX));
        etpLearningIterations.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberChangeListener);

        etpDesiredActivityWeight.setText(String.valueOf(Double.longBitsToDouble(preferences.getLong(getString(R.string.shared_preferences_settings_desired_activity_weight), Double.doubleToLongBits(Constants.DEFAULT_DESIRED_ACTIVITY_WEIGHT)))));
        etpDesiredActivityWeight.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number_decimal), etpDesiredActivityWeight.getText(), Constants.DESIRED_ACTIVITY_WEIGHT_MIN, Constants.DESIRED_ACTIVITY_WEIGHT_MAX));
        etpDesiredActivityWeight.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberDecimalChangeListener);

        etpUndesiredActivityWeight.setText(String.valueOf(Double.longBitsToDouble(preferences.getLong(getString(R.string.shared_preferences_settings_undesired_activity_weight), Double.doubleToLongBits(Constants.DEFAULT_UNDESIRED_ACTIVITY_WEIGHT)))));
        etpUndesiredActivityWeight.setSummary(String.format(getString(R.string.formatter_edit_text_preference_summary_artificial_neural_network_number_decimal), etpUndesiredActivityWeight.getText(), Constants.UNDESIRED_ACTIVITY_WEIGHT_MIN, Constants.UNDESIRED_ACTIVITY_WEIGHT_MAX));
        etpUndesiredActivityWeight.setOnPreferenceChangeListener(onArtificialNeuralNetworkNumberDecimalChangeListener);
    }
}
