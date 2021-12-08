package project.listick.fakegps.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import project.listick.fakegps.Contract.SettingsImpl;
import project.listick.fakegps.Interfaces.EditTextDialogImpl;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.Presenter.SettingsPresenter;
import project.listick.fakegps.R;

public class SettingsActivity extends Activity implements SettingsImpl.UI {
    public static final String FAKEGPS_SETTINGS = "fakegps_settings";

    private SettingsPresenter presenter;
    private CheckBox brakeAtTurning;
    private CheckBox keepAtCenter;
    private CheckBox locationError;
    private CheckBox autoAltitude;
    private TextView settingsOfAccuracy;
    private TextView gpsUpdatesDelay;
    private TextView systemAppToggle;
    private Spinner unitSpinner;
    private Spinner mapTiles;
    private Spinner trafficSide;
    private View mAlreadySpoofing;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        brakeAtTurning = findViewById(R.id.brake_at_turning);
        locationError = findViewById(R.id.location_error);
        autoAltitude = findViewById(R.id.auto_altitude);
        keepAtCenter = findViewById(R.id.current_location_center);
        settingsOfAccuracy = findViewById(R.id.settings_of_accuracy);
        gpsUpdatesDelay = findViewById(R.id.gps_updates_delay);
        systemAppToggle = findViewById(R.id.system_app_toggle);
        unitSpinner = findViewById(R.id.unitSpinner);
        mapTiles = findViewById(R.id.map_tiles);
        mAlreadySpoofing = findViewById(R.id.already_spoofing);
        trafficSide = findViewById(R.id.trafficSideSpinner);
        View about = findViewById(R.id.about);

        RelativeLayout unitHandler = findViewById(R.id.unit_container);
        RelativeLayout tileContainer = findViewById(R.id.map_tiles_container);

        unitSpinner.setClickable(false);
        mapTiles.setClickable(false);

        presenter = new SettingsPresenter(this, this);

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            systemAppToggle.setVisibility(View.GONE);
        }

        unitHandler.setOnClickListener(v -> unitSpinner.performClick());
        tileContainer.setOnClickListener(v -> mapTiles.performClick());

        about.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onAbout();
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.unitList, R.layout.spinner_item);
        ArrayAdapter tileAdapter = ArrayAdapter.createFromResource(this, R.array.map_tiles, R.layout.spinner_item);
        ArrayAdapter trafficAdapter = ArrayAdapter.createFromResource(this, R.array.trafficSide, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        tileAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        trafficAdapter.setDropDownViewResource(R.layout.spinner_dropdown);

        mapTiles.setAdapter(tileAdapter);
        unitSpinner.setAdapter(adapter);
        trafficSide.setAdapter(trafficAdapter);

        trafficSide.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.saveTrafficSide(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mapTiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == MapLoader.GOOGLE_MAPS_TILES && Preferences.getMapsApiKey(SettingsActivity.this) == null) { // google
                    EditText editText = new EditText(SettingsActivity.this);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
                    dialog.setMessage("Routes");
                    dialog.setTitle("Enter Maps API Key");
                    dialog.setView(editText);

                    Dialog.OnClickListener addInBookmarks = (DialogInterface dialogImpl, int which) -> {
                        String apiKey = editText.getText().toString();

                        if (apiKey.isEmpty()) {
                            PrettyToast.show(SettingsActivity.this, "Enter your API Key!", -1);
                            return;
                        }

                        SharedPreferences preferences = getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE);
                        preferences.edit().putString("maps_v2_apikey", apiKey).apply();
                        presenter.onMapTilesChanged(position);
                    };

                    dialog.setPositiveButton(R.string.okay, addInBookmarks);
                    dialog.show();
                    return;
                }

                presenter.onMapTilesChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                presenter.onUnitSpinner(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        locationError.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onLocationDeviation(isChecked));
        brakeAtTurning.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onBrakeAtTurning(isChecked));
        keepAtCenter.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onKeepAtCenter(isChecked));
        autoAltitude.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setAutoAltitude(isChecked));

        systemAppToggle.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onSystemAppStatus();
            }
        });

        settingsOfAccuracy.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onAccuracy();
            }
        });

        gpsUpdatesDelay.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onGpsUpdatesDelay();
            }
        });


        View back = findViewById(R.id.back);
        back.setOnClickListener(view -> finish());

        presenter.onActivityLoad();
    }

    @Override
    public void setAccuracy(int accuracy) {
        settingsOfAccuracy.setText(getString(R.string.settings_of_accuracy, String.valueOf(accuracy)));
    }


    @Override
    public void getBrakeAtTurning(boolean mode) {
        brakeAtTurning.setChecked(mode);
    }

    @Override
    public void getUpdatesDelay(float timeInMillis) {
        gpsUpdatesDelay.setText(getString(R.string.gps_updates_delay, String.valueOf(timeInMillis)));
    }

    @Override
    public void setAppSystemStatus(boolean isSystem) {
        if (isSystem)
            systemAppToggle.setText(getString(R.string.make_non_system_app));
        else
            systemAppToggle.setText(getString(R.string.make_system_app));
    }

    @Override
    public void setKeepAtCenter(boolean keepAtCenter) {
        this.keepAtCenter.setChecked(keepAtCenter);
    }

    @Override
    public void setUnitSpinner(int unit) {
        unitSpinner.setSelection(unit);
    }

    @Override
    public void showEditTextDialog(String title, String text, EditTextDialogImpl callback) {
        EditTextDialog dialog = new EditTextDialog(this, callback);
        dialog.show(title, text);
    }

    @Override
    public void setLocationDeviation(boolean isChecked) {
        locationError.setChecked(isChecked);
    }

    @Override
    public void getAutoAltitude(boolean enabled) {
        autoAltitude.setChecked(enabled);
    }

    @Override
    public void showAlreadySpoofing(boolean isSpoofing) {
        if (isSpoofing)
            mAlreadySpoofing.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTrafficSide(int side) {
        trafficSide.setSelection(side);
    }

    @Override
    public void setStandartUnit(int unit) {
        unitSpinner.setSelection(unit);
    }

    @Override
    public void getMapTileProvider(int tileProvider) {
        mapTiles.setSelection(tileProvider);
    }
}
