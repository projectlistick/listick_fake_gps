package project.listick.fakegps.UI;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
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

import project.listick.fakegps.Contract.MapsImpl;
import project.listick.fakegps.Daemons.RouteSpooferService;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.LocationOperations;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.Presenter.MapsPresenter;
import project.listick.fakegps.Presenter.RouteSettingsPresenter;
import project.listick.fakegps.R;
import project.listick.fakegps.SpoofingPlaceInfo;

/*
 * Created by LittleAngry on 25.12.18 (macOS 10.12)
 * */
public class MapsActivity extends Edge2EdgeActivity implements MapsImpl.UIImpl, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;

    private MapView mMap;

    private MapsPresenter mPresenter;

    private RelativeLayout mSearchLayout;
    private RelativeLayout mStopContainer;
    private RelativeLayout mSpeedbarLayout;
    private RelativeLayout mPauseContainer;
    private RelativeLayout mEditContainer;
    private RelativeLayout mDoneContainer;
    private RelativeLayout mAddMoreRoute;

    private View mJoystickMessage;
    private View mRemoveRoute;

    private TextView mSpeedbarInfo;
    private TextView mSourceAddress;
    private TextView mWhereTo;

    private ImageView mMenuIcon;
    private ImageView mPauseIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_redesigned);

        mMap = findViewById(R.id.map);
        MapLoader mapLoader = new MapLoader(this);
        mapLoader.load(mMap, findViewById(R.id.copyright_txt));

        mPresenter = new MapsPresenter(mMap, this);

        RelativeLayout getLocation = findViewById(R.id.getlocation_container);
        RelativeLayout loading = findViewById(R.id.loading);

        mStopContainer = findViewById(R.id.stop_container);
        mPauseContainer = findViewById(R.id.pause_container);
        mSpeedbarInfo = findViewById(R.id.speedbar_info);
        mDoneContainer = findViewById(R.id.done_container);
        mPauseIcon = findViewById(R.id.pause);
        mEditContainer = findViewById(R.id.edit_container);
        mAddMoreRoute = findViewById(R.id.add_more_points);

        mJoystickMessage = findViewById(R.id.joystick_mode_message);

        mPauseContainer.setOnClickListener(view -> mPresenter.handlePause());

        mRemoveRoute = findViewById(R.id.remove_route_view);

        mSearchLayout = findViewById(R.id.where_to_container);
        mWhereTo = findViewById(R.id.where_to);
        mSpeedbarLayout = findViewById(R.id.speed_fragment);
        mMenuIcon = findViewById(R.id.settings);

        mSourceAddress = findViewById(R.id.firstAddress);
        mDrawerLayout = findViewById(R.id.drawer);

        NavigationView navigationView = findViewById(R.id.navigation_header_container);
        navigationView.setNavigationItemSelectedListener(this);

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

        getLocation.setOnClickListener(v -> mPresenter.onCurrentLocationClick());
        loading.setOnClickListener(v -> { });

        lockSearchBar(false);

        RelativeLayout copyright = findViewById(R.id.copyright);
        int speedbarBottomMargin = ((ViewGroup.MarginLayoutParams) mSpeedbarLayout.getLayoutParams()).bottomMargin;
        int searchLayoutTopMargin = ((ViewGroup.MarginLayoutParams) findViewById(R.id.search_layout).getLayoutParams()).topMargin;

        getLocation.setOnApplyWindowInsetsListener((v, insets) -> {

            int topInset = insets.getSystemWindowInsetTop();
            int bottomInset = insets.getSystemWindowInsetBottom();
            boolean isGesturesEnabled = false;

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLocation.getLayoutParams();
            params.bottomMargin = bottomInset + params.bottomMargin;
            params = (ViewGroup.MarginLayoutParams) mMenuIcon.getLayoutParams();
            params.topMargin = topInset;

            params = (ViewGroup.MarginLayoutParams) copyright.getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (insets.getSystemGestureInsets().bottom != 0
                        && insets.getSystemGestureInsets().left != 0
                        && insets.getSystemGestureInsets().right != 0) {
                    isGesturesEnabled = true;
                }
            }
            if (!isGesturesEnabled) {
                params.bottomMargin = bottomInset;
            }
            params = (ViewGroup.MarginLayoutParams) findViewById(R.id.search_layout).getLayoutParams();
            params.topMargin = topInset + searchLayoutTopMargin;

            params = (ViewGroup.MarginLayoutParams) mSpeedbarLayout.getLayoutParams();
            params.bottomMargin = bottomInset + speedbarBottomMargin;

            navigationView.setPadding(0, topInset, 0, 0);
            mJoystickMessage.setPadding(0, topInset, 0, 0);
            return insets.consumeSystemWindowInsets();
        });

        mPresenter.onActivityLoad();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SearchActivity.ACTIVITY_REQUEST_CODE && data != null) {
            double sourceLat = data.getDoubleExtra(SpoofingPlaceInfo.ORIGIN_LAT, 0f);
            double sourceLong = data.getDoubleExtra(SpoofingPlaceInfo.ORIGIN_LNG, 0f);

            double destLat = data.getDoubleExtra(SpoofingPlaceInfo.DEST_LAT, 0f);
            double destLong = data.getDoubleExtra(SpoofingPlaceInfo.DEST_LNG, 0f);

            setWhereToAddress(data.getStringExtra(SpoofingPlaceInfo.DEST_ADDRESS));

            ERouteTransport transport = (ERouteTransport) data.getSerializableExtra(SpoofingPlaceInfo.TRANSPORT);

            lockSearchBar(true);

            if (mPresenter != null && destLat != 0f)
                mPresenter.onRoute(sourceLat, sourceLong, destLat, destLong, transport);
        }

        if (requestCode == LocationOperations.ROUTE_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            mPresenter.setRoutingMode();
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
    public void enableDone(int visibility) {
        mDoneContainer.setVisibility(visibility);
    }

    @Override
    public void enableSpeedbar(int visibility) {
        mSpeedbarLayout.setVisibility(visibility);
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
        if (isPaused)
            mPauseIcon.setImageDrawable(getDrawable(R.drawable.ic_resume));
        else
            mPauseIcon.setImageDrawable(getDrawable(R.drawable.ic_pause));
    }

    @Override
    public void updateSpeedbar(int speed, double passedDistance, double distance) {
        String unit = Preferences.getUnitName(this, Preferences.getStandartUnit(this));

        String format = String.format(Locale.ENGLISH, "%.2f", passedDistance) + "/" + String.format(Locale.ENGLISH, "%.2f", distance) + " " + speed;

        StringBuilder distanceUi = new StringBuilder();
        distanceUi.append(format)
                .append(" ")
                .append(unit);

        mSpeedbarInfo.setText(distanceUi);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void useMenu(boolean isOpen) {
        if (isOpen) mDrawerLayout.openDrawer(Gravity.START);
        else mDrawerLayout.closeDrawer(Gravity.END);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean isServiceWorks = PermissionManager.isServiceRunning(getApplicationContext(), RouteSpooferService.class);
        if (!isServiceWorks)
            mPresenter.removeRoute();

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
    public void getAddress(String address) {
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
    }

    @Override
    public void setLocationDisabledNotification(int visibilty) {
        RelativeLayout layout = findViewById(R.id.please_enable_location_services);

        layout.setOnClickListener(v -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), -1));

        layout.setVisibility(visibilty);
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

}
