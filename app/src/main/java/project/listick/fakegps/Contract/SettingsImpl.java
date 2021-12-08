package project.listick.fakegps.Contract;

import project.listick.fakegps.Interfaces.EditTextDialogImpl;

/*
 * Created by LittleAngry on 26.12.18 (macOS 10.12)
 * */
public interface SettingsImpl {
    interface Presenter {
        void onActivityLoad();
        void onAccuracyChanged(int accuracy);
        void onBrakeAtTurning(boolean isChecked);
        void onGpsUpdatesDelay();
        void onAccuracy();
        void onGpsUpdatesDelayChanged(float timeInSeconds);
        void onUnitSpinner(int position);
        void onSystemAppStatus();
        void onKeepAtCenter(boolean isChecked);
        void onAbout();
        void onLocationDeviation(boolean isChecked);
        void onMapTilesChanged(int position);
        void setAutoAltitude(boolean isChecked);
        void saveTrafficSide(int side);
    }

    interface UI {
        void setAccuracy(int accuracy);
        void getBrakeAtTurning(boolean mode);
        void getUpdatesDelay(float timeInSeconds);
        void setStandartUnit(int unit);
        void getMapTileProvider(int tileProvider);
        void setAppSystemStatus(boolean isSystem);
        void setKeepAtCenter(boolean keepAtCenter);
        void setUnitSpinner(int unit);
        void showEditTextDialog(String title, String text, EditTextDialogImpl callback);
        void setLocationDeviation(boolean isChecked);
        void getAutoAltitude(boolean enabled);
        void showAlreadySpoofing(boolean isSpoofing);
        void setTrafficSide(int side);
    }

}
