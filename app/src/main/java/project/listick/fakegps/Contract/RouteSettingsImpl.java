package project.listick.fakegps.Contract;

import android.content.SharedPreferences;

/*
 * Created by LittleAngry on 09.01.19 (macOS 10.12)
 * */
public interface RouteSettingsImpl {
    interface Presenter {
        void onActivityLoad();
        void onCancelClick();
        void onContinueClick(int speed, int difference, float elevation, float elevationDiff, boolean isClosedRoute);
    }

    interface UI {
        void getSpeed(int speed);
        void getSpeedDifference(int difference);
        void pushDifferenceError();
        void getElevation(float elevation, float difference);
        void startAltitudeDetection();
        void stopAltitudeDetection();
        void onAltitudeDetermined(boolean success, boolean isRoute);
        void setFixedMode();
        void addMoreRoute();
        int getTimerMinutes();
        int getTimerSeconds();
    }
}
