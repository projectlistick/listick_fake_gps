package project.listick.fakegps.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.FusedLocationsProvider;
import project.listick.fakegps.JoystickControl;
import project.listick.fakegps.JoystickOverlay;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.MockLocProvider;
import project.listick.fakegps.Randomizer;

public class JoystickService extends Service {

    private FusedLocationsProvider fusedProvider;
    private Handler handler;

    private double latitude;
    private double longitude;
    private int updatesDelay;
    private int accuracy;
    private int speed;

    private double step;
    private float bearing;

    private Randomizer randomizer;
    private JoystickOverlay overlay;

    @Override
    public void onCreate() {
        super.onCreate();

        overlay = new JoystickOverlay(this, (direction, strength, angle) -> {
            bearing = -angle;
            double radians = Math.toRadians(-angle);

            longitude = Math.cos(radians) * step + longitude;
            latitude = Math.sin(radians) * step + latitude;

        });
        overlay.drawOverlay();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MockLocProvider.initTestProvider();

        handler = new Handler();

        latitude = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        longitude = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);

        accuracy = AppPreferences.getAccuracy(this);

        fusedProvider = new FusedLocationsProvider(this);
        randomizer = new Randomizer();

        speed = intent.getIntExtra(JoystickControl.JOYSTICK_MAX_SPEED, 3);

        step = 0.0000001;
        step *= speed;
        updatesDelay = 500;

        if (spooferThread.getState() == Thread.State.NEW) {
            spooferThread.start();
        } else {
            spooferThread.interrupt();
            handler.removeCallbacks(runnable);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        spooferThread.interrupt();
        if (handler != null && runnable != null)
            handler.removeCallbacks(runnable);
        overlay.removeViews();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            float accuracy = randomizer.getAccuracy(JoystickService.this.accuracy);
            float altitude = randomizer.getElevation(120f, 5f);
            MockLocProvider.setNetworkProvider(latitude, longitude, accuracy, bearing, altitude);
            MockLocProvider.setGpsProvider(latitude, longitude,  bearing, speed, accuracy, altitude);
            fusedProvider.spoof(fusedProvider.build(latitude, longitude, accuracy, bearing, speed, altitude));

            handler.postDelayed(runnable, updatesDelay);
        }
    };

    Thread spooferThread = new Thread(runnable);

}
