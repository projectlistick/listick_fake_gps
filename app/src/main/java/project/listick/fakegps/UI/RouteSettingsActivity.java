package project.listick.fakegps.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;

import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.Contract.RouteSettingsImpl;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.R;

/*
 * Created by LittleAngry on 09.01.19 (macOS 10.12)
 * */
public class RouteSettingsActivity extends FragmentActivity implements RouteSettingsImpl.UI {

    private TextView mPauseAtStartingTimer;
    private TextView mLatestPointDelayTimer;
    private EditText speedField;
    private EditText differenceField;

    private EditText elevation;
    private EditText elevationDiff;
    private MaterialButton mContinue;

    private CheckBox mClosedRoute;

    private View mPauseAtStartingContainer;
    private View mLatestPointDelayContainer;
    private ShimmerFrameLayout mDetectingAltitude;

    private int mOriginTimerMinutes;
    private int mOriginTimerSeconds;

    private int mDestTimerMinutes;
    private int mDestTimerSeconds;

    private RouteSettingsPresenter mPresenter;

    public static void startActivity(Activity activity, double latitude, double longitude, double distance, boolean isRoute, boolean addMoreRoute, int requestCode) {
        activity.startActivityForResult(new Intent(activity, RouteSettingsActivity.class)
                .putExtra(ListickApp.LATITUDE, latitude)
                .putExtra(ListickApp.LONGITUDE, longitude)
                .putExtra(ListickApp.DISTANCE, distance)
                .putExtra(RouteSettingsPresenter.ADD_MORE_ROUTE, addMoreRoute)
                .putExtra(RouteSettingsPresenter.IS_ROUTE, isRoute), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CaptchaActivity.ACTIVITY_REQUEST_CODE) {
            if (mPresenter != null && resultCode == RESULT_OK) {
                String challengeResult = data.getStringExtra(CaptchaActivity.KEY_CAPTCHA_RESULT);
                mPresenter.onChallengePassed(challengeResult);
            } else if (mPresenter != null) {
                stopAltitudeDetection();
                mPresenter.setElevation();
                onAltitudeDetermined(false, false);
            }
        }

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

        mLatestPointDelayContainer = findViewById(R.id.delay_at_the_last_point);
        mLatestPointDelayTimer = findViewById(R.id.datlp_timepicker);

        mPauseAtStartingContainer = findViewById(R.id.pause_at_starting);
        mPauseAtStartingTimer = findViewById(R.id.parking_time);

        mDetectingAltitude.hideShimmer();

        TextView speedUnit = findViewById(R.id.speed_unit);
        TextView speedDiffUnit = findViewById(R.id.speed_diff_unit);

        String unitName = AppPreferences.getUnitName(this, AppPreferences.getStandartUnit(this));
        speedUnit.setText(unitName);
        speedDiffUnit.setText(unitName);

        mPresenter = new RouteSettingsPresenter(this);

        mContinue.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!speedField.getText().toString().isEmpty() && TextUtils.isDigitsOnly(speedField.getText().toString()) &&
                        !differenceField.getText().toString().isEmpty() && TextUtils.isDigitsOnly(differenceField.getText().toString())) {
                    String elevationStr = RouteSettingsActivity.this.elevation.getText().toString();
                    String elevationDiffStr = RouteSettingsActivity.this.elevationDiff.getText().toString();

                    float elevation = 0;
                    float elevationDiff = 0;

                    if (!elevationStr.isEmpty()) {
                        try {
                            elevation = Float.parseFloat(elevationStr);
                        } catch (NumberFormatException e) {
                            UIEffects.TextView.attachErrorWithShake(RouteSettingsActivity.this, RouteSettingsActivity.this.elevation, () -> {
                            });
                            return;
                        }
                    }

                    if (!elevationDiffStr.isEmpty()) {
                        try {
                            elevationDiff = Float.parseFloat(elevationDiffStr);
                        } catch (NumberFormatException e) {
                            UIEffects.TextView.attachErrorWithShake(RouteSettingsActivity.this, RouteSettingsActivity.this.elevationDiff, () -> {
                            });
                            return;
                        }
                    }
                    mPresenter.onContinueClick(Integer.parseInt(speedField.getText().toString()), Integer.parseInt(differenceField.getText().toString()), elevation, elevationDiff, mClosedRoute.isChecked());
                }

                mPresenter.saveElevation(Float.parseFloat(elevation.getText().toString()), Float.parseFloat(elevationDiff.getText().toString()));


            }
        });

        back.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mPresenter.onCancelClick();
            }
        });

        // Origin timer
        mPauseAtStartingTimer.setOnClickListener(view -> {

            TimePickerDialog tm = new TimePickerDialog(this, (minutes, seconds, format) -> {
                mOriginTimerMinutes = minutes;
                mOriginTimerSeconds = seconds;


                mPauseAtStartingTimer.setText(format);

            });
            tm.show();

        });

        // Dest timer
        mLatestPointDelayTimer.setOnClickListener(view -> {

            TimePickerDialog tm = new TimePickerDialog(this, (minutes, seconds, format) -> {
                mDestTimerMinutes = minutes;
                mDestTimerMinutes = seconds;

                mLatestPointDelayTimer.setText(format);

            });
            tm.show(getString(R.string.delay_at_the_last_point), "");

        });

        mLatestPointDelayContainer.setVisibility(View.GONE);

        mPresenter.onActivityLoad();
    }

    @Override
    public void getSpeed(int speed) {
        speedField.setText(String.valueOf(speed));
    }

    @Override
    public void getSpeedDifference(int difference) {
        differenceField.setText(String.valueOf(difference));
    }

    @Override
    public void pushDifferenceError() {
        Toast.makeText(this, R.string.difference_error, Toast.LENGTH_SHORT).show();

        UIEffects.TextView.attachErrorWithShake(this, differenceField, () -> {
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

        mPauseAtStartingContainer.setVisibility(View.GONE);
        mLatestPointDelayContainer.setVisibility(View.GONE);
    }

    @Override
    public void addMoreRoute() {
        mClosedRoute.setVisibility(View.GONE);
        mLatestPointDelayContainer.setVisibility(View.GONE);
    }

    @Override
    public int getOriginTimerMinutes() {
        return mOriginTimerMinutes;
    }

    @Override
    public int getOriginTimerSeconds() {
        return mOriginTimerSeconds;
    }

    @Override
    public int getDestTimerMinutes() {
        return mDestTimerMinutes;
    }

    @Override
    public int getDestTimerSeconds() {
        return mDestTimerSeconds;
    }


}
