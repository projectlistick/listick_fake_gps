package project.listick.fakegps.Presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import project.listick.fakegps.AppUtils;
import project.listick.fakegps.Contract.SettingsImpl;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.MainServiceControl;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.R;
import project.listick.fakegps.UI.AboutActivity;
import project.listick.fakegps.UI.SettingsActivity;

/*
 * Created by LittleAngry on 26.12.18 (macOS 10.12)
 * */
public class SettingsPresenter implements SettingsImpl.Presenter {

    public static final String AUTO_ALTITUDE = "auto_altitude";
    public static final String LOCATION_ERROR = "location_error";

    private SettingsImpl.UI mUserInterface;
    private Context mContext;
    private SharedPreferences mPreferences;


    public SettingsPresenter(SettingsImpl.UI mUserInterface, Context context) {
        this.mUserInterface = mUserInterface;
        this.mContext = context;
        this.mPreferences = context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE);
    }


    @Override
    public void onActivityLoad() {
        prepareUi();
    }

    @Override
    public void onAccuracyChanged(int accuracy) {
        setAccuracy(accuracy);
    }

    @Override
    public void onBrakeAtTurning(boolean isChecked) {
        setBrakeAtTurning(isChecked);
    }

    @Override
    public void onGpsUpdatesDelay() {
        mUserInterface.showEditTextDialog(mContext.getString(R.string.gps_updates_delay_name), mContext.getString(R.string.enter_updates_time_value),
                (value) -> {
                    if (value.length() > 4 || value.isEmpty() || Float.parseFloat(value) > 5000) {
                        onGpsUpdatesDelay();
                    } else {
                        setUpdatesDelay(Float.parseFloat(value));
                    }

                });
    }

    @Override
    public void onAccuracy() {
        mUserInterface.showEditTextDialog(mContext.getString(R.string.accuracy_settings), mContext.getString(R.string.enter_accuracy_value),
                (value) -> {
                    if (value.length() > 3 || value.isEmpty() || !TextUtils.isDigitsOnly(value))
                        onAccuracy();
                    else
                        setAccuracy(Integer.parseInt(value));
                });
    }

    @Override
    public void onGpsUpdatesDelayChanged(float timeInSeconds) {
        setUpdatesDelay(timeInSeconds);
    }

    @Override
    public void onUnitSpinner(int position) {
        mPreferences.edit().putInt(ListickApp.STANDART_UNIT, position).apply();
    }

    @Override
    public void onSystemAppStatus() {
        boolean isSystemApp = PermissionManager.isSystemApp(mContext);
        if (isSystemApp)
            makeNonSystemApp();
        else
            makeSystemApp();
    }

    @Override
    public void onKeepAtCenter(boolean isChecked) {
        setKeepAtCenter(isChecked);
    }

    @Override
    public void onAbout() {
        mContext.startActivity(new Intent(mContext, AboutActivity.class));
    }

    @Override
    public void onLocationDeviation(boolean isChecked) {
        setLocationDeviation(isChecked);
    }

    @Override
    public void onMapTilesChanged(int position) {
        changeDefaultTileProvider(position);
        mUserInterface.getMapTileProvider(position);
    }

    @Override
    public void setAutoAltitude(boolean isChecked) {
        mPreferences.edit().putBoolean(AUTO_ALTITUDE, isChecked).apply();
    }

    @Override
    public void saveTrafficSide(int side) {
        mPreferences.edit().putInt(Preferences.TRAFFIC_SIDE, side).apply();
    }

    private void changeDefaultTileProvider(int position) {
        mPreferences.edit().putInt(MapLoader.DEFAULT_TILE_PROVIDER, position).apply();
    }

    public void setAccuracy(int accuracy) {
        mPreferences.edit().putInt(ListickApp.ACCURACY_SETTINGS, accuracy).apply();
        prepareUi();
    }

    public void setUpdatesDelay(float timeInSeconds) {
        mPreferences.edit().putFloat(ListickApp.GPS_UPDATES_DELAY, timeInSeconds).apply();
        prepareUi();
    }

    public void setBrakeAtTurning(boolean isChecked) {
        mPreferences.edit().putBoolean(ListickApp.BRAKE_AT_TURNING, isChecked).apply();
        prepareUi();
    }

    public void makeSystemApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.experemental_feature))
                .setMessage(mContext.getString(R.string.system_mover_description))
                .setCancelable(true)
                .setPositiveButton(mContext.getString(R.string.okay), (dialog, which) -> {
                    AppUtils appUtils = new AppUtils(mContext);
                    appUtils.makeSystemApp();
                })
                .setNegativeButton(mContext.getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void setKeepAtCenter(boolean isChecked) {
        mPreferences.edit().putBoolean(ListickApp.KEEP_AT_CENTER, isChecked).apply();
    }

    public void makeNonSystemApp() {
        AppUtils appUtils = new AppUtils(mContext);
        appUtils.makeNonSystemApp();
    }

    public void setLocationDeviation(boolean isChecked) {
        mPreferences.edit().putBoolean(LOCATION_ERROR, isChecked).apply();
    }

    public void prepareUi() {
        mUserInterface.setAccuracy(mPreferences.getInt(ListickApp.ACCURACY_SETTINGS, 10));
        mUserInterface.setLocationDeviation(mPreferences.getBoolean(LOCATION_ERROR, true));
        mUserInterface.setKeepAtCenter(mPreferences.getBoolean(ListickApp.KEEP_AT_CENTER, false));
        mUserInterface.getBrakeAtTurning(mPreferences.getBoolean(ListickApp.BRAKE_AT_TURNING, true));
        mUserInterface.getUpdatesDelay(mPreferences.getFloat(ListickApp.GPS_UPDATES_DELAY, 1));
        mUserInterface.getMapTileProvider(mPreferences.getInt(MapLoader.DEFAULT_TILE_PROVIDER, MapLoader.DEFAULT_TILES));

        mUserInterface.setStandartUnit(Preferences.getStandartUnit(mContext));
        mUserInterface.setUnitSpinner(Preferences.getStandartUnit(mContext));
        mUserInterface.setTrafficSide(Preferences.getTrafficSide(mContext));
        mUserInterface.getAutoAltitude(Preferences.getAutoAltitude(mContext));
        mUserInterface.setAppSystemStatus(PermissionManager.isSystemApp(mContext));

        boolean isAlreadySpoofing = MainServiceControl.isJoystickSpoofingRunning(mContext)
                || MainServiceControl.isRouteSpoofingServiceRunning(mContext) || MainServiceControl.isFixedSpoofingServiceRunning(mContext);
        mUserInterface.showAlreadySpoofing(isAlreadySpoofing);
    }

}
