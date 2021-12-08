package project.listick.fakegps.Model;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import project.listick.fakegps.Contract.MapsImpl;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.UI.PermissionsActivity;

/*
 * Created by LittleAngry on 25.12.18 (macOS 10.12)
 * */
public class MapsModel implements MapsImpl.ModelImpl {

    private MapView mMap;
    private Context mContext;

    private MapsImpl.UIImpl mUserInterface;

    public MapsModel(MapView mMap, Context context) {
        this.mMap = mMap;
        this.mContext = context;
        this.mUserInterface = (MapsImpl.UIImpl) context;
    }

    @Override
    public void getPermissions() {
        if (!PermissionManager.isPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION))
            mContext.startActivity(new Intent(mContext, PermissionsActivity.class));
    }

    @Override
    public void moveCameraToLastLocation() {
        SharedPreferences locationPreferences = mContext.getSharedPreferences(ListickApp.LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        mMap.getController().animateTo(new GeoPoint(locationPreferences.getFloat(ListickApp.LATITUDE, 0f), locationPreferences.getFloat(ListickApp.LONGITUDE, 0f)));
        mMap.getController().setZoom(locationPreferences.getFloat(ListickApp.ZOOM, ListickApp.STANDART_ZOOM_VALUE));
    }


    @Override
    public void openMenu() {
        mUserInterface.useMenu(true);
    }

}