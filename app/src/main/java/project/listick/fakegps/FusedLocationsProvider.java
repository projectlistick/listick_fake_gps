package project.listick.fakegps;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/*
 * Created by LittleAngry on 21.01.19 (macOS 10.12)
 * */
public class FusedLocationsProvider implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private final GoogleApiClient apiClient;
    private final Context context;
    private final Location location;

    public Location build(double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
        float speedInMeters = speed / 3.6f;

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(speedInMeters);
        location.setAccuracy(accuracy);
        location.setAltitude(altitude);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setBearing(bearing);

        return this.location;
    }

    public void spoof(Location location) {
        if (!PermissionManager.isMockLocationsEnabled(context))
            return;

        if (apiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(context).setMockMode(true);
            LocationServices.getFusedLocationProviderClient(context).setMockLocation(location);
        }
    }

    public FusedLocationsProvider(Context context) {
        this.context = context;
        this.location = new Location(LocationManager.GPS_PROVIDER);

        this.apiClient = new GoogleApiClient.Builder(this.context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.apiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.setMockMode(apiClient, true);
        } catch (SecurityException e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
