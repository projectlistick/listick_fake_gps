package project.listick.fakegps.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.FusedLocationsProvider;
import project.listick.fakegps.Geometry;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.LocationOperations;
import project.listick.fakegps.MainServiceControl;
import project.listick.fakegps.MockLocProvider;
import project.listick.fakegps.MultipleRoutesInfo;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Presenter.MapsPresenter;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.Randomizer;
import project.listick.fakegps.RouteManager;
import project.listick.fakegps.SpoofingPlaceInfo;

public class RouteSpooferService extends Service {

    public static final String KEY_ACCURACY = "accuracy";
    public static final String KEY_DEVIATION = "deviation";
    public static final String KEY_UPDATES_DELAY = "updates_delay";
    public static final String KEY_DEFAULT_UNIT = "default_unit";
    public static final String KEY_BRAKE_AT_TURINING = "brake_at_turning";

    public static final String UI_SPEED_KEY = "ui_speed_key";
    public static final String UI_PASSED_DISTANCE = "ui_passed_distance";
    public static final String UI_TOTAL_DISTANCE = "ui_total_distance";

    private FusedLocationsProvider mFusedLocationProvider;
    private Randomizer mRandomizer;
    private GeoPoint mCurrentStep;
    private Handler mHandler;

    private int mSpeed;
    private int mSpeedDiff;
    private int mDefaultUnit;
    private int mTrafficSide;

    private int mOriginDelay;
    private int mDestDelay;
    private int mUpdatesDelay;

    private float mAccuracy;
    private float mElevation;
    private float mElevationDiff;
    private float mBearing;

    private double mTotalDistance;
    private double mPassedDistance;

    private boolean isClosedRoute = false;
    private boolean mDeviation;
    private boolean mBrakeAtTurning;
    private boolean isMockLocationsEnabled;
    private boolean isSystemApp;
    private boolean isPaused;
    private boolean waitingStart;

    private Intent mUpdateUI;

    private int mRouteSlice = 0;
    private ArrayList<GeoPoint>[] mSlices;
    private ArrayList<GeoPoint> mSpoofRoute = new ArrayList<GeoPoint>();
    private ArrayList<MultipleRoutesInfo> mRoutes = new ArrayList<>();

    private static class SourceData {
        public static double totalDistance;
        public static boolean isClosedRoute;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        MainServiceControl.startServiceForeground(this);

        return START_STICKY;
    }

    private Geometry.UnitCast castAllUnits(int speed, int speedDiff) {

        Geometry.UnitCast casted = new Geometry.UnitCast();

        if (mDefaultUnit == AppPreferences.METERS) {
            speed = (int) Geometry.Speed.metersToKilometers(speed);
            speedDiff = (int) Geometry.Speed.metersToKilometers(speedDiff);
        } else if (mDefaultUnit == AppPreferences.MILES) {
            speed = (int) Geometry.Speed.milesToKilometers(speed);
            speedDiff = (int) Geometry.Speed.milesToKilometers(speedDiff);
        }

        casted.speed = speed;
        casted.speedDiff = speedDiff;

        return casted;
    }

    private void cast() {
        if (mDefaultUnit == AppPreferences.KILOMETERS)
            mTotalDistance = Geometry.Distance.metersToKilometers(mTotalDistance);
        if (mDefaultUnit == AppPreferences.MILES) {
            mTotalDistance = Geometry.Distance.metersToMiles(mTotalDistance);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mHandler = new Handler();
        mRandomizer = new Randomizer();
        mFusedLocationProvider = new FusedLocationsProvider(this);
        isMockLocationsEnabled = PermissionManager.isMockLocationsEnabled(this);
        isSystemApp = PermissionManager.isSystemApp(this);
        MockLocProvider.initTestProvider();

        mAccuracy = intent.getFloatExtra(KEY_ACCURACY, 10);
        mUpdatesDelay = intent.getIntExtra(KEY_UPDATES_DELAY, 1000);
        mDeviation = intent.getBooleanExtra(KEY_DEVIATION, true);
        mDefaultUnit = intent.getIntExtra(KEY_DEFAULT_UNIT, AppPreferences.METERS);
        mBrakeAtTurning = intent.getBooleanExtra(KEY_BRAKE_AT_TURINING, true);

        mTotalDistance = intent.getDoubleExtra(ListickApp.DISTANCE, 0);
        mSpeed = intent.getIntExtra(ListickApp.SPEED, 0);
        mBearing = (float) ThreadLocalRandom.current().nextDouble(5, 180);
        mSpeedDiff = intent.getIntExtra(RouteSettingsPresenter.SPEED_DIFF, 0);
        mTrafficSide = intent.getIntExtra(AppPreferences.TRAFFIC_SIDE, AppPreferences.RIGHT_HAND_TRAFFIC);
        mElevation = intent.getFloatExtra(RouteSettingsPresenter.ELEVATION, 197);
        mElevationDiff = intent.getFloatExtra(RouteSettingsPresenter.ELEVATION_DIFF, 4);

        mOriginDelay = intent.getIntExtra("origin_timeout", 0);
        waitingStart = mOriginDelay > 0;

        SourceData.totalDistance = mTotalDistance;
        SourceData.isClosedRoute = intent.getBooleanExtra(SpoofingPlaceInfo.CLOSED_ROUTE_MOTION_INVERT, false);

        cast();

        if (mSpeed <= 8) {
            mBrakeAtTurning = false;
        }

        /* cast */
        Geometry.UnitCast casted = castAllUnits(mSpeed, mSpeedDiff);
        mSpeed = casted.speed;
        mSpeedDiff = casted.speedDiff;

        mUpdateUI = new Intent();
        mUpdateUI.setAction(MapsPresenter.UPDATE_UI_ACTION);
        mUpdateUI.putExtra(UI_TOTAL_DISTANCE, mTotalDistance);


        return new ISpooferService.Stub() {
            @Override
            public void attachRoutes(List<MultipleRoutesInfo> routes) throws RemoteException {
                // combine all segments and start route spoofing

                setRoute(routes, false);
                mCurrentStep = new GeoPoint(mSpoofRoute.get(0).getLatitude(), mSpoofRoute.get(0).getLongitude(), mSpoofRoute.get(0).getAltitude());

                // ongoing point segmentation
                // int arrayRunSpeed = mRandomizer.getArrayRunSpeed(mSpeed, mUpdatesDelay);
                // int index = 3 * arrayRunSpeed;

                // if (index <= mSpoofRoute)


                if (mainRouteThread.getState() == Thread.State.NEW) {
                    mainRouteThread.start();
                } else {
                    mainRouteThread.interrupt();
                    mHandler.removeCallbacks(mainRouteRunnable);
                }

                if (waitingStart)
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        waitingStart = false;
                    }, mOriginDelay);

            }

            @Override
            public void setPause(boolean pause) throws RemoteException {
                isPaused = pause;
            }

            @Override
            public boolean isPaused() throws RemoteException {
                return isPaused;
            }

            @Override
            public List<MultipleRoutesInfo> getRoutes() throws RemoteException {
                return mRoutes;
            }
        };
    }

    private void setRoute(List<MultipleRoutesInfo> routes, boolean closedRoute) {
        mRoutes = (ArrayList<MultipleRoutesInfo>) routes;

        try {
            mSlices = new ArrayList[mRoutes.size()];
            mRouteSlice = 0;

            for (int i = 0; i < mRoutes.size(); i++) {
                mSlices[i] = new ArrayList<>();
                List<GeoPoint> points = mRoutes.get(i).getRoute();
                RouteManager.startMotion(points, mSlices[i]);
                if (isClosedRoute) {
                    isClosedRoute = false;
                    Collections.reverse(mSlices[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSpoofRoute = mSlices[mRouteSlice];
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainRouteThread.interrupt();
        if (mHandler != null && mainRouteRunnable != null)
            mHandler.removeCallbacks(mainRouteRunnable);

        stopForeground(true);
    }

    public static class FakeRouteInfo {
        boolean arrived;
        float speed;
        double altitude;
    }


    Runnable mainRouteRunnable = new Runnable() {
        private int arrayRunIndex = 0;
        private int arrayRunSpeed;
        private int brakeSpeed;
        private boolean isNeedBrake;

        @Override
        public void run() {
            float rSpeed = mRandomizer.getRandomSpeed(mSpeed, mSpeedDiff);
            float rElevation = (float) mCurrentStep.getAltitude();
            float rAccuracy = mRandomizer.getAccuracy(mAccuracy);
            if (isNeedBrake) rSpeed = brakeSpeed;

            arrayRunSpeed = mRandomizer.getArrayRunSpeed((int) rSpeed, mUpdatesDelay);
            if (mSpoofRoute == null || mSpoofRoute.isEmpty())
                return;

            if (!isPaused && !waitingStart) arrayRunIndex += arrayRunSpeed;
            if (arrayRunIndex >= mSpoofRoute.size() - 1) {
                arrayRunIndex = mSpoofRoute.size() - 1;
                if (mRouteSlice < mSlices.length - 1) {
                    replaceRouteSlice();
                    mHandler.postDelayed(this, mUpdatesDelay);
                    return;
                }
            }

            if (mSpeedDiff != 0)
                rSpeed += Math.random() * mSpeedDiff;
                //rSpeed += ThreadLocalRandom.current().nextDouble(0, mSpeedDiff);

            FakeRouteInfo info = onMockArrived(rSpeed, rAccuracy);
            rSpeed = info.speed;

            //if (!info.arrived) {
                mCurrentStep.setLatitude(mSpoofRoute.get(arrayRunIndex).getLatitude());
                mCurrentStep.setLongitude(mSpoofRoute.get(arrayRunIndex).getLongitude());
                mCurrentStep.setAltitude(mSpoofRoute.get(arrayRunIndex).getAltitude());
            //}

            int nextPosBearing = arrayRunIndex + 2;
            if (nextPosBearing >= mSpoofRoute.size() - 1) nextPosBearing = arrayRunIndex;
            float bearing = (float) Geometry.getAzimuth(mCurrentStep.getLatitude(), mCurrentStep.getLongitude(), mSpoofRoute.get(nextPosBearing).getLatitude(),
                    mSpoofRoute.get(nextPosBearing).getLongitude());
            Log.d("Randomize", "bearing: " + bearing);

            if (info.arrived) {
                float coefficient = (float) ThreadLocalRandom.current().nextDouble(-5, 5);
                bearing = mBearing;
                bearing += coefficient;
                rElevation = (float) info.altitude;
            }

            makeBrakeAtTurning();
            if (!info.arrived){
                addTrafficSideOffset(nextPosBearing);
                rElevation += ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
            }
            setMockLocation(rSpeed == -1 ? 0 : rSpeed, rAccuracy, bearing + (float) Math.random(), rElevation);

            if (rSpeed == -1 && isClosedRoute) {
                setRoute(mRoutes, isClosedRoute);

                MultipleRoutesInfo routeInfo = mRoutes.get(0);
                mSpeed = routeInfo.getSpeed();
                mSpeedDiff = routeInfo.getSpeedDiff();

                mElevation = routeInfo.getElevation();
                mElevationDiff = routeInfo.getElevationDiff();

                mPassedDistance = 0;
                arrayRunSpeed = 0;
                arrayRunIndex = 0;
                isNeedBrake = false;

                mHandler.postDelayed(this, mUpdatesDelay);
                return;
            }

            mHandler.postDelayed(this, mUpdatesDelay);
        }

        public void replaceRouteSlice() {
            mSpoofRoute = mSlices[++mRouteSlice];

            MultipleRoutesInfo routeInfo = mRoutes.get(mRouteSlice);
            mSpeed = routeInfo.getSpeed();
            mSpeedDiff = routeInfo.getSpeedDiff();
            Log.d("MultipleRoutesInfo", "mSpeed: " + mSpeed + " mSpeedDiff: " + mSpeedDiff);

            mElevation = routeInfo.getElevation();
            mElevationDiff = routeInfo.getElevationDiff();
            Log.d("MultipleRoutesInfo", "mElevation: " + mElevation + " mElevationDiff: " + mElevationDiff);

            Log.d("MultipleRoutesInfo", "Sleeping (" + routeInfo.getStartingPauseTime() + " ms)");
            SystemClock.sleep(routeInfo.getStartingPauseTime()); // Parking at the start

            arrayRunSpeed = 0;
            arrayRunIndex = 0;
            isNeedBrake = false;
            Log.d("MultipleRoutesInfo", "Route slice changed");
        }

        private void setMockLocation(float speed, float accuracy, float bearing, float elevation) {
            if (isMockLocationsEnabled) {
                MockLocProvider.setNetworkProvider(mCurrentStep.getLatitude(), mCurrentStep.getLongitude(), accuracy, bearing, elevation);
                MockLocProvider.setGpsProvider(mCurrentStep.getLatitude(), mCurrentStep.getLongitude(), bearing, speed, accuracy, elevation);
                Location fusedLocation = mFusedLocationProvider.build(mCurrentStep.getLatitude(), mCurrentStep.getLongitude(), accuracy, bearing, speed, elevation);
                mFusedLocationProvider.spoof(fusedLocation);
            } else if (isSystemApp) {
                MockLocProvider.reportLocation(mCurrentStep.getLatitude(), mCurrentStep.getLongitude(), accuracy, bearing, speed, elevation);
            }
        }

        // Returns speed
        private FakeRouteInfo onMockArrived(float speed, float accuracy) {
            FakeRouteInfo routeInfo = new FakeRouteInfo();
            if (arrayRunIndex >= mSpoofRoute.size() - 1) {
                // Arrived
                routeInfo.arrived = true;
                //if (Math.random() < 0.8)
                    speed = (float) ThreadLocalRandom.current().nextDouble(0, 0.3);
                //else
                //    speed = 0;
                updateUI(speed, mTotalDistance);

                if (SourceData.isClosedRoute) {
                    isClosedRoute = !isClosedRoute;
                    routeInfo.speed = -1;
                    return routeInfo;
                }

                double altitude = mCurrentStep.getAltitude();
                altitude += ThreadLocalRandom.current().nextDouble(-1, +1);

                if (mDeviation)
                    deviate(accuracy);
                routeInfo.speed = speed;
                routeInfo.altitude = altitude;
                return routeInfo;
            } else {
                if (!isPaused && !waitingStart) {
                    mPassedDistance += Geometry.distance(mSpoofRoute.get(arrayRunIndex - arrayRunSpeed).getLatitude(), mSpoofRoute.get(arrayRunIndex - arrayRunSpeed).getLongitude(),
                            mSpoofRoute.get(arrayRunIndex).getLatitude(), mSpoofRoute.get(arrayRunIndex).getLongitude(), mDefaultUnit);
                    updateUI(speed, mPassedDistance);
                } else {
                    updateUI(0, mPassedDistance);
                }
            }
            routeInfo.speed = speed;
            routeInfo.arrived = false;
            return routeInfo;
        }

        private void deviate(float accuracy) {
            GeoPoint point = LocationOperations.deviate(mCurrentStep, 0.5f);
            mCurrentStep.setLatitude(point.getLatitude());
            mCurrentStep.setLongitude(point.getLongitude());
        }

        private void addTrafficSideOffset(int nextPosBearing) {
            double latitude = mCurrentStep.getLatitude();
            double longitude = mCurrentStep.getLongitude();
            // longitude offset

            if (mTrafficSide == AppPreferences.RIGHT_HAND_TRAFFIC) {
                double distance = Geometry.distance(latitude, longitude, mSpoofRoute.get(nextPosBearing).getLatitude(),
                        mSpoofRoute.get(nextPosBearing).getLongitude(), AppPreferences.KILOMETERS);
                double bearing = getNewAngle(latitude, longitude, mSpoofRoute.get(nextPosBearing).getLatitude(),
                        mSpoofRoute.get(nextPosBearing).getLongitude());

                Log.d("Listick", "bearing: " + bearing + "\ndistance: " + distance);

                GeoPoint geo = bearingDistance(latitude, longitude, distance, bearing + 25);
                mCurrentStep.setLatitude(geo.getLatitude());
                mCurrentStep.setLongitude(geo.getLongitude());
            } else if (mTrafficSide == AppPreferences.LEFT_HAND_TRAFFIC) {
                double distance = Geometry.distance(latitude, longitude, mSpoofRoute.get(nextPosBearing).getLatitude(),
                        mSpoofRoute.get(nextPosBearing).getLongitude(), AppPreferences.KILOMETERS);
                double bearing = getNewAngle(latitude, longitude, mSpoofRoute.get(nextPosBearing).getLatitude(),
                        mSpoofRoute.get(nextPosBearing).getLongitude());

                Log.d("Listick", "bearing: " + bearing + "\ndistance: " + distance);

                GeoPoint geo = bearingDistance(latitude, longitude, distance, bearing - 25);
                mCurrentStep.setLatitude(geo.getLatitude());
                mCurrentStep.setLongitude(geo.getLongitude());
            }

        }

        public double getNewAngle(double startLat, double startLong, double destLat, double destLong) {

            double dLon = (destLong - startLong);
            double y = Math.sin(dLon) * Math.cos(destLat);
            double x = Math.cos(startLat) * Math.sin(destLat) - Math.sin(startLat) * Math.cos(destLat) * Math.cos(dLon);

            return Math.toDegrees((Math.atan2(y, x)));
        }


        private GeoPoint bearingDistance(double lat, double lon, double radius, double bearing) {
            double lat1Rads = toRad(lat);
            double lon1Rads = toRad(lon);
            double R_KM = 6371; // radius in KM
            double d = radius / R_KM; //angular distance on earth's surface

            double bearingRads = toRad(bearing);
            double lat2Rads = Math.asin(Math.sin(lat1Rads) * Math.cos(d) + Math.cos(lat1Rads) * Math.sin(d) * Math.cos(bearingRads));

            double lon2Rads = lon1Rads + Math.atan2(
                    Math.sin(bearingRads) * Math.sin(d) * Math.cos(lat1Rads),
                    Math.cos(d) - Math.sin(lat1Rads) * Math.sin(lat2Rads)
            );

            return new GeoPoint(toDeg(lat2Rads), toDeg(lon2Rads));
        }

        double toRad(double degrees) {
            return degrees * Math.PI / 180;
        }

        double toDeg(double radians) {
            return radians * 180 / Math.PI;
        }


        private void updateUI(float speed, double passedDistance) {

            if (mDefaultUnit == AppPreferences.METERS) {
                speed = (int) Geometry.Speed.kilometersToMeters(speed);
                passedDistance = (int) Geometry.Speed.kilometersToMeters(passedDistance);
            } else if (mDefaultUnit == AppPreferences.MILES) {
                speed = (int) Geometry.Speed.kilometersToMiles(speed);
                passedDistance = (int) Geometry.Speed.kilometersToMiles(passedDistance);
            }

            mUpdateUI.putExtra(UI_PASSED_DISTANCE, passedDistance);
            mUpdateUI.putExtra(UI_SPEED_KEY, (int) speed);

            sendBroadcast(mUpdateUI);
        }

        private void makeBrakeAtTurning() {
            if (mBrakeAtTurning) {
                int nextPosAngle = arrayRunIndex + (arrayRunSpeed * 2);
                int nextPosAngle2 = arrayRunIndex + 1;
                if (nextPosAngle >= mSpoofRoute.size() - 1) nextPosAngle = mSpoofRoute.size() - 1;
                if (nextPosAngle2 >= mSpoofRoute.size() - 1) nextPosAngle2 = mSpoofRoute.size() - 1;

                double angle = Geometry.getAngle(mCurrentStep.getLatitude(), mCurrentStep.getLongitude(),
                        mSpoofRoute.get(nextPosAngle).getLatitude(), mSpoofRoute.get(nextPosAngle).getLongitude(),
                        mSpoofRoute.get(nextPosAngle2).getLatitude(), mSpoofRoute.get(nextPosAngle2).getLongitude());
                double coefficient = angle / 180;
                brakeSpeed = (int) (mSpeed * coefficient);
                isNeedBrake = true;
            }

        }

    };

    Thread mainRouteThread = new Thread(mainRouteRunnable);
}
