package project.listick.fakegps;

import android.content.Context;
import android.location.Criteria;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.view.InputDeviceCompat;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MockLocProvider {

    private static Location gpsLocation;
    private static Location networkLocation;
    private static LocationManager locationManager;
    private static ILocationManager ILocationManager;

    private static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;

    private static boolean isMockLocationsEnabled;
    private static boolean isSystemApp;

    private static Random random;

    public static void initTestProvider() {
        locationManager = (LocationManager) FakeGPSApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        gpsLocation = new Location(GPS_PROVIDER);
        networkLocation = new Location(NETWORK_PROVIDER);
        isMockLocationsEnabled = PermissionManager.isMockLocationsEnabled(FakeGPSApplication.getAppContext());
        isSystemApp = PermissionManager.isSystemApp(FakeGPSApplication.getAppContext());
        random = new Random();

        if (isMockLocationsEnabled) {
            try {
                removeProviders();
                locationManager.addTestProvider(GPS_PROVIDER,
                        true, true, false, false,
                        true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                locationManager.setTestProviderEnabled(GPS_PROVIDER, true);

                locationManager.addTestProvider(NETWORK_PROVIDER,
                        false, false, false, false,
                        true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE);
                locationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);

            } catch (IllegalArgumentException e) {
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            }
            return;
        }

        if (isSystemApp) {
            try {
                Field mService = Class.forName("android.location.LocationManager").getDeclaredField("mService");
                mService.setAccessible(true);
                ILocationManager = (ILocationManager) mService.get(locationManager);
            } catch (Exception e) {
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            }
        }
    }

    public static void setGpsProvider(double latitude, double longitude, float bearing, float speed, float accuracy, float altitude) {
        float speedInMeters = speed / 3.6f;
        gpsLocation.setLatitude(latitude);
        gpsLocation.setLongitude(longitude);
        gpsLocation.setAltitude(altitude);
        gpsLocation.setBearing(bearing);
        gpsLocation.setAccuracy(accuracy);
        gpsLocation.setSpeed(speedInMeters);
        gpsLocation.setTime(getTime());
        gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        Log.d("MockLocProvider", "bearing: " + bearing + " speed: " + speed + " accuracy: " + accuracy + " altitude: " + altitude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gpsLocation.setVerticalAccuracyMeters(accuracy - ((accuracy >= 5) ? (float) ThreadLocalRandom.current().nextDouble(0, 4) : 0));
            gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
            gpsLocation.setBearingAccuracyDegrees(10.0f);
        }

        try {
            locationManager.setTestProviderLocation(GPS_PROVIDER, gpsLocation);
        } catch (SecurityException se) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, se);
        }
    }

    public static void setNetworkProvider(double latitude, double longitude, float accuracy, float bearing, float altitude) {
        networkLocation.setLatitude(latitude);
        networkLocation.setLongitude(longitude);
        networkLocation.setTime(getTime());
        networkLocation.setBearing(bearing);
        networkLocation.setAltitude(altitude);
        networkLocation.setAccuracy(accuracy);
        networkLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            networkLocation.setVerticalAccuracyMeters(100.f);
            networkLocation.setSpeedAccuracyMetersPerSecond(20.f);
            networkLocation.setBearingAccuracyDegrees(10.0f);
        }
        try {
            locationManager.setTestProviderLocation(NETWORK_PROVIDER, networkLocation);
        } catch (SecurityException se) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, se);
        }
    }


    public static void reportLocation(double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
        try {
            gpsLocation.setLatitude(latitude);
            gpsLocation.setLongitude(longitude);
            gpsLocation.setTime(getTime());
            gpsLocation.setAccuracy(accuracy);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gpsLocation.setVerticalAccuracyMeters(100.0f);
                gpsLocation.setSpeedAccuracyMetersPerSecond(20.0f);
                gpsLocation.setBearingAccuracyDegrees(10.0f);
            }
            gpsLocation.setAltitude(altitude);
            gpsLocation.setBearing(bearing);
            gpsLocation.setSpeed(speed);
            gpsLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            if (isSystemApp && !isMockLocationsEnabled) {
                ILocationManager.reportLocation(gpsLocation, false);
            }
        } catch (Exception e) {
            Log.d(BuildConfig.APPLICATION_ID, null, e);
        }
    }

    private static long getTime() {
        return System.currentTimeMillis() - ((long) random.nextInt(InputDeviceCompat.SOURCE_KEYBOARD));
    }

    public static void removeProviders() {
        if (isMockLocationsEnabled) {
            try {
                locationManager.removeTestProvider(GPS_PROVIDER);
                locationManager.removeTestProvider(NETWORK_PROVIDER);
            } catch (IllegalArgumentException e) {
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            }
        }
    }
}