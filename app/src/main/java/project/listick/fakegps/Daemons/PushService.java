package project.listick.fakegps.Daemons;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import project.listick.fakegps.R;

public class PushService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            sendNotification(notification.getTitle(), notification.getBody());
        }
    }

    private void sendNotification(String title, String body) {
        int PUSH_NOTIFICATION_ID = 0;

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "project.listick.fakegps.push";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.push_notification_name), NotificationManager.IMPORTANCE_DEFAULT);
             channel.enableLights(true);
            channel.setLightColor(Color.BLACK);

            if (manager != null)
                manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_my_location)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.DEFAULT_ALL)
                .setContentTitle(title)
                .setContentText(body);

        if (manager != null)
            manager.notify(PUSH_NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

}
