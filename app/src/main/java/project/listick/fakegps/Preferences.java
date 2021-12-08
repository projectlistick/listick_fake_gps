package project.listick.fakegps;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import project.listick.fakegps.Presenter.SettingsPresenter;
import project.listick.fakegps.UI.SettingsActivity;

/*
 * Created by LittleAngry on 01.01.19 (macOS 10.12)
 * */
public class Preferences {

    public static final int METERS = 0;
    public static final int KILOMETERS = 1;
    public static final int MILES = 2;

    public static final int RIGHT_HAND_TRAFFIC = 0;
    public static final int LEFT_HAND_TRAFFIC = 1;

    public static final String TRAFFIC_SIDE = "traffic_side";

    public static int getUpdatesDelay(@NonNull Context context){
        return (int) (context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getFloat(ListickApp.GPS_UPDATES_DELAY, 1) * 1000);
    }
    public static boolean getBrakeAtTurning(@NonNull Context context){
        return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getBoolean(ListickApp.BRAKE_AT_TURNING, true);
    }
    public static int getStandartUnit(@NonNull Context context){
        return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getInt(ListickApp.STANDART_UNIT, KILOMETERS);
    }
    public static int getTrafficSide(@NonNull Context context){
        return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getInt(TRAFFIC_SIDE, RIGHT_HAND_TRAFFIC);
    }
    static boolean getKeepAtCenter(@NonNull Context context){
       return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getBoolean(ListickApp.KEEP_AT_CENTER, false);
    }
    public static boolean getLocationError(@NonNull Context context){
       return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getBoolean(SettingsPresenter.LOCATION_ERROR, true);
    }
    public static boolean getAutoAltitude(@NonNull Context context){
       return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getBoolean(SettingsPresenter.AUTO_ALTITUDE, true);
    }
    public static int getMapTileProvider(@NonNull Context context){
       return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getInt(MapLoader.DEFAULT_TILE_PROVIDER, MapLoader.DEFAULT_TILES);
    }

    public static int getAccuracy(Context context){
        return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getInt(ListickApp.ACCURACY_SETTINGS, 10);
    }

    public static String getMapsApiKey(Context context){
        return context.getSharedPreferences(SettingsActivity.FAKEGPS_SETTINGS, Context.MODE_PRIVATE).getString("maps_v2_apikey", null);
    }


    public static String getUnitName(@NonNull Context context, int unit){
        Resources res = context.getResources();

        if (unit == KILOMETERS)
            return res.getString(R.string.kiloabbr);
        if (unit == MILES)
            return res.getString(R.string.milesabbr);
        if (unit == METERS)
            return res.getString(R.string.meterabbr);

        return null;
    }
}
