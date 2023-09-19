package project.listick.fakegps.UI;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.CurrentLocation;
import project.listick.fakegps.Services.JoystickService;
import project.listick.fakegps.JoystickControl;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.R;
import project.listick.fakegps.SpoofingPlaceInfo;

public class JoystickActivity extends Activity {


    public static final int JOYSTICK_SELECT_ON_MAP_REQUEST = 1;
    public static final String JOYSTICK_SELECT_DEST = "joystick_select_dest";

    private double latitude;
    private double longitude;

    private double inputLat;
    private double inputLng;

    private TextView selectOnMap;
    private TextView myLocation;

    private boolean isServiceRunning;
    private boolean isCanStartSpoofing;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JOYSTICK_SELECT_ON_MAP_REQUEST && resultCode == RESULT_OK) {
            isCanStartSpoofing = true;
            latitude = data.getDoubleExtra(ListickApp.LATITUDE, 0d);
            longitude = data.getDoubleExtra(ListickApp.LONGITUDE, 0d);
            selectOnMap.setText(data.getStringExtra(SpoofingPlaceInfo.ADDRESS));
            selectOnMap.setMaxLines(2);
            return;
        }
        isCanStartSpoofing = false;
        selectOnMap.setText(R.string.select_on_map);
        selectOnMap.setBackground(ContextCompat.getDrawable(this, R.drawable.material_ripple));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        selectOnMap = findViewById(R.id.select_on_map);
        myLocation = findViewById(R.id.from_current_loc);
        Button startService = findViewById(R.id.btn_continue);
        TextView speedUnit = findViewById(R.id.speed_unit);
        final EditText maxSpeed = findViewById(R.id.max_speed);

        Intent intent = getIntent();
        inputLat = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        inputLng = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);

        final SharedPreferences joystickPrefs = getSharedPreferences(JoystickControl.JOYSTICK_PREFERENCES, Context.MODE_PRIVATE);

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        myLocation.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void onSingleClick(View v) {
                isServiceRunning = PermissionManager.isServiceRunning(JoystickActivity.this, JoystickService.class);

                if (isServiceRunning) {
                    Toast.makeText(getApplicationContext(), R.string.firstly_close_joystick, Toast.LENGTH_SHORT).show();
                    return;
                }

                DialogInterface.OnClickListener click = (dialogInterface, i) -> dialogInterface.cancel();

                if (lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (ActivityCompat.checkSelfPermission(JoystickActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(JoystickActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location gpsLastLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Location networkLastLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


                        if (gpsLastLoc != null && CurrentLocation.isBetterLocation(gpsLastLoc, networkLastLoc)) {
                            latitude = gpsLastLoc.getLatitude();
                            longitude = gpsLastLoc.getLongitude();
                        } else if (networkLastLoc != null && CurrentLocation.isBetterLocation(networkLastLoc, gpsLastLoc)) {
                            latitude = networkLastLoc.getLatitude();
                            longitude = networkLastLoc.getLongitude();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(JoystickActivity.this);
                            builder.setTitle(R.string.warning)
                                    .setMessage(R.string.failed_get_currentloc)
                                    .setCancelable(true)
                                    .setPositiveButton(R.string.okay, click)
                                    .setNegativeButton(R.string.cancel, click);
                            AlertDialog alert = builder.create();
                            alert.show();
                            return;
                        }
                    } else {

                        myLocation.startAnimation(AnimationUtils.loadAnimation(JoystickActivity.this, R.anim.attenuation));
                        myLocation.getCompoundDrawablesRelative()[0].setTint(getColor(R.color.error));
                        myLocation.setTextColor(getColor(R.color.error));
                        myLocation.setText(R.string.failed_get_currentloc);

                        CountDownTimer timer = new CountDownTimer(2000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                myLocation.startAnimation(AnimationUtils.loadAnimation(JoystickActivity.this, R.anim.attenuation));
                                myLocation.getCompoundDrawablesRelative()[0].setTint(getColor(R.color.uicolor));
                                myLocation.setTextColor(getColor(R.color.uicolor_text_medium));
                                myLocation.setText(R.string.from_current_loc);
                            }
                        };
                        timer.start();
                        return;
                    }
                }


                isCanStartSpoofing = true;
                selectOnMap.setText(R.string.select_on_map);
                selectOnMap.setBackground(ContextCompat.getDrawable(JoystickActivity.this, R.drawable.material_ripple));
                v.setBackground(ContextCompat.getDrawable(JoystickActivity.this, R.drawable.material_ripple_pressed));
            }
        });

        selectOnMap.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                isServiceRunning = PermissionManager.isServiceRunning(JoystickActivity.this, JoystickService.class);

                if (isServiceRunning) {
                    Toast.makeText(JoystickActivity.this, R.string.firstly_close_joystick, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(JoystickActivity.this, SelectPointActivity.class);
                intent.putExtra(ListickApp.LATITUDE, inputLat);
                intent.putExtra(ListickApp.LONGITUDE, inputLng);
                intent.putExtra(JOYSTICK_SELECT_DEST, true);

                isCanStartSpoofing = false;
                myLocation.setBackground(ContextCompat.getDrawable(JoystickActivity.this, R.drawable.material_ripple));
                v.setBackground(ContextCompat.getDrawable(JoystickActivity.this, R.drawable.material_ripple_pressed));
                startActivityForResult(intent, JOYSTICK_SELECT_ON_MAP_REQUEST);
            }
        });

        startService.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                boolean isMockLocationEnabled = PermissionManager.isMockLocationsEnabled(JoystickActivity.this);
                boolean isSystemApp = PermissionManager.isSystemApp(JoystickActivity.this);

                if (!(isMockLocationEnabled || isSystemApp)) {
                    startActivity(new Intent(JoystickActivity.this, MockLocationPermissionActivity.class));
                    return;
                }

                if (maxSpeed.getText().toString().isEmpty())
                    return;

                isServiceRunning = PermissionManager.isServiceRunning(JoystickActivity.this, JoystickService.class);
                if (isServiceRunning) {
                    stopService(new Intent(JoystickActivity.this, JoystickService.class));
                    ((Button) v).setText(getString(R.string.start_joystick));
                    return;
                }

                if (isCanStartSpoofing) {
                    ((Button) v).setText(getString(R.string.stop_joystick));

                    int speed = Integer.parseInt(maxSpeed.getText().toString());

                    joystickPrefs.edit()
                            .putInt(JoystickControl.JOYSTICK_MAX_SPEED, speed).apply();

                    startService(new Intent(JoystickActivity.this, JoystickService.class)
                            .putExtra(ListickApp.LATITUDE, latitude)
                            .putExtra(ListickApp.LONGITUDE, longitude)
                            .putExtra(JoystickControl.JOYSTICK_MAX_SPEED, speed));



                } else {
                    Toast.makeText(JoystickActivity.this, getString(R.string.select_start_point), Toast.LENGTH_SHORT).show();
                }
            }
        });

        isServiceRunning = PermissionManager.isServiceRunning(JoystickActivity.this, JoystickService.class);
        maxSpeed.setText(String.valueOf(joystickPrefs.getInt(JoystickControl.JOYSTICK_MAX_SPEED, 3)));

        if (isServiceRunning) {
            startService.setText(getString(R.string.stop_joystick));
        } else {
            myLocation.performClick();
        }

        speedUnit.setText(AppPreferences.getUnitName(this, AppPreferences.getStandartUnit(this)));

    }
}
