package project.listick.fakegps.Contract;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityOptionsCompat;

import org.osmdroid.util.GeoPoint;

import project.listick.fakegps.Enumerations.ERouteTransport;

/*
 * Created by LittleAngry on 25.12.18 (macOS 10.12)
 * */
public interface MapsImpl {
    interface UIImpl {
        void getAddress(String address);
        void toggleRemoveRoute(int status);
        void setWhereToAddress(String address);
        void lockSearchBar(boolean isLocked);
        void useMenu(boolean isOpen);
        void alertDialog(String dialogTitle, String dialogMessage, boolean setCancelable, String posButtonLabel, DialogInterface.OnClickListener posButtonHandler, String negativeButtonLabel, DialogInterface.OnClickListener negativeButtonHandler, @DrawableRes int iconRes);
        void enableDone(int visibility);
        void enableSpeedbar(int visibility);
        void enablePause(int visibility);
        void enableStop(int visibility);
        void setPauseIcon(boolean isPaused);
        void updateSpeedbar(int speed, double passedDistance, double distance);
        String getWhereToAddress();
        void setJoystickMsgVisiblity(int visiblity);
        void toggleEditButton(int show);
        void setAddressShimmer(boolean enable);
        void setAddMoreRoute(int visibilty);
        void setLocationDisabledNotification(int visibilty);
    }

    interface PresenterImpl {
        void onMapDrag();
        void onCurrentLocationClick();
        void onActivityLoad();
        void onSpoofClick(GeoPoint geoPoint);
        void onRoute(double sourceLat, double sourceLong, double destLat, double destLong, ERouteTransport transport);
        void onDestroy();
        void onMenu();
        void removeRoute();
        void onBookmarkResult(Intent data, int resultCode);
        void onRouteRemove();
        void onPause();
        void onResume();
        void onItem(MenuItem item);
        void setFixedMode();
        void handleStop();
        void handleClear();
        void handlePause();
        void onAddMoreRoute(ActivityOptionsCompat uiOptions);
        void changePoint();
    }

    interface ModelImpl {
        void getPermissions();
        void moveCameraToLastLocation();
        void openMenu();
    }
}
