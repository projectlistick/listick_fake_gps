package project.listick.fakegps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import project.listick.fakegps.Services.FixedSpooferService;
import project.listick.fakegps.Services.ISpooferService;
import project.listick.fakegps.Services.JoystickService;
import project.listick.fakegps.Services.RouteSpooferService;
import project.listick.fakegps.UI.MapsActivity;

public class MainServiceControl {

    public static final String SERVICE_CONTROL_ACTION = "project.listick.fakegps.actionservice.daemons.ctrl";
    private final Context mContext;

    public MainServiceControl(Context context){
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainServiceControl.SERVICE_CONTROL_ACTION);
    }

    public void sendNewCoordinates(double latitude, double longitude){
        Intent local = new Intent();
        local.setAction(SERVICE_CONTROL_ACTION);
        local.putExtra(ListickApp.LATITUDE, latitude);
        local.putExtra(ListickApp.LONGITUDE, longitude);
        mContext.sendBroadcast(local);
    }

    public static boolean isRouteSpoofingServiceRunning(Context context) {
        return PermissionManager.isServiceRunning(context, RouteSpooferService.class);
    }
    public static boolean isFixedSpoofingServiceRunning(Context context) {
        return PermissionManager.isServiceRunning(context, FixedSpooferService.class);
    }
    public static boolean isJoystickSpoofingRunning(Context context) {
        return PermissionManager.isServiceRunning(context, JoystickService.class);
    }

    public void setPause(ISpooferService service, boolean pause) {
        try {
            service.setPause(pause);
        } catch (Exception e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
        }
    }

    public boolean isPaused(ISpooferService service) {
        try {
            return service.isPaused();
        } catch (Exception e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
        }
        return false;
    }

    public static void startServiceForeground(Service context) {
        String NOTIFICATION_CHANNEL_ID = "project.listick.fakegps_SPOOFING_STATUS";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_status_control), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.BLACK);

            if (manager != null)
                manager.createNotificationChannel(channel);
        }

        Intent openActivity = new Intent(context, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, openActivity, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_pin)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notify_status_description));

        int NOTIFICATION_ID = 2;
        context.startForeground(NOTIFICATION_ID, builder.build());
    }


}
