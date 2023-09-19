package project.listick.fakegps.Presenter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import project.listick.fakegps.API.LFGSimpleApi;
import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.Contract.RouteSettingsImpl;
import project.listick.fakegps.FakeGPSApplication;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.MultipleRoutesInfo;
import project.listick.fakegps.RouteManager;
import project.listick.fakegps.Services.FixedSpooferService;
import project.listick.fakegps.Services.ISpooferService;
import project.listick.fakegps.Services.RouteSpooferService;
import project.listick.fakegps.SpoofingPlaceInfo;
import project.listick.fakegps.UI.CaptchaActivity;
import project.listick.fakegps.UI.SettingsActivity;

/*
 * Created by LittleAngry on 09.01.19 (macOS 10.12)
 * */
public class RouteSettingsPresenter implements RouteSettingsImpl.Presenter {

    public static final String TAG = RouteSettingsPresenter.class.getName();

    public static final int ANOTHER_ROUTE_ADDED = 7;

    public static final String ELEVATION = "elevation";
    public static final String ELEVATION_DIFF = "elevation_diff";
    public static final String SPEED_DIFF = "difference";
    public static final String IS_ROUTE = "is_route";
    public static final String ADD_MORE_ROUTE = "add_more_route";

    private final RouteSettingsImpl.UI mUserInterface;
    private final Activity mActivity;
    private final SharedPreferences mSettingsPreferences;

    private float elevation;
    private float elevationDiff;
    private boolean isClosedRoute;

    private final boolean mIsRoute;
    private final boolean mAddMoreRoutes;

    public static ISpooferService sService;
    public static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            sService = ISpooferService.Stub.asInterface(binder);
            try {
                sService.attachRoutes(RouteManager.routes);
            } catch (Exception e) {
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sService = null;
        }
    };

    public static void unbindService() {
        try {
            if (sService != null)
                FakeGPSApplication.getAppContext().unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, null, e);
        }
    }

    public RouteSettingsPresenter(RouteSettingsImpl.UI userInterface) {
        this.mUserInterface = userInterface;
        this.mActivity = (Activity) userInterface;
        this.mSettingsPreferences = mActivity.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE);

        Intent intent = mActivity.getIntent();
        mIsRoute = intent.getBooleanExtra(IS_ROUTE, false);
        mAddMoreRoutes = intent.getBooleanExtra(ADD_MORE_ROUTE, false);
        if (!mIsRoute)
            mUserInterface.setFixedMode();

        if (mAddMoreRoutes) {
            mActivity.setResult(Activity.RESULT_CANCELED);
            mUserInterface.addMoreRoute();
        }
    }

    @Override
    public void onActivityLoad() {
        prepareUi();

        if (AppPreferences.getAutoAltitude(mActivity)) {
            findElevation(null);
        } else {
            setElevation();
        }
    }

    @Override
    public void onChallengePassed(String challengeResult) {
        findElevation(challengeResult);
    }

    @Override
    public void onCancelClick() {
        mActivity.finish();
    }

    @Override
    public void onContinueClick(int speed, int difference, float elevation, float elevationDiff, boolean isClosedRoute) {
        if (!mAddMoreRoutes) {
            saveSpeedSettings(speed, difference);
            saveElevation(elevation, elevationDiff);
            this.isClosedRoute = isClosedRoute;
        }
        if (checkDifference(speed, difference))
            startMocking();
    }


    @Override
    public void setElevation() {
        elevation = mSettingsPreferences.getFloat(ELEVATION, 120);
        elevationDiff = mSettingsPreferences.getFloat(ELEVATION_DIFF, 2);
        mUserInterface.getElevation(elevation, elevationDiff);
    }

    public void saveElevation(float elevation, float difference) {
        mSettingsPreferences.edit().putFloat(ELEVATION, elevation)
                .putFloat(ELEVATION_DIFF, difference).apply();
    }


    private void startMocking() {
        Intent intent = mActivity.getIntent();

        int speed = mSettingsPreferences.getInt(ListickApp.SPEED, 60);
        int speedDiff = mSettingsPreferences.getInt(ListickApp.SPEED_DIFFERENCE, 0);
        int trafficSide = AppPreferences.getTrafficSide(mActivity);

        int originMinutes = mUserInterface.getOriginTimerMinutes();
        int originSeconds = mUserInterface.getOriginTimerSeconds();
        int originTimeout = (originSeconds + originMinutes * 60) * 1000; // (convert to seconds) to ms

        if (mAddMoreRoutes) {
            MultipleRoutesInfo info = RouteManager.routes.get(RouteManager.getLatestElement());

            info.setSpeed(speed);
            info.setSpeedDiff(speedDiff);


            info.setStartingPauseTime(originTimeout);

            info.setElevation(elevation);
            info.setElevationDiff(elevationDiff);

            mActivity.setResult(Activity.RESULT_OK);
            mActivity.finish();
            return;
        }

        int dest_minutes = mUserInterface.getDestTimerMinutes();
        int dest_seconds = mUserInterface.getDestTimerSeconds();
        int destTimeout = (dest_seconds + dest_minutes * 60) * 1000; // (convert to seconds) to ms

        if (mIsRoute) {
            double distance = intent.getDoubleExtra(ListickApp.DISTANCE, Double.NaN);

            float accuracy = AppPreferences.getAccuracy(mActivity);
            int updatesDelay = AppPreferences.getUpdatesDelay(mActivity);
            boolean deviation = AppPreferences.getLocationError(mActivity);
            int defaultUnit = AppPreferences.getStandartUnit(mActivity);
            boolean brakeAtTurning = AppPreferences.getBrakeAtTurning(mActivity);

            RouteManager.routes.get(0).setSpeed(speed);
            RouteManager.routes.get(0).setSpeedDiff(speedDiff);
            RouteManager.routes.get(0).setElevation(elevation);
            RouteManager.routes.get(0).setElevationDiff(elevationDiff);

            Intent i = new Intent(mActivity, RouteSpooferService.class)
                    .putExtra(ListickApp.SPEED, speed)
                    .putExtra(ListickApp.DISTANCE, distance)
                    .putExtra(ELEVATION, elevation)
                    .putExtra(ELEVATION_DIFF, elevationDiff)
                    .putExtra(SpoofingPlaceInfo.CLOSED_ROUTE_MOTION_INVERT, isClosedRoute)
                    .putExtra(SPEED_DIFF, speedDiff)
                    .putExtra("origin_timeout", originTimeout)
                    .putExtra("dest_timeout", destTimeout)
                    .putExtra(AppPreferences.TRAFFIC_SIDE, trafficSide)
                    .putExtra(RouteSpooferService.KEY_ACCURACY, accuracy)
                    .putExtra(RouteSpooferService.KEY_DEVIATION, deviation)
                    .putExtra(RouteSpooferService.KEY_DEFAULT_UNIT, defaultUnit)
                    .putExtra(RouteSpooferService.KEY_BRAKE_AT_TURINING, brakeAtTurning)
                    .putExtra(RouteSpooferService.KEY_UPDATES_DELAY, updatesDelay);

            FakeGPSApplication.getAppContext().startService(i);
            FakeGPSApplication.getAppContext().bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);

        } else {
            double latitude = intent.getDoubleExtra(ListickApp.LATITUDE, Double.NaN);
            double longitude = intent.getDoubleExtra(ListickApp.LONGITUDE, Double.NaN);

            mActivity.startService(new Intent(mActivity, FixedSpooferService.class)
                    .putExtra(ListickApp.LATITUDE, latitude)
                    .putExtra(ListickApp.LONGITUDE, longitude)
                    .putExtra(ELEVATION, elevation)
                    .putExtra("origin_timeout", originTimeout)
                    .putExtra("dest_timeout", destTimeout)
                    .putExtra(ELEVATION_DIFF, elevationDiff));
        }
        mActivity.setResult(Activity.RESULT_OK);
        mActivity.finish();
    }


    private boolean checkDifference(int speed, int difference) {
        if (difference >= speed)
            mUserInterface.pushDifferenceError();

        return !(difference >= speed);
    }

    private void findElevation(String challengeResult) {
        if (RouteManager.routes != null
                && RouteManager.routes.size() > 0
                && RouteManager.routes.get(0).getRoute() != null
                && !RouteManager.routes.isEmpty()
                && !RouteManager.routes.get(0).getRoute().isEmpty()) {
            elevation = (float) RouteManager.routes.get(0).getRoute().get(0).getAltitude();
            if (elevation == 0.0d) {
                mUserInterface.getElevation(elevation, elevationDiff);
                mUserInterface.onAltitudeDetermined(false, true);
                return;
            }
            mUserInterface.getElevation(elevation, elevationDiff);
            mUserInterface.onAltitudeDetermined(true, true);
            return;
        }

        Intent intent = mActivity.getIntent();
        double latitude = intent.getDoubleExtra(ListickApp.LATITUDE, Double.NaN);
        double longitude = intent.getDoubleExtra(ListickApp.LONGITUDE, Double.NaN);
        elevationDiff = mSettingsPreferences.getFloat(ELEVATION_DIFF, 2);
        mUserInterface.startAltitudeDetection();

        LFGSimpleApi.Elevation service = new LFGSimpleApi.Elevation(mActivity.getCacheDir());
        service.getElevation(latitude, longitude, challengeResult, new LFGSimpleApi.Elevation.ElevationCallback() {
            @Override
            public void onRequestSuccess(float altitude) {
                elevation = altitude;
                mActivity.runOnUiThread(() -> {
                    mUserInterface.stopAltitudeDetection();
                    mUserInterface.getElevation(altitude, elevationDiff);
                    mUserInterface.onAltitudeDetermined(true, false);
                });
            }

            @Override
            public void onCaptchaResult() {
                mActivity.startActivityForResult(new Intent(mActivity, CaptchaActivity.class), CaptchaActivity.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onRequestError() {
                mActivity.runOnUiThread(() -> {
                    mUserInterface.stopAltitudeDetection();
                    mUserInterface.onAltitudeDetermined(false, false);
                    setElevation();
                });
            }
        });


    }


    public void saveSpeedSettings(int speed, int difference) {
        mSettingsPreferences.edit().putInt(ListickApp.SPEED, speed).putInt(ListickApp.SPEED_DIFFERENCE, difference).apply();
    }

    public void prepareUi() {
        mUserInterface.getSpeed(mSettingsPreferences.getInt(ListickApp.SPEED, 60));
        mUserInterface.getSpeedDifference(mSettingsPreferences.getInt(ListickApp.SPEED_DIFFERENCE, 10));
    }


}
