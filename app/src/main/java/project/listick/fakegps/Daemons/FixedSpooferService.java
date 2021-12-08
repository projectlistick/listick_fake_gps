

package project.listick.fakegps.Daemons;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.osmdroid.util.GeoPoint;

import java.util.concurrent.ThreadLocalRandom;

import project.listick.fakegps.FusedLocationsProvider;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.LocationOperations;
import project.listick.fakegps.MainServiceControl;
import project.listick.fakegps.MockLocProvider;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.Randomizer;

public class FixedSpooferService extends Service {

    private FusedLocationsProvider mFusedLocationProvider;
    private Randomizer mRandomizer;
    private Handler mHandler;

    private int mUpdatesDelay;

    private float mBearing;
    private float mAccuracy;
    private float mElevation;
    private float mElevationDiff;

    private double mLatitude;
    private double mLongitude;

    private boolean mDeviation;
    private boolean isMockLocationsEnabled;
    private boolean isSystemApp;
    private BroadcastReceiver mUpdateService;

    private GeoPoint geoPoint;

    private float rAccuracy;
    private float rSpeed;
    private float rBearing;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainServiceControl.startServiceForeground(this);

        mHandler = new Handler();
        mRandomizer = new Randomizer();
        mFusedLocationProvider = new FusedLocationsProvider(this);
        isMockLocationsEnabled = PermissionManager.isMockLocationsEnabled(this);
        isSystemApp = PermissionManager.isSystemApp(this);
        MockLocProvider.initTestProvider();

        mBearing = (float) ThreadLocalRandom.current().nextDouble(10, 180);
        mAccuracy = Preferences.getAccuracy(this);
        mUpdatesDelay = Preferences.getUpdatesDelay(this);
        mDeviation = Preferences.getLocationError(this);

        mLatitude = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        mLongitude = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);
        mElevation = intent.getFloatExtra(RouteSettingsPresenter.ELEVATION, 197);
        mElevationDiff = intent.getFloatExtra(RouteSettingsPresenter.ELEVATION_DIFF, 2);

        geoPoint = new GeoPoint(mLatitude, mLongitude);

        if (mainStaticThread.getState() == Thread.State.NEW) {
            mainStaticThread.start();
        } else {
            mainStaticThread.interrupt();
            mHandler.removeCallbacks(mainStaticRunnable);
        }

        registerReceiver(mUpdateService, new IntentFilter(MainServiceControl.SERVICE_CONTROL_ACTION));
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainStaticThread.interrupt();
        if (mHandler != null && mainStaticThread != null)
            mHandler.removeCallbacks(mainStaticRunnable);

        stopForeground(true);
        try {
            unregisterReceiver(mUpdateService);
        } catch (IllegalArgumentException e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
        }
    }


    Runnable mainStaticRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDeviation)
                deviate();

            float rElevation;
            float rSpeed;

            { // Bearing
                float coefficient = (float) ThreadLocalRandom.current().nextDouble(-5, 5);
                rBearing = mBearing;
                rBearing += coefficient;
            }

            { // Altitude
                rElevation = mElevation;
                rElevation += ThreadLocalRandom.current().nextDouble(-1, +1);
            }

            {
                rSpeed = (float) ThreadLocalRandom.current().nextDouble(0, 0.3);
            }

            {
                float diff = (float) ThreadLocalRandom.current().nextDouble(-2, 2);
                rAccuracy = mAccuracy + diff;
            }

            setMockLocation(geoPoint, rAccuracy, rElevation, (float) (rBearing + Math.random()), rSpeed);

            mHandler.postDelayed(this, ThreadLocalRandom.current().nextLong((mUpdatesDelay > 200) ? mUpdatesDelay - 50 : mUpdatesDelay, mUpdatesDelay + 50));
        }

        private void deviate() {
            GeoPoint point = LocationOperations.deviate(geoPoint, 0.5f);
            geoPoint.setLatitude(point.getLatitude());
            geoPoint.setLongitude(point.getLongitude());
        }

        private void setMockLocation(GeoPoint location, float accuracy, float elevation, float bearing, float speed) {
            if (isMockLocationsEnabled) {
                MockLocProvider.setGpsProvider(location.getLatitude(), location.getLongitude(), bearing, speed, accuracy, elevation);
                MockLocProvider.setNetworkProvider(location.getLatitude(), location.getLongitude(), accuracy, bearing, elevation);
                Location fusedLocation = mFusedLocationProvider.build(location.getLatitude(), location.getLongitude(), accuracy, bearing, speed, elevation);
                mFusedLocationProvider.spoof(fusedLocation);
            } else if (isSystemApp) {
                MockLocProvider.reportLocation(location.getLatitude(), location.getLongitude(), accuracy, bearing, speed, elevation);
            }
        }
    };

    Thread mainStaticThread = new Thread(mainStaticRunnable);
}

/*
package project.listick.fakegps.Daemons;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.osmdroid.util.GeoPoint;

import java.util.concurrent.ThreadLocalRandom;

import project.listick.fakegps.FusedLocationsProvider;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.LocationOperations;
import project.listick.fakegps.MainServiceControl;
import project.listick.fakegps.MockLocProvider;
import project.listick.fakegps.MovementSimulator;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.Randomizer;

public class FixedSpooferService extends Service {

    public static final float ACCURACY_DIFF = 5;

    private FusedLocationsProvider mFusedLocationProvider;
    private Randomizer mRandomizer;
    private Handler mHandler;

    private int mUpdatesDelay;

    private float mBearing;
    private float mAccuracy;
    private float mElevation;
    private float mElevationDiff;

    private double mLatitude;
    private double mLongitude;

    private boolean mDeviation;
    private boolean isMockLocationsEnabled;
    private boolean isSystemApp;
    private boolean isPaused;
    private BroadcastReceiver mUpdateService;

    private MovementSimulator mSimulator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHandler = new Handler();
        mRandomizer = new Randomizer();
        mFusedLocationProvider = new FusedLocationsProvider(this);
        isMockLocationsEnabled = PermissionManager.isMockLocationsEnabled(this);
        isSystemApp = PermissionManager.isSystemApp(this);
        MockLocProvider.initTestProvider();

        mBearing = (float) ThreadLocalRandom.current().nextDouble(0, 180);
        mAccuracy = Preferences.getAccuracy(this);
        mUpdatesDelay = Preferences.getUpdatesDelay(this);
        mDeviation = Preferences.getLocationError(this);
        mSimulator = new MovementSimulator();

        mLatitude = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        mLongitude = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);
        mElevation = intent.getFloatExtra(RouteSettingsPresenter.ELEVATION, 197);
        mElevationDiff = intent.getFloatExtra(RouteSettingsPresenter.ELEVATION_DIFF, 4);
        mUpdateService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getExtras();
                if (data == null)
                    return;

                mLatitude = data.getDouble(ListickApp.LATITUDE, mLatitude);
                mLongitude = data.getDouble(ListickApp.LONGITUDE, mLongitude);

            }
        };

        if (mainStaticThread.getState() == Thread.State.NEW) {
            mainStaticThread.start();
        } else {
            mainStaticThread.interrupt();
            mHandler.removeCallbacks(mainStaticRunnable);
        }

        MainServiceControl.startServiceForeground(this);
        registerReceiver(mUpdateService, new IntentFilter(MainServiceControl.SERVICE_CONTROL_ACTION));
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainStaticThread.interrupt();
        if (mHandler != null && mainStaticThread != null)
            mHandler.removeCallbacks(mainStaticRunnable);

        stopForeground(true);
        try {
            unregisterReceiver(mUpdateService);
        } catch (IllegalArgumentException e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
        }
    }


    Runnable mainStaticRunnable = new Runnable() {
        @Override
        public void run() {
            float rSpeed = mRandomizer.getStaticSpeed(0, 0.2f);
            float rElevation = mRandomizer.getElevation(mElevation, mElevationDiff);
            float rAccuracy = mRandomizer.getAccuracy(mAccuracy, ACCURACY_DIFF);
            float rBearing = mRandomizer.getBearing(mBearing, 3);

            GeoPoint geoPoint = new GeoPoint(mLatitude, mLongitude);

            setMockLocation(geoPoint, rAccuracy, rElevation, rBearing, rSpeed);

            mHandler.postDelayed(this, mUpdatesDelay);
        }

        private void setMockLocation(GeoPoint location, float accuracy, float elevation, float bearing, float speed) {
            if (isMockLocationsEnabled) {
                MockLocProvider.setNetworkProvider(location.getLatitude(), location.getLongitude(), accuracy, bearing, elevation);
                MockLocProvider.setGpsProvider(location.getLatitude(), location.getLongitude(), bearing, speed, accuracy, elevation);
                Location fusedLocation = mFusedLocationProvider.build(location.getLatitude(), location.getLongitude(), accuracy, bearing, speed, elevation);
                mFusedLocationProvider.spoof(fusedLocation);
            } else if (isSystemApp) {
                MockLocProvider.reportLocation(location.getLatitude(), location.getLongitude(), accuracy, bearing, speed, elevation);
            }
        }
    };

    Thread mainStaticThread = new Thread(mainStaticRunnable);
}
*/
