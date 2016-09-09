package mlipa.move.client;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {
    private final Integer INPUT_NEURONS_MIN = 1;
    private final Integer INPUT_NEURONS_MAX = 36;
    private final Integer HIDDEN_LAYERS_MIN = 1;
    private final Integer HIDDEN_LAYER_MAX = 5;
    private final Integer HIDDEN_NEURONS_MIN = 1;
    private final Integer HIDDEN_NEURONS_MAX = 36;
    private final Integer LEARNING_ITERATIONS_MIN = 100;
    private final Integer LEARNING_ITERATIONS_MAX = 1000000;
    private final Double DESIRED_ACTIVITY_WEIGHT_MIN = 0.0;
    private final Double DESIRED_ACTIVITY_WEIGHT_MAX = 1.0;
    private final Double UNDESIRED_ACTIVITY_WEIGHT_MIN = 0.0;
    private final Double UNDESIRED_ACTIVITY_WEIGHT_MAX = 1.0;
    private final Integer TIME_WINDOW_LENGTH_MIN = 2500;
    private final Integer TIME_WINDOW_LENGTH_MAX = 5000;

    private Context context;
    private SharedPreferences sharedPreferences;

    private ListPreference classifier;
    private EditTextPreference inputNeurons;
    private EditTextPreference hiddenLayers;
    private EditTextPreference hiddenNeurons;
    private EditTextPreference learningIterations;
    private EditTextPreference desiredActivityWeight;
    private EditTextPreference undesiredActivityWeight;
    private EditTextPreference timeWindowLength;

    private Preference.OnPreferenceChangeListener listPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ListPreference classifier = (ListPreference) preference;
            String newValueString = newValue.toString();
            String message = getString(R.string.success_toast);

            classifier.setValue(newValueString);
            classifier.setSummary(classifier.getEntry());

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            return true;
        }
    };

    private Preference.OnPreferenceChangeListener numberEditTextPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String newValueString = newValue.toString();
            Integer newValueInteger = Integer.parseInt(newValueString);
            String message = getString(R.string.success_toast);

            editTextPreference.setText(newValueString);
            editTextPreference.setSummary(editTextPreference.getText());

            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (editTextPreference.getKey().equals(getString(R.string.input_neurons_key))) {
                if (newValueInteger >= INPUT_NEURONS_MIN && newValueInteger <= INPUT_NEURONS_MAX) {
                    editor.putInt(getString(R.string.input_neurons_shared_preferences_key), newValueInteger);
                } else {
                    message = getString(R.string.requested_range_message);
                }
            } else if (editTextPreference.getKey().equals(getString(R.string.hidden_layers_key))) {
                if (newValueInteger >= HIDDEN_LAYERS_MIN && newValueInteger <= HIDDEN_LAYER_MAX) {
                    editor.putInt(getString(R.string.hidden_layers_shared_preferences_key), newValueInteger);
                } else {
                    message = getString(R.string.requested_range_message);
                }
            } else if (editTextPreference.getKey().equals(getString(R.string.hidden_neurons_key))) {
                if (newValueInteger >= HIDDEN_NEURONS_MIN && newValueInteger <= HIDDEN_NEURONS_MAX) {
                    editor.putInt(getString(R.string.hidden_neurons_shared_preferences_key), newValueInteger);
                } else {
                    message = getString(R.string.requested_range_message);
                }
            } else if (editTextPreference.getKey().equals(getString(R.string.learning_iterations_key))) {
                if (newValueInteger >= LEARNING_ITERATIONS_MIN && newValueInteger <= LEARNING_ITERATIONS_MAX) {
                    editor.putInt(getString(R.string.learning_iterations_shared_preferences_key), newValueInteger);
                } else {
                    message = getString(R.string.requested_range_message);
                }
            } else if (editTextPreference.getKey().equals(getString(R.string.time_window_length_key))) {
                if (newValueInteger >= TIME_WINDOW_LENGTH_MIN && newValueInteger <= TIME_WINDOW_LENGTH_MAX) {
                    editor.putInt(getString(R.string.time_window_length_shared_preferences_key), newValueInteger);
                } else {
                    message = getString(R.string.requested_range_message);
                }
            }

            editor.apply();

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            showReLearnAlertDialog();

            return true;
        }
    };

    private Preference.OnPreferenceChangeListener numberDecimalEditTextPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String newValueString = newValue.toString();
            Double newValueDouble = Double.parseDouble(newValueString);
            String message = getString(R.string.success_toast);

            editTextPreference.setText(newValueString);
            editTextPreference.setSummary(editTextPreference.getText());

            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (editTextPreference.getKey().equals(getString(R.string.desired_activity_weight_key))) {
                if (newValueDouble >= DESIRED_ACTIVITY_WEIGHT_MIN && newValueDouble <= DESIRED_ACTIVITY_WEIGHT_MAX) {
                    editor.putLong(getString(R.string.desired_activity_weight_shared_preferences_key), Double.doubleToRawLongBits(newValueDouble));
                } else {
                    message = getString(R.string.requested_range_message);

                }
            } else if (editTextPreference.getKey().equals(getString(R.string.undesired_activity_weight_key))) {
                if (newValueDouble >= UNDESIRED_ACTIVITY_WEIGHT_MIN && newValueDouble <= UNDESIRED_ACTIVITY_WEIGHT_MAX) {
                    editor.putLong(getString(R.string.undesired_activity_weight_shared_preferences_key), Double.doubleToRawLongBits(newValueDouble));
                } else {
                    message = getString(R.string.requested_range_message);
                }
            }

            editor.apply();

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            showReLearnAlertDialog();

            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        context = getContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.settings_shared_preferences_key), Context.MODE_PRIVATE);

        classifier = (ListPreference) findPreference(getString(R.string.classifier_key));
        inputNeurons = (EditTextPreference) findPreference(getString(R.string.input_neurons_key));
        hiddenLayers = (EditTextPreference) findPreference(getString(R.string.hidden_layers_key));
        hiddenNeurons = (EditTextPreference) findPreference(getString(R.string.hidden_neurons_key));
        learningIterations = (EditTextPreference) findPreference(getString(R.string.learning_iterations_key));
        desiredActivityWeight = (EditTextPreference) findPreference(getString(R.string.desired_activity_weight_key));
        undesiredActivityWeight = (EditTextPreference) findPreference(getString(R.string.undesired_activity_weight_key));
        timeWindowLength = (EditTextPreference) findPreference(getString(R.string.time_window_length_key));

        classifier.setValue(sharedPreferences.getString(getString(R.string.classifier_id_shared_preferences_key), SplashActivity.CLASSIFIER_ID));
        classifier.setSummary(classifier.getEntry());

        inputNeurons.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.input_neurons_shared_preferences_key), SplashActivity.INPUT_NEURONS)));
        inputNeurons.setSummary(inputNeurons.getText());
        inputNeurons.setDialogMessage(getString(R.string.range) + " " + String.valueOf(INPUT_NEURONS_MIN) + " - " + String.valueOf(INPUT_NEURONS_MAX));

        hiddenLayers.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.hidden_layers_shared_preferences_key), SplashActivity.HIDDEN_LAYERS)));
        hiddenLayers.setSummary(hiddenLayers.getText());
        hiddenLayers.setDialogMessage(getString(R.string.range) + " " + String.valueOf(HIDDEN_LAYERS_MIN) + " - " + String.valueOf(HIDDEN_LAYER_MAX));

        hiddenNeurons.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.hidden_neurons_shared_preferences_key), SplashActivity.HIDDEN_NEURONS)));
        hiddenNeurons.setSummary(hiddenNeurons.getText());
        hiddenNeurons.setDialogMessage(getString(R.string.range) + " " + String.valueOf(HIDDEN_NEURONS_MIN) + " - " + String.valueOf(HIDDEN_NEURONS_MAX));

        learningIterations.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.learning_iterations_shared_preferences_key), SplashActivity.LEARNING_ITERATIONS)));
        learningIterations.setSummary(learningIterations.getText());
        learningIterations.setDialogMessage(getString(R.string.range) + " " + String.valueOf(LEARNING_ITERATIONS_MIN) + " - " + String.valueOf(LEARNING_ITERATIONS_MAX));

        desiredActivityWeight.setText(String.valueOf(Double.longBitsToDouble(sharedPreferences.getLong(getString(R.string.desired_activity_weight_shared_preferences_key), Double.doubleToRawLongBits(SplashActivity.DESIRED_ACTIVITY_WEIGHT)))));
        desiredActivityWeight.setSummary(desiredActivityWeight.getText());
        desiredActivityWeight.setDialogMessage(getString(R.string.range) + " " + String.valueOf(DESIRED_ACTIVITY_WEIGHT_MIN) + " - " + String.valueOf(DESIRED_ACTIVITY_WEIGHT_MAX));

        undesiredActivityWeight.setText(String.valueOf(Double.longBitsToDouble(sharedPreferences.getLong(getString(R.string.undesired_activity_weight_shared_preferences_key), Double.doubleToRawLongBits(SplashActivity.UNDESIRED_ACTIVITY_WEIGHT)))));
        undesiredActivityWeight.setSummary(undesiredActivityWeight.getText());
        undesiredActivityWeight.setDialogMessage(getString(R.string.range) + " " + String.valueOf(UNDESIRED_ACTIVITY_WEIGHT_MIN) + " - " + String.valueOf(UNDESIRED_ACTIVITY_WEIGHT_MAX));

        timeWindowLength.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.time_window_length_shared_preferences_key), SplashActivity.TIME_WINDOW_LENGTH)));
        timeWindowLength.setSummary(timeWindowLength.getText());
        timeWindowLength.setDialogMessage(getString(R.string.range) + " " + String.valueOf(TIME_WINDOW_LENGTH_MIN) + " - " + String.valueOf(TIME_WINDOW_LENGTH_MAX) + ", " + getString(R.string.time_unit));

        classifier.setOnPreferenceChangeListener(listPreferenceChangeListener);
        inputNeurons.setOnPreferenceChangeListener(numberEditTextPreferenceChangeListener);
        hiddenLayers.setOnPreferenceChangeListener(numberEditTextPreferenceChangeListener);
        hiddenNeurons.setOnPreferenceChangeListener(numberEditTextPreferenceChangeListener);
        learningIterations.setOnPreferenceChangeListener(numberEditTextPreferenceChangeListener);
        desiredActivityWeight.setOnPreferenceChangeListener(numberDecimalEditTextPreferenceChangeListener);
        undesiredActivityWeight.setOnPreferenceChangeListener(numberDecimalEditTextPreferenceChangeListener);
        timeWindowLength.setOnPreferenceChangeListener(numberEditTextPreferenceChangeListener);
    }

    private void showReLearnAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(R.string.re_learn);
        builder.setMessage(R.string.re_learn_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SettingsActivity.fab.callOnClick();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }
}
