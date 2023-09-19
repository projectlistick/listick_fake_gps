package project.listick.fakegps;

import android.app.Activity;
import android.location.Address;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import project.listick.fakegps.UI.RouteSettingsActivity;

public class LocationOperations {

    public static final int ROUTE_SETTINGS_REQUEST_CODE = 3;
    public static final int FIXED_SETTINGS_REQUEST_CODE = 5;

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
        double latError = ThreadLocalRandom.current().nextDouble(0, 90);
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
