package project.listick.fakegps.UI;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Locale;

import project.listick.fakegps.AppPreferences;
import project.listick.fakegps.Contract.MapsImpl;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.LocationOperations;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Presenter.MapsPresenter;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.R;
import project.listick.fakegps.Services.RouteSpooferService;

/*
 * Created by LittleAngry on 25.12.18 (macOS 10.12)
 * */
public class MapsActivity extends Edge2EdgeActivity implements MapsImpl.UIImpl, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private MapView mMap;

    private MapsPresenter mPresenter;

    private TextView mSearchLayout;
    private MaterialButton mStopContainer;
    private CardView mActiveRouteLayout;
    private MaterialButton mPauseContainer;
    private MaterialButton mEditContainer;
    private MaterialButton mDoneContainer;
    private MaterialButton mAddMoreRoute;
    private MaterialButton mRestoreLocation;

    private View mJoystickMessage;
    private MaterialButton mRemoveRoute;

    private TextView mSpeedInfo;
    private TextView mDistanceInfo;
    private TextView mSourceAddress;
    private TextView mWhereTo;

    private ImageView mMenuIcon;

    private BottomSheetBehavior mBottomSheet;
    private View mProgressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_material3);

        mMap = findViewById(R.id.map);
        MapLoader mapLoader = new MapLoader(this);
        mapLoader.load(mMap, findViewById(R.id.copyright_txt));

        mPresenter = new MapsPresenter(mMap, this);

        MaterialButton getLocation = findViewById(R.id.getlocation_container);

        mStopContainer = findViewById(R.id.stop_button);
        mPauseContainer = findViewById(R.id.pause_button);
        mSpeedInfo = findViewById(R.id.speedometer);
        mDistanceInfo = findViewById(R.id.distance_info);
        mDoneContainer = findViewById(R.id.start_spoofing);
        mEditContainer = findViewById(R.id.edit_button);
        mAddMoreRoute = findViewById(R.id.add_more_points);
        mRestoreLocation = findViewById(R.id.restore_location_button);

        mJoystickMessage = findViewById(R.id.joystick_mode_message);

        mPauseContainer.setOnClickListener(view -> mPresenter.handlePause());

        mRemoveRoute = findViewById(R.id.remote_route);

        mSearchLayout = findViewById(R.id.where_to);
        mWhereTo = findViewById(R.id.where_to);
        mActiveRouteLayout = findViewById(R.id.active_route_info);
        mMenuIcon = findViewById(R.id.settings);

        mSourceAddress = findViewById(R.id.firstAddress);
        mDrawerLayout = findViewById(R.id.drawer);

        mNavigationView = findViewById(R.id.navigation_header_container);
        mNavigationView.setNavigationItemSelectedListener(this);

        mMap.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                mPresenter.onMapDrag();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }

        }, 100));

        mMap.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return false;
        });

        // Douglas-Peucker algorithm
        /*
        mMap.addMapListener(new DelayedMapListener(new ZoomListener(new ZoomListener.IZoomListener() {
            ArrayList<GeoPoint> optimized;

            @Override
            public void onZoomChanged(double startZoomLevel, final double endZoomLevel) {
                if (SpoofingPlaceInfo.isRoute && endZoomLevel <= 9) {
                    if (MapUtil.pointsBackup != null && !MapUtil.pointsBackup.isEmpty()) {

                        Thread douglasPeucker = new Thread(() -> {

                            for (Overlay overlay : mMap.getOverlays()) {
                                if (overlay instanceof Polyline) {
                                    mMap.getOverlays().remove(overlay);
                                }
                            }

                            optimized = MapUtil.optimizePoly(endZoomLevel);
                            runOnUiThread(() -> MapUtil.drawPath(mMap, optimized));
                            optimized = null;

                        });
                        douglasPeucker.start();
                    }


                } else if (SpoofingPlaceInfo.isRoute && MapUtil.pointsBackup != null && !MapUtil.pointsBackup.isEmpty() && optimized != null) {

                    for (Overlay overlay : mMap.getOverlays()) {
                        if (overlay instanceof Polyline) {
                            mMap.getOverlays().remove(overlay);
                            MapUtil.drawPath(mMap, MapUtil.pointsBackup);
                        }
                    }
                }

            }
        }), 300));
        */

        mMenuIcon.setOnClickListener(v -> mPresenter.onMenu());
        mRemoveRoute.setOnClickListener(v -> mPresenter.onRouteRemove());
        mEditContainer.setOnClickListener(v -> mPresenter.changePoint());
        mJoystickMessage.setOnClickListener(v -> mPresenter.startJoystickActivity());
        mDoneContainer.setOnClickListener(v -> mPresenter.onSpoofClick(new GeoPoint(mMap.getMapCenter().getLatitude(), mMap.getMapCenter().getLongitude())));
        mStopContainer.setOnClickListener(view -> mPresenter.handleStop());
        mAddMoreRoute.setOnClickListener(view -> mPresenter.onAddMoreRoute(ActivityOptionsCompat.makeSceneTransitionAnimation(this, mSearchLayout, "whereTo")));
        mRestoreLocation.setOnClickListener(v -> mPresenter.handleClear());

        getLocation.setOnClickListener(v -> mPresenter.onCurrentLocationClick());

        lockSearchBar(false);

        mBottomSheet = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        mMap.setOnApplyWindowInsetsListener((v, insets) -> {

            int topInset = insets.getSystemWindowInsetTop();
            int bottomInset = insets.getSystemWindowInsetBottom();

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLocation.getLayoutParams();
            params.bottomMargin = bottomInset + params.bottomMargin;
            params = (ViewGroup.MarginLayoutParams) mMenuIcon.getLayoutParams();
            params.topMargin = topInset;

            mBottomSheet.setPeekHeight(findViewById(R.id.search_layout).getMeasuredHeight() + bottomInset);

            params = (ViewGroup.MarginLayoutParams) findViewById(R.id.active_route_info).getLayoutParams();
            params.bottomMargin = bottomInset;

            mNavigationView.setPadding(0, topInset, 0, 0);
            mJoystickMessage.setPadding(0, topInset, 0, 0);
            return insets.consumeSystemWindowInsets();
        });

        mBottomSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_COLLAPSED == newState)
                    mActiveRouteLayout.setAlpha(0);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                getLocation.animate().translationY(1 - (slideOffset * 300)).setDuration(0).start();
                mActiveRouteLayout.animate().alpha(0 + slideOffset).alpha(0+ slideOffset).setDuration(0).start();
                mRestoreLocation.animate().alpha(0 + slideOffset).alpha(0+ slideOffset).setDuration(0).start();
            }
        });
        mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mActiveRouteLayout.setAlpha(0);
        mRestoreLocation.setAlpha(0);

        mPresenter.onActivityLoad();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SearchActivity.ACTIVITY_REQUEST_CODE && data != null) {
            if (mPresenter != null)
                mPresenter.onRoute(data);
        }

        if (requestCode == LocationOperations.ROUTE_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            mPresenter.setRoutingMode();
            mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        if (requestCode == RouteSettingsPresenter.ANOTHER_ROUTE_ADDED && resultCode == RESULT_CANCELED) {
            mPresenter.removeLatestRoute(); // remove latest route
        }

        if (requestCode == LocationOperations.FIXED_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            mPresenter.setFixedMode();
        }

        if (requestCode == BookmarksActivity.BOOKMARKS_REQUEST_CODE) {
            if (mPresenter != null)
                mPresenter.onBookmarkResult(data, resultCode);
        }
        if (requestCode == MockLocationPermissionActivity.ML_GRANTED_REQUEST_CODE && resultCode == RESULT_OK) {
            if (mPresenter != null)
                mPresenter.onSpoofClick(new GeoPoint(mMap.getMapCenter().getLatitude(), mMap.getMapCenter().getLongitude()));
        }

        mMap.invalidate();
    }

    @Override
    public void alertDialog(String dialogTitle, String dialogMessage, boolean setCancelable, String posButtonLabel,
                            DialogInterface.OnClickListener posButtonHandler, String negativeButtonLabel, DialogInterface.OnClickListener negativeButtonHandler, @DrawableRes int iconRes) {

        negativeButtonHandler = (dialog, id) -> dialog.cancel();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
        builder.setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(posButtonLabel, posButtonHandler)
                .setNegativeButton(negativeButtonLabel, negativeButtonHandler)
                .setIcon(iconRes)
                .show();
    }

    @Override
    public void alertDialog(String dialogTitle, Spanned dialogMessage, boolean setCancelable, String posButtonLabel, DialogInterface.OnClickListener posButtonHandler, String negativeButtonLabel, DialogInterface.OnClickListener negativeButtonHandler, int iconRes) {
        negativeButtonHandler = (dialog, id) -> dialog.cancel();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
        AlertDialog dialog = builder.setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(posButtonLabel, posButtonHandler)
                .setNegativeButton(negativeButtonLabel, negativeButtonHandler)
                .setIcon(iconRes)
                .create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void startSpoofingVisibility(int visibility) {
        mDoneContainer.setVisibility(visibility);
    }

    @Override
    public void setRouteInfo(int visibility) {
        mSpeedInfo.setText("0 " + AppPreferences.getUnitName(this, AppPreferences.getStandartUnit(this)));
        mDistanceInfo.setText("0/0");
    }

    @Override
    public void enablePause(int visibility) {
        mPauseContainer.setVisibility(visibility);
    }

    @Override
    public void enableStop(int visibility) {
        mStopContainer.setVisibility(visibility);
    }

    @Override
    public void setPauseIcon(boolean isPaused) {
        if (isPaused) {
            mPauseContainer.setIconResource(R.drawable.ic_resume);
            mPauseContainer.setText(R.string.resume);
        } else {
            mPauseContainer.setIconResource(R.drawable.ic_pause);
            mPauseContainer.setText(R.string.pause);
        }
    }

    @Override
    public void updateRouteInfo(int speed, double passedDistance, double distance) {
        String unit = AppPreferences.getUnitName(this, AppPreferences.getStandartUnit(this));

        String format = String.format(Locale.ENGLISH, "%.2f", passedDistance) + "/" + String.format(Locale.ENGLISH, "%.2f", distance);

        StringBuilder distanceUi = new StringBuilder();
        distanceUi.append(format)
                .append(" ");
                //.append(unit);

        mSpeedInfo.setText(String.format(Locale.ENGLISH, "%d %s", speed, unit));
        mDistanceInfo.setText(distanceUi);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void openMenu(boolean isOpen) {
        if (isOpen) mDrawerLayout.openDrawer(Gravity.START);
        else mDrawerLayout.closeDrawer(Gravity.END);
    }

    @Override
    public void removeMenuItem(int itemId) {
        mNavigationView.getMenu().removeItem(itemId);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean isServiceWorks = PermissionManager.isServiceRunning(getApplicationContext(), RouteSpooferService.class);
        if (!isServiceWorks)
            mPresenter.removeAllRoutes();

        setTheme(R.style.AppTheme);
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        IGeoPoint mapCenter = mMap.getMapCenter();
        outState.putDouble(ListickApp.LATITUDE, mapCenter.getLatitude());
        outState.putDouble(ListickApp.LONGITUDE, mapCenter.getLongitude());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        GeoPoint lastSavedPoint = new GeoPoint(savedInstanceState.getDouble(ListickApp.LATITUDE, 0f), savedInstanceState.getDouble(ListickApp.LONGITUDE, 0f));
        mMap.getController().animateTo(lastSavedPoint);
    }

    @Override
    public void setAddress(String address) {
        mSourceAddress.setText(address);
    }

    @Override
    public String getWhereToAddress() {
        return mWhereTo.getText().toString();
    }

    @Override
    public void setJoystickMsgVisiblity(int visiblity) {
        mJoystickMessage.setVisibility(visiblity);
    }

    @Override
    public void toggleEditButton(int show) {
        mEditContainer.setVisibility(show);
    }

    @Override
    public void setAddressShimmer(boolean enable) {
        ShimmerFrameLayout addressShimmer = findViewById(R.id.address_shimmer);
        if (enable) {
            addressShimmer.showShimmer(true);
        } else {
            addressShimmer.stopShimmer();
            addressShimmer.hideShimmer();
        }
    }

    @Override
    public void setAddMoreRoute(int visibilty) {
        mAddMoreRoute.setVisibility(visibilty);
        mMap.requestApplyInsets();
    }

    @Override
    public void setLocationDisabledNotification(int visibilty) {
        MaterialButton layout = findViewById(R.id.please_enable_location_services);

        layout.setOnClickListener(v -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), -1));

        layout.setVisibility(visibilty);
        mMap.requestApplyInsets();
    }

    @Override
    public void toggleRemoveRoute(int status) {
        mRemoveRoute.setVisibility(status);
    }

    @Override
    public void setWhereToAddress(String address) {
        mWhereTo.setText(address);
    }

    @Override
    public void lockSearchBar(boolean isLocked) {

        final View.OnClickListener searchLayoutClickListener;

        if (isLocked) {
            searchLayoutClickListener = v -> {
                Dialog.OnClickListener stopSpoofing = (dialogInterface, i) -> {
                    mPresenter.handleStop();
                    mSearchLayout.performClick();
                };

                alertDialog(getString(R.string.warning), getString(R.string.search_locked_description), true,
                        getString(R.string.stop), stopSpoofing, getString(R.string.cancel), null, R.drawable.ic_round_warning_48);
            };

        } else {
            searchLayoutClickListener = new OnSingleClickListener() {
                @Override
                public void onSingleClick(final View v) {
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MapsActivity.this, mSearchLayout, "whereTo");

                    String originAddress = mSourceAddress.getText().toString();
                    double latitude = mMap.getMapCenter().getLatitude();
                    double longitude = mMap.getMapCenter().getLongitude();

                    SearchActivity.startActivity(MapsActivity.this, originAddress, latitude, longitude, false, activityOptionsCompat.toBundle());
                }
            };
        }


        mSearchLayout.setOnClickListener(searchLayoutClickListener);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawers();

        mPresenter.onItem(menuItem);
        return false;
    }

    @Override
    public void inflateProgressLayout(View.OnClickListener onCancel) {
        LinearLayout progressLayoutContainer = findViewById(R.id.route_building_status_container);
        progressLayoutContainer.setVisibility(View.VISIBLE);
        mProgressDialog = getLayoutInflater().inflate(R.layout.route_building_fullscreen_dialog, null);
        mProgressDialog.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        progressLayoutContainer.addView(mProgressDialog);

        progressLayoutContainer.findViewById(R.id.cancel).setOnClickListener(onCancel);
        mProgressDialog.findViewById(R.id.loading).setOnClickListener(v -> { });
        mDoneContainer.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.buttonUnavailable));
        mDoneContainer.setEnabled(false);
    }

    @Override
    public void removeProgressLayout() {
        LinearLayout progressLayoutContainer = findViewById(R.id.route_building_status_container);
        progressLayoutContainer.removeView(mProgressDialog);
        progressLayoutContainer.setVisibility(View.GONE);

        mDoneContainer.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.start_spoofing));
        mDoneContainer.setEnabled(true);
    }
}
