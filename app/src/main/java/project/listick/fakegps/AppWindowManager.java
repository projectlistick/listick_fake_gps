package project.listick.fakegps;

import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.Window;

public class AppWindowManager {
    public static void makeEdge2Edge(Window window){
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    public static void drawColoredIcons(Window window, int configuration){
        if (configuration != Configuration.UI_MODE_NIGHT_YES) {
            window.getDecorView().setSystemUiVisibility(
                    window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void makeDarkNavbarButtons(Window window, int configuration) {
        if (configuration != Configuration.UI_MODE_NIGHT_YES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.getDecorView().setSystemUiVisibility(
                        window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }

    }
}
