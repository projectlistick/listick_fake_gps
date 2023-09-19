package project.listick.fakegps;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/*
 * Created by LittleAngry on 01.01.19 (macOS 10.12)
 * */

// TODO: Bad code, rewrite
public class AppPreferences {

    public static final int METERS = 0;
    public static final int KILOMETERS = 1;
    public static final int MILES = 2;

    public static final int RIGHT_HAND_TRAFFIC = 0;
    public static final int LEFT_HAND_TRAFFIC = 1;

    public static final String TRAFFIC_SIDE = "traffic_side";

    public static int getUpdatesDelay(@NonNull Context context){
        return (int) (Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(context).getString(ListickApp.GPS_UPDATES_DELAY, String.valueOf(1))) * 1000);
    }
    public static boolean getBrakeAtTurning(@NonNull Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ListickApp.BRAKE_AT_TURNING, true);
    }
    public static int getStandartUnit(@NonNull Context context){
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(ListickApp.STANDART_UNIT, String.valueOf(KILOMETERS)));
    }
    public static int getTrafficSide(@NonNull Context context){
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(TRAFFIC_SIDE, String.valueOf(RIGHT_HAND_TRAFFIC)));
    }
    public static boolean getLocationError(@NonNull Context context){
       return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("location_error", true);
    }
    public static boolean getAutoAltitude(@NonNull Context context){
       return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_altitude", true);
    }
    public static int getMapTileProvider(@NonNull Context context){
       return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(MapLoader.DEFAULT_TILE_PROVIDER, String.valueOf(MapLoader.DEFAULT_TILES)));
    }

    public static int getAccuracy(Context context){
        return (int) Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(context).getString(ListickApp.ACCURACY_SETTINGS, "10"));
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
