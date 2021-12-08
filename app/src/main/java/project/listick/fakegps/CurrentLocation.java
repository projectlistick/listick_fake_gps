package project.listick.fakegps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import project.listick.fakegps.Daemons.RouteSpooferService;

/*
 * Created by LittleAngry on 06.01.19 (macOS 10.12)
 * */
public class CurrentLocation implements LocationListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener  {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private final GoogleApiClient apiClient;
    private final LocationManager lm;
    private final Context context;
    private final MapView mapView;
    private MyLocationPoint mMyLocationPoint;

    private Location location;

    public CurrentLocation(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;

        this.lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.mMyLocationPoint = new MyLocationPoint();

        this.apiClient = new GoogleApiClient.Builder(context)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        apiClient.connect();

        registerUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null)
            return;

        this.location = location;
        GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

        mMyLocationPoint.setMyLocationPoint(mapView, currentLocation, location.getAccuracy(), location.getBearing());

        if (PermissionManager.isServiceRunning(context, RouteSpooferService.class) && Preferences.getKeepAtCenter(context))
                mapView.getController().animateTo(new GeoPoint(location.getLatitude() + 0.0025, location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    public void onDestroy() {
        lm.removeUpdates(this);

        if (apiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
    }

    @SuppressLint("MissingPermission")
    public void registerUpdates(){
        if (PermissionManager.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                && PermissionManager.isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            if (lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
                lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
    }

    public void removeUpdates(){
        lm.removeUpdates(this);
    }


    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public Location getLocation(String lastKnownLocationProvider) {
        if (PermissionManager.isPermissionGranted(this.context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                PermissionManager.isPermissionGranted(this.context, Manifest.permission.ACCESS_COARSE_LOCATION))
            return (this.location != null) ? this.location : this.lm.getLastKnownLocation(lastKnownLocationProvider);
        else
            return null;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location  are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        // Also determine location for telemtry goat on 775 socket by 
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /*
    * Google Fused Locations Provider
    * */
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
       onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(apiClient));
       if (apiClient.isConnected() && PermissionManager.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
           LocationRequest request = new LocationRequest(); // import from gms library :: импорт с библиотеки от гугла gms
           request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

           if (PermissionManager.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION))
               LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
       }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        if (PermissionManager.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                || PermissionManager.isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION))
            return (location != null) ? location : LocationServices.FusedLocationApi.getLastLocation(apiClient);
        else
            return null;
    }
}
