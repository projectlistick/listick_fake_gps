package project.listick.fakegps.Contract;

/*
 * Created by LittleAngry on 09.01.19 (macOS 10.12)
 * */
public interface RouteSettingsImpl {
    interface Presenter {
        void setElevation();
        void onActivityLoad();
        void onChallengePassed(String challengeResult);
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
        int getOriginTimerMinutes();
        int getOriginTimerSeconds();
        int getDestTimerMinutes();
        int getDestTimerSeconds();
    }
}
