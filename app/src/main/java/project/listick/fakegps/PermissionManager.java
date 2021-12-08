package project.listick.fakegps;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.provider.Settings;

import java.util.List;

/*
 * Created by LittleAngry on 27.12.18 (macOS 10.12)
 * */
public class PermissionManager {

    public static boolean canDrawOverlays(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager == null)
            return false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    public static boolean isMockLocationsEnabled(Context context) {
        boolean isMockLocationEnabled;
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOps == null)
            return false;

        int mockLocationResult = appOps.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), BuildConfig.APPLICATION_ID);

        isMockLocationEnabled = mockLocationResult == AppOpsManager.MODE_ALLOWED;
        return isMockLocationEnabled;
    }

    public static boolean isSystemApp(Context context) {
        boolean isSystemApp = false;
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> list = pm.getInstalledPackages(0);


            for (PackageInfo pi : list) {
                ApplicationInfo ai = pm.getApplicationInfo(pi.packageName, 0);

                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    if (pi.packageName.equals(AppUtils.getPackageName(context)))
                        isSystemApp = true;
                }
            }
            return isSystemApp;
        } catch (Exception e) {
            return isSystemApp;
        }
    }

}
