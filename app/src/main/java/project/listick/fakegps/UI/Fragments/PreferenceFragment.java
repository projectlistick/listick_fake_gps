package project.listick.fakegps.UI.Fragments;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.R;
import rikka.preference.SimpleMenuPreference;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs);

        EditTextPreference accuracySettingsPref = findPreference("accuracy_settings");
        if (accuracySettingsPref != null) {
            accuracySettingsPref.setSummary(AppPreferences.getAccuracy(requireContext()) + " m.");
            accuracySettingsPref.setDialogMessage(R.string.enter_accuracy_value);
            accuracySettingsPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));
                editText.addTextChangedListener(getCommaReplacerTextWatcher(editText));
            });


            accuracySettingsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    Float.parseFloat((String) newValue);
                    preference.setSummary(newValue + " m.");
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), R.string.enter_valid_value, Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            });

        }
        EditTextPreference updatesDelayPref = findPreference("gps_updates_delay");
        if (updatesDelayPref != null) {
            updatesDelayPref.setSummary(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("gps_updates_delay", "1") + " s.");

            updatesDelayPref.setDialogMessage(R.string.enter_updates_time_value);
            updatesDelayPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));
                editText.addTextChangedListener(getCommaReplacerTextWatcher(editText));
            });

            updatesDelayPref.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    Float.parseFloat((String) newValue);
                    preference.setSummary(newValue + " s.");
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), R.string.enter_valid_value, Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            });
        }

        SimpleMenuPreference standartUnit = findPreference("standart_unit");
        if (standartUnit != null) {
            int stdUnit = AppPreferences.getStandartUnit(requireContext());

            Resources res = getResources();
            TypedArray array = res.obtainTypedArray(R.array.unitList);

            standartUnit.setSummary(array.getString(stdUnit));

            standartUnit.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(array.getString(Integer.parseInt(newValue.toString())));
                return true;
            });
        }

        SimpleMenuPreference tileProvider = findPreference("default_tile_provider");
        if (tileProvider != null) {
            int defTileProvider = AppPreferences.getMapTileProvider(requireContext());

            Resources res = getResources();
            TypedArray array = res.obtainTypedArray(R.array.map_tiles);

            tileProvider.setSummary(array.getString(defTileProvider));

            tileProvider.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(array.getString(Integer.parseInt(newValue.toString())));
                return true;
            });
        }

        SimpleMenuPreference trafficSide = findPreference("traffic_side");
        if (trafficSide != null) {
            int defSide = AppPreferences.getTrafficSide(requireContext());

            Resources res = getResources();
            TypedArray array = res.obtainTypedArray(R.array.trafficSide);

            trafficSide.setSummary(array.getString(defSide));

            trafficSide.setOnPreferenceChangeListener((preference, newValue) -> {
                preference.setSummary(array.getString(Integer.parseInt(newValue.toString())));
                return true;
            });
        }
    }


    private TextWatcher getCommaReplacerTextWatcher(EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.contains(",")) {
                    editText.setText(text.replace(",", "."));
                    editText.setSelection(editText.getText().length());
                }
            }
        };
    }
}
