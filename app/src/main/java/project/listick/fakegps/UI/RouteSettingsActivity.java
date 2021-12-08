package project.listick.fakegps.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.Locale;

import project.listick.fakegps.Contract.RouteSettingsImpl;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.R;

/*
 * Created by LittleAngry on 09.01.19 (macOS 10.12)
 * */
public class RouteSettingsActivity extends FragmentActivity implements RouteSettingsImpl.UI {

    private TextView mPauseAtStartingTimer;
    private EditText speedField;
    private EditText differenceField;

    private EditText elevation;
    private EditText elevationDiff;
    private Button mContinue;

    private CheckBox mClosedRoute;

    private View mPauseAtStartingContainer;
    private ShimmerFrameLayout mDetectingAltitude;

    private int mTimerMinutes;
    private int mTimerSeconds;

    public static void startActivity(Activity activity, double latitude, double longitude, double distance, boolean isRoute, boolean addMoreRoute, int requestCode) {
        activity.startActivityForResult(new Intent(activity, RouteSettingsActivity.class)
                .putExtra(ListickApp.LATITUDE, latitude)
                .putExtra(ListickApp.LONGITUDE, longitude)
                .putExtra(ListickApp.DISTANCE, distance)
                .putExtra(RouteSettingsPresenter.ADD_MORE_ROUTE, addMoreRoute)
                .putExtra(RouteSettingsPresenter.IS_ROUTE, isRoute), requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_activity);

        mContinue = findViewById(R.id.continue_action);
        View back = findViewById(R.id.back);

        speedField = findViewById(R.id.speed);
        differenceField = findViewById(R.id.speed_difference);
        elevation = findViewById(R.id.elevation);
        elevationDiff = findViewById(R.id.elevation_different);
        mClosedRoute = findViewById(R.id.closed_route);
        mDetectingAltitude = findViewById(R.id.detecting_altitude);
        mPauseAtStartingContainer = findViewById(R.id.pause_at_starting);
        mPauseAtStartingTimer = findViewById(R.id.parking_time);

        mDetectingAltitude.hideShimmer();

        TextView speedUnit = findViewById(R.id.speed_unit);
        TextView speedDiffUnit = findViewById(R.id.speed_diff_unit);

        String unitName = Preferences.getUnitName(this, Preferences.getStandartUnit(this));
        speedUnit.setText(unitName);
        speedDiffUnit.setText(unitName);

        final RouteSettingsPresenter presenter = new RouteSettingsPresenter(this);

        mContinue.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!speedField.getText().toString().isEmpty() && TextUtils.isDigitsOnly(speedField.getText().toString()) &&
                        !differenceField.getText().toString().isEmpty() && TextUtils.isDigitsOnly(differenceField.getText().toString())){
                    String elevationStr = RouteSettingsActivity.this.elevation.getText().toString();
                    String elevationDiffStr = RouteSettingsActivity.this.elevationDiff.getText().toString();

                    float elevation = 0;
                    float elevationDiff = 0;

                    if (!elevationStr.isEmpty()) {
                        elevation = Float.parseFloat(elevationStr);
                    }

                    if (!elevationDiffStr.isEmpty()) {
                        elevationDiff = Float.parseFloat(elevationDiffStr);
                    }
                    presenter.onContinueClick(Integer.parseInt(speedField.getText().toString()), Integer.parseInt(differenceField.getText().toString()), elevation, elevationDiff, mClosedRoute.isChecked());
                }

                presenter.saveElevation(Float.parseFloat(elevation.getText().toString()), Float.parseFloat(elevationDiff.getText().toString()));
            }
        });

        back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onCancelClick();
            }
        });

        presenter.onActivityLoad();
    }

    @Override
    public void getSpeed(int speed) {
        speedField.setText(String.valueOf(speed));
    }

    @Override
    public void getSpeedDifference(int difference) {
        differenceField.setText(String.valueOf(difference));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void pushDifferenceError() {
        Toast.makeText(this, R.string.difference_error, Toast.LENGTH_SHORT).show();

        differenceField.setBackground(getDrawable(R.drawable.uisearchbar_error));
        differenceField.startAnimation(AnimationUtils.loadAnimation(this, R.anim.attenuation));
        differenceField.getCompoundDrawables()[0].setTint(Color.WHITE);
        differenceField.setTextColor(getColor(R.color.white));
        differenceField.setOnTouchListener((view, motionEvent) -> {
            view.setBackground(getDrawable(R.drawable.uisearchbar));
            differenceField.setTextColor(getColor(R.color.uisearch_textcolor));
            differenceField.getCompoundDrawables()[0].setTint(getColor(R.color.uisearch_icon_tint));
            view.startAnimation(AnimationUtils.loadAnimation(RouteSettingsActivity.this, R.anim.attenuation));
            view.setOnTouchListener(null);
            return false;
        });
    }

    @Override
    public void getElevation(float elevation, float difference) {
        this.elevation.setText(String.valueOf(elevation));
        this.elevationDiff.setText(String.valueOf(difference));
    }

    @Override
    public void startAltitudeDetection() {
        mContinue.setEnabled(false);
        mContinue.setText(R.string.detecting_altitude);
        mDetectingAltitude.showShimmer(true);
    }

    @Override
    public void stopAltitudeDetection() {
        mContinue.setEnabled(true);
        mContinue.setText(R.string.continue_text);
        mDetectingAltitude.hideShimmer();
        elevation.startAnimation(AnimationUtils.loadAnimation(this, R.anim.attenuation));
    }

    @Override
    public void onAltitudeDetermined(boolean success, boolean isRoute) {
        PrettyToast.show(this, success ? getString(R.string.altitude_determined) : getString(R.string.altitude_detection_failed), R.drawable.ic_terrain);

        LinearLayout elevationController = findViewById(R.id.elevation_controller);
        TextView autoAltitude = findViewById(R.id.auto_elevation);

        if (success && isRoute) {
            elevationController.setVisibility(View.GONE);
            autoAltitude.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setFixedMode() {
        LinearLayout speedContainer = findViewById(R.id.speeds_container);
        TextView activityTitle = findViewById(R.id.title);
        activityTitle.setText(R.string.spoofing);
        speedContainer.setVisibility(View.GONE);
        mClosedRoute.setVisibility(View.GONE);
    }

    @Override
    public void addMoreRoute() {
        mClosedRoute.setVisibility(View.GONE);
        mPauseAtStartingContainer.setVisibility(View.VISIBLE);
        mPauseAtStartingTimer.setOnClickListener(view -> {

            TimePickerDialog tm = new TimePickerDialog(this, (minutes, seconds) -> {
                String format = String.format(Locale.ENGLISH, "%d:%d", minutes, seconds);
                mTimerMinutes = minutes;
                mTimerSeconds = seconds;

                if (minutes < 9)
                    format = String.format(Locale.ENGLISH, "0%d:%d", minutes, seconds);

                if (seconds < 9)
                    format = String.format(Locale.ENGLISH, "%d:0%d", minutes, seconds);

                if (minutes < 9 && seconds < 9)
                    format = String.format(Locale.ENGLISH, "0%d:0%d", minutes, seconds);

                mPauseAtStartingTimer.setText(format);

            });
            tm.show(getString(R.string.parking_time), getString(R.string.enter_parking_time));

        });
    }

    @Override
    public int getTimerMinutes() {
        return mTimerMinutes;
    }

    @Override
    public int getTimerSeconds() {
        return mTimerSeconds;
    }


}
