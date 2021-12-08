package project.listick.fakegps;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.MessagePattern;
import android.location.Address;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.UI.RouteSettingsActivity;

public class LocationOperations {

    public static final int ROUTE_SETTINGS_REQUEST_CODE = 3;
    public static final int FIXED_SETTINGS_REQUEST_CODE = 5;

    private static final int DEVIATION_MAX_COUNTS = ThreadLocalRandom.current().nextInt(15, 40);
    private static int deviationCounter = 0;

    public void startSpoofing(GeoPoint geoPoint, double distance, Activity mActivity, boolean isRoute) {
        int requestCode = (isRoute) ? ROUTE_SETTINGS_REQUEST_CODE : FIXED_SETTINGS_REQUEST_CODE;
        double startLatitude = (isRoute) ? RouteManager.routes.get(0).getRoute().get(0).getLatitude() : geoPoint.getLatitude();
        double startLongitude = (isRoute) ? RouteManager.routes.get(0).getRoute().get(0).getLongitude() : geoPoint.getLongitude();

        SpoofingPlaceInfo.latitude = geoPoint.getLatitude();
        SpoofingPlaceInfo.longtiude = geoPoint.getLongitude();

        RouteSettingsActivity.startActivity(mActivity, startLatitude, startLongitude, distance, isRoute, false, requestCode);

        if (!isRoute) {
            AsyncGeocoder geocoder = new AsyncGeocoder(mActivity);
            geocoder.getLocationAddress(geoPoint.getLatitude(), geoPoint.getLongitude(), new AsyncGeocoder.Callback() {
                @Override
                public void onSuccess(List<Address> locations) {
                    SpoofingPlaceInfo.address = locations.get(0).getAddressLine(0);
                }

                @Override
                public void onError() {
                    SpoofingPlaceInfo.address = mActivity.getString(R.string.failed_to_define_address);
                }
            });
        }

    }
    public static GeoPoint deviate(GeoPoint input, float accuracy) {
        // latitude это широта
        double latError = ThreadLocalRandom.current().nextDouble(0, 90);
        // longitude это долгота - от
        double lngError = ThreadLocalRandom.current().nextDouble(0, 180);

        double tempLat = accuracy / (111_111 / Math.cos(input.getLatitude()));
        tempLat *= Math.cos(latError);
        if (ThreadLocalRandom.current().nextBoolean()) tempLat += input.getLatitude();
        else tempLat = input.getLatitude() - tempLat;

        double tempLng = accuracy / (111_111 / Math.cos(input.getLongitude()));
        tempLng *= Math.sin(lngError);
        if (ThreadLocalRandom.current().nextBoolean()) tempLng += input.getLongitude();
        else tempLng = input.getLongitude() - tempLng;

        return new GeoPoint(tempLat, tempLng);
    }

}

/*
public static GeoPoint deviate(double latitude, double longitude) {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        if (deviationCounter < DEVIATION_MAX_COUNTS) {

            double latError = ThreadLocalRandom.current().nextDouble(0, 180);
            double lngError = ThreadLocalRandom.current().nextDouble(180, 360);

            boolean isPlus = ThreadLocalRandom.current().nextBoolean();

            //double deviationStep = ThreadLocalRandom.current().nextDouble(0.00000429999, 0.00000539999);

           //  if (!isPlus)
           //      deviationStep = 0 - deviationStep; // negative

            double tempLng = Math.cos(latError) * deviationStep + longitude;
            double tempLat = Math.sin(lngError) * deviationStep + latitude;

            geoPoint.setLatitude(tempLat);
            geoPoint.setLongitude(tempLng);
        }
        if (deviationCounter >= DEVIATION_MAX_COUNTS)
            deviationCounter = 0;
        deviationCounter++;

        return geoPoint;
    }

 */