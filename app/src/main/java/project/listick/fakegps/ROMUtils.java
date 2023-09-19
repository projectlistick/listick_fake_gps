package project.listick.fakegps;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ROMUtils {

    public static final int AOSP = 0;
    public static final int MIUI = 1;
    public static final int ONEUI = 2;

    public static int getVendorUI() {
        if (isMiui())
            return MIUI;
        else if (isOneUI())
            return ONEUI;

        return AOSP;
    }

    public static boolean isMiui() {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            String miui = (String) get.invoke(c, "ro.miui.ui.version.code");
            if (miui == null)
                throw new NullPointerException();
            return !miui.isEmpty();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            return false;
        }
    }

    public static boolean isOneUI() {
        try {
            Field semPlatformIntField = Build.VERSION.class.getDeclaredField("SEM_PLATFORM_INT");
            int version = semPlatformIntField.getInt(null) - 90000;
            if (version < 0) {
                // not one ui (could be previous Samsung OS)
                return false;
            }
            return true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            return false;
        }
    }

}
