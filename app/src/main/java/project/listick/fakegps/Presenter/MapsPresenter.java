package project.listick.fakegps.Presenter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import project.listick.fakegps.AsyncGeocoder;
import project.listick.fakegps.AsyncWebClient;
import project.listick.fakegps.BuildRoute;
import project.listick.fakegps.Contract.MapsImpl;
import project.listick.fakegps.CurrentLocation;
import project.listick.fakegps.Daemons.FixedSpooferService;
import project.listick.fakegps.Daemons.ISpooferService;
import project.listick.fakegps.Daemons.JoystickService;
import project.listick.fakegps.Daemons.RouteSpooferService;
import project.listick.fakegps.Enumerations.EDirectionService;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.FakeGPSApplication;
import project.listick.fakegps.Interfaces.EditTextDialogImpl;
import project.listick.fakegps.JoystickOverlay;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.LocationMarker;
import project.listick.fakegps.LocationOperations;
import project.listick.fakegps.LocationServices;
import project.listick.fakegps.MainServiceControl;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.MapUtil;
import project.listick.fakegps.MockLocProvider;
import project.listick.fakegps.Model.BookmarksDBHelper;
import project.listick.fakegps.Model.MapsModel;
import project.listick.fakegps.MultipleRoutesInfo;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Preferences;
import project.listick.fakegps.R;
import project.listick.fakegps.RouteManager;
import project.listick.fakegps.RouteMarker.OriginAndDestMarker;
import project.listick.fakegps.SpoofingPlaceInfo;
import project.listick.fakegps.UI.BookmarksActivity;
import project.listick.fakegps.UI.EditTextDialog;
import project.listick.fakegps.UI.JoystickActivity;
import project.listick.fakegps.UI.MockLocationPermissionActivity;
import project.listick.fakegps.UI.PrettyToast;
import project.listick.fakegps.UI.RouteSettingsActivity;
import project.listick.fakegps.UI.SearchActivity;
import project.listick.fakegps.UI.SettingsActivity;

/*
 * Created by LittleAngry on 25.12.18 (macOS 10.12)
 * */
public class MapsPresenter implements MapsImpl.PresenterImpl {

    public static final String UPDATE_UI_ACTION = "project.listick.fakegps.actionservice.daemons.update_speedbar_ui";

    private final List<OriginAndDestMarker> mMarkerList;

    private final MapsImpl.ModelImpl mModel;
    private final MapView mMap;
    private final Context mContext;
    private final Activity mActivity;
    private final SpoofingPlaceInfo spoofingPlaceInfo;
    private final MapsImpl.UIImpl mUserInterface;
    private final MainServiceControl mServiceControl;
    private CurrentLocation currentLocation;

    private final BroadcastReceiver updateUIReciver;
    private final IntentFilter filter;

    private boolean isRoute = false;
    private double mDistance = -1;

    @Override
    public void onActivityLoad() {
        mModel.getPermissions();
        loadLocationMarker();
        mModel.moveCameraToLastLocation();
        initSearch();
        mUserInterface.setAddressShimmer(false);

        restoreRoute();
    }

    public MapsPresenter(MapView mMap, Context context) {
        this.mModel = new MapsModel(mMap, context);
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.mUserInterface = (MapsImpl.UIImpl) context;
        this.mServiceControl = new MainServiceControl(context);
        this.spoofingPlaceInfo = new SpoofingPlaceInfo(mMap);
        this.mMarkerList = new ArrayList<OriginAndDestMarker>();
        this.mMap = mMap;

        filter = new IntentFilter();

        filter.addAction(UPDATE_UI_ACTION);

        updateUIReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int speed = intent.getIntExtra(RouteSpooferService.UI_SPEED_KEY, 0);
                double passedDistance = intent.getDoubleExtra(RouteSpooferService.UI_PASSED_DISTANCE, 0);
                double totalDistance = intent.getDoubleExtra(RouteSpooferService.UI_TOTAL_DISTANCE, 0);
                mUserInterface.updateSpeedbar(speed, passedDistance, totalDistance);
            }
        };
        context.registerReceiver(updateUIReciver, filter);
    }

    @Override
    public void onMapDrag() {
        AsyncGeocoder geocoder = new AsyncGeocoder(mActivity);
        mUserInterface.setAddressShimmer(true);

        geocoder.getLocationAddress(mMap.getMapCenter().getLatitude(),
                mMap.getMapCenter().getLongitude(), new AsyncGeocoder.Callback() {
                    @Override
                    public void onSuccess(List<Address> locations) {
                        String locationName = locations.get(0).getAddressLine(0);

                        mUserInterface.setAddressShimmer(false);
                        mUserInterface.getAddress(locationName);
                    }

                    @Override
                    public void onError() {
                        mUserInterface.setAddressShimmer(false);
                        mUserInterface.getAddress(mContext.getString(R.string.failed_to_define_address));
                    }
                });

    }

    @Override
    public void onCurrentLocationClick() {
        try {
            if (PermissionManager.isPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Location myLocation = currentLocation.getLocation(LocationManager.PASSIVE_PROVIDER);

                if (myLocation == null) {
                    myLocation = currentLocation.getLocation();
                }

                GeoPoint geoPoint = new GeoPoint(myLocation.getLatitude(), myLocation.getLongitude());

                if (mMap.getZoomLevelDouble() < MapLoader.MIN_ZOOM_LVL)
                    mMap.getController().animateTo(geoPoint, MapLoader.OPTIMIZED_ZOOM_LVL, MapLoader.ZOOM_ANIMATION_SPEED);
                else
                    mMap.getController().animateTo(geoPoint);
            }
        } catch (Exception e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            Toast.makeText(mContext, R.string.error_get_currentloc, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setFixedMode() {
        mUserInterface.enableDone(View.GONE);
        mUserInterface.enableStop(View.VISIBLE);
        mUserInterface.toggleEditButton(View.VISIBLE);
        mUserInterface.lockSearchBar(true);
        mUserInterface.setAddMoreRoute(View.GONE);

        if (!LocationServices.isLocationEnabled(mContext))
            mUserInterface.setLocationDisabledNotification(View.VISIBLE);
    }

    @Override
    public void handleStop() {
        if (MainServiceControl.isRouteSpoofingServiceRunning(mContext)) {
            mContext.stopService(new Intent(mContext, RouteSpooferService.class));
            RouteSettingsPresenter.unbindService();
         } else if (MainServiceControl.isFixedSpoofingServiceRunning(mContext))
            mContext.stopService(new Intent(mContext, FixedSpooferService.class));

        for (Overlay overlay : mMap.getOverlays()) {
            if (!(overlay instanceof LocationMarker))
                mMap.getOverlays().remove(overlay);
        }

        mUserInterface.enableSpeedbar(View.GONE);
        mUserInterface.enableDone(View.VISIBLE);
        mUserInterface.enablePause(View.GONE);
        mUserInterface.toggleEditButton(View.GONE);
        mUserInterface.enableStop(View.GONE);
        mUserInterface.setLocationDisabledNotification(View.GONE);


        removeRoute();
    }

    @Override
    public void handleClear() {
        DialogInterface.OnClickListener cancel = (DialogInterface dialogInterface, int i) -> dialogInterface.cancel();

        DialogInterface.OnClickListener restoreLocation = (DialogInterface dialogInterface, int i) -> {
            handleStop();
            MockLocProvider.initTestProvider(); // re-init mock location provider for init LocationManager
            MockLocProvider.removeProviders();
            dialogInterface.cancel();
        };

        mUserInterface.alertDialog(mContext.getString(R.string.restore_to_real_location), mContext.getString(R.string.restore_to_real_location_confirm),
                true, mContext.getString(R.string.okay), restoreLocation, mContext.getString(R.string.cancel),
                cancel, R.drawable.ic_baseline_restore_24);
    }

    @Override
    public void handlePause() {
        boolean isPaused = mServiceControl.isPaused(RouteSettingsPresenter.sService);
        mServiceControl.setPause(RouteSettingsPresenter.sService, !isPaused);
        mUserInterface.setPauseIcon(!isPaused);
    }

    @Override
    public void onAddMoreRoute(ActivityOptionsCompat uiOptions) {

        String originAddress = mUserInterface.getWhereToAddress();
        List<GeoPoint> latestRoute = RouteManager.routes.get(RouteManager.getLatestElement()).getRoute();
        double latitude = latestRoute.get(latestRoute.size() - 1).getLatitude();
        double longitude = latestRoute.get(latestRoute.size() - 1).getLongitude();

        mMap.getController().animateTo(new GeoPoint(latitude, longitude));

        // use handler for finish animation
        new Handler().postDelayed(() -> SearchActivity.startActivity(mActivity, originAddress, latitude, longitude, true, uiOptions.toBundle()), 500);
    }

    @Override
    public void changePoint() {
        mServiceControl.sendNewCoordinates(mMap.getMapCenter().getLatitude(), mMap.getMapCenter().getLongitude());
    }

    @Override
    public void onSpoofClick(final GeoPoint geoPoint) {
        if (PermissionManager.isServiceRunning(mContext, JoystickService.class)) {

            DialogInterface.OnClickListener cancel = (DialogInterface dialogInterface, int i) -> dialogInterface.cancel();
            DialogInterface.OnClickListener stopJoystick = (DialogInterface dialogInterface, int i) -> {
                mContext.stopService(new Intent(mContext, JoystickService.class));
                mUserInterface.setJoystickMsgVisiblity(View.GONE);
                onSpoofClick(geoPoint);
                dialogInterface.cancel();
            };

            mUserInterface.alertDialog(mContext.getString(R.string.joystick_enabled), mContext.getString(R.string.joystick_enabled_error),
                    true, mContext.getString(R.string.stop), stopJoystick, mContext.getString(R.string.cancel),
                    cancel, R.drawable.joystick);

            return;
        }

        if (PermissionManager.isMockLocationsEnabled(mContext) || PermissionManager.isSystemApp(mContext)) {
            LocationOperations loc = new LocationOperations();
            loc.startSpoofing(geoPoint, mDistance, mActivity, isRoute);
        } else
            mContext.startActivity(new Intent(mContext, MockLocationPermissionActivity.class));
    }

    @Override
    public void onRoute(double sourceLat, double sourceLong, double destLat, double destLong, ERouteTransport transport) {
        BuildRoute.BuildRouteListener routeListener = new BuildRoute.BuildRouteListener() {
            @Override
            public void onRouteBuilt(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport) {

                PrettyToast.show(mActivity, mActivity.getString(R.string.route_built), R.drawable.ic_route);

                double distancePoly = MapUtil.drawPath(mMap, points);
                mDistance += distancePoly;
                MultipleRoutesInfo multipleRoutesInfo = new MultipleRoutesInfo();
                multipleRoutesInfo.setRoute(points);
                multipleRoutesInfo.setDistance(distancePoly);
                multipleRoutesInfo.setTransport(transport);

                // will be setting during RouteSettingsActivity
                multipleRoutesInfo.setSpeed(-1);
                multipleRoutesInfo.setSpeedDiff(-1);
                multipleRoutesInfo.setStartingPauseTime(-1);

                RouteManager.routes.add(multipleRoutesInfo);
                isRoute = true;

                SpoofingPlaceInfo.sourceLat = sourceLat;
                SpoofingPlaceInfo.sourceLng = sourceLong;
                SpoofingPlaceInfo.destLat = destLat;
                SpoofingPlaceInfo.destLng = destLong;
                SpoofingPlaceInfo.transport = transport;

                AsyncGeocoder geocoder = new AsyncGeocoder(mActivity);
                geocoder.getLocationAddress(sourceLat, sourceLong, new AsyncGeocoder.Callback() {
                    @Override
                    public void onSuccess(List<Address> locations) {
                        String locationName = locations.get(0).getAddressLine(0);
                        SpoofingPlaceInfo.originAddress = locationName;
                        SpoofingPlaceInfo.address = locationName;
                    }

                    @Override
                    public void onError() {
                        String failed = mContext.getString(R.string.failed_to_define_address);
                        SpoofingPlaceInfo.originAddress = failed;
                        SpoofingPlaceInfo.address = failed;
                    }
                });

                SpoofingPlaceInfo.destAddress = mUserInterface.getWhereToAddress();
                multipleRoutesInfo.setAddress(mUserInterface.getWhereToAddress());

                setRouteOriginDestMarkers(multipleRoutesInfo);
                mUserInterface.toggleRemoveRoute(View.VISIBLE);
                mUserInterface.setAddMoreRoute(View.VISIBLE);

                // open route settinger if add points
                if (RouteManager.routes.size() >= 2) {
                    RouteSettingsActivity.startActivity(mActivity, -1, -1, distance, true, true, RouteSettingsPresenter.ANOTHER_ROUTE_ADDED);
                }
            }

            @Override
            public void onRouteError(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport) {
                PrettyToast.show(mActivity, mActivity.getString(R.string.failed_to_build_route), R.drawable.ic_route);
                onRouteBuilt(points, sourceLat, sourceLong, destLat, destLong, distance, transport);
            }

            @Override
            public void onCancel() {
                if (RouteManager.routes.size() >= 2)
                    removeLatestRoute();
                else
                    removeRoute();
            }
        };

        String maps_api = Preferences.getMapsApiKey(mContext);
        EDirectionService service = EDirectionService.OPEN_ROUTE_SERVICE;

        BuildRoute buildRoute = new BuildRoute(sourceLat, sourceLong, destLat, destLong, service, mContext, null, transport, routeListener);
        buildRoute.execute();

        mMap.invalidate();

    }

    @Override
    public void onDestroy() {
        saveCurrentLocation();
        destroyLocationMarker();
    }

    @Override
    public void onMenu() {
        mModel.openMenu();
    }

    public void setRoutingMode() {
        mUserInterface.enableSpeedbar(View.VISIBLE);
        mUserInterface.enableDone(View.GONE);
        mUserInterface.enablePause(View.VISIBLE);
        mUserInterface.toggleRemoveRoute(View.GONE);
        mUserInterface.enableStop(View.VISIBLE);
        mUserInterface.setAddMoreRoute(View.GONE);
        if (!LocationServices.isLocationEnabled(mContext))
            mUserInterface.setLocationDisabledNotification(View.VISIBLE);
    }

    @Override
    public void onRouteRemove() {
        if (RouteManager.routes.size() >= 2)
            removeLatestRoute();
        else
            removeRoute();
    }

    @Override
    public void removeRoute() {

        while (RouteManager.routes.size() >= 2)
            removeLatestRoute();

        int latestRoute = RouteManager.getLatestElement();
        removeRouteMarkers(latestRoute);

        RouteManager.routes.clear();

        spoofingPlaceInfo.removeRoute();
        mUserInterface.setAddMoreRoute(View.GONE);
        mUserInterface.toggleRemoveRoute(View.GONE);
        mUserInterface.setWhereToAddress(mContext.getString(R.string.where_to));
        mUserInterface.lockSearchBar(false);
        mUserInterface.enableStop(View.GONE);
        isRoute = false;
        mDistance = -1;

        mUserInterface.setLocationDisabledNotification(View.GONE);
    }

    public void removeLatestRoute() {
        MapUtil.removeLatestRoute(mMap);

        int latestRoute = RouteManager.getLatestElement();
        mDistance -= RouteManager.routes.get(latestRoute).getDistance();
        removeRouteMarkers(latestRoute);
        RouteManager.routes.remove(latestRoute);

        latestRoute = RouteManager.getLatestElement();
        String address = RouteManager.routes.get(latestRoute).getAddress();
        mUserInterface.setWhereToAddress(address);

        // вернуть адрес на шаг назад
        // mUserInterface.setWhereToAddress(mContext.getString(R.string.where_to));
    }

    @Override
    public void onBookmarkResult(Intent data, int resultCode) {
        if (data == null) {
            return;
        }

        if (MainServiceControl.isRouteSpoofingServiceRunning(mContext))
            handleStop();

        if (resultCode == BookmarksActivity.ROUTE) {
            String destAddress = data.getStringExtra(BookmarksDBHelper.KEY_DEST_ADDRESS);

            double originLat = data.getDoubleExtra(BookmarksDBHelper.KEY_ORIGIN_LATIUTDE, 0f);
            double originLng = data.getDoubleExtra(BookmarksDBHelper.KEY_ORIGIN_LONGITUDE, 0f);
            double destLat = data.getDoubleExtra(BookmarksDBHelper.KEY_DEST_LATIUTDE, 0f);
            double destLng = data.getDoubleExtra(BookmarksDBHelper.KEY_DEST_LONGITUDE, 0f);

            ERouteTransport transport = (ERouteTransport) data.getSerializableExtra(BookmarksDBHelper.KEY_TRANSPORT);

            mUserInterface.lockSearchBar(true);
            mUserInterface.setWhereToAddress(destAddress);

            onRoute(originLat, originLng, destLat, destLng, transport);
            mMap.getController().animateTo(new GeoPoint(originLat, originLng), 17d, MapLoader.ZOOM_ANIMATION_SPEED);
        } else if (resultCode == BookmarksActivity.STATIC) {
            double latitude = data.getDoubleExtra(BookmarksDBHelper.KEY_LATITUDE, 0f);
            double longitude = data.getDoubleExtra(BookmarksDBHelper.KEY_LONGITUDE, 0f);

            mMap.getController().animateTo(new GeoPoint(latitude, longitude), 17d, MapLoader.ZOOM_ANIMATION_SPEED);
        }
    }

    @Override
    public void onItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                openAppPreferences();
                break;
            case R.id.joystick:
                joystickMode();
                break;
            case R.id.go_to_coordinates:
                goToCoordinates();
                break;
            case R.id.share_location:
                handleShareLocation();
                break;
            case R.id.restore_location:
                handleClear();
                break;
            // case R.id.add_in_bookmarks:
            //     addInBookmarks();
            //     break;
            // case R.id.bookmarks:
            //     openBookmarks();
            //     break;
            case R.id.rate_app:
                openGpForRate();
                break;
        }
    }

    private void openAppPreferences() {
        mContext.startActivity(new Intent(mContext, SettingsActivity.class));
    }

    private void openGpForRate() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=project.listick.fakegps"));
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.unknown_error, Toast.LENGTH_LONG).show();
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
        }
    }

    private void openBookmarks() {
        mActivity.startActivityForResult(new Intent(mContext, BookmarksActivity.class), BookmarksActivity.BOOKMARKS_REQUEST_CODE);
    }

    private void addInBookmarks() {
        if (!MainServiceControl.isRouteSpoofingServiceRunning(mContext)
                && !MainServiceControl.isFixedSpoofingServiceRunning(mContext)) {
            Toast.makeText(mContext, R.string.before_run_spoofing, Toast.LENGTH_LONG).show();
            return;
        }

        EditText editText = new EditText(mContext);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setMessage(R.string.bookmark_label);
        dialog.setTitle(R.string.add_in_boookmarks);
        dialog.setView(editText);

        Dialog.OnClickListener addInBookmarks = (DialogInterface dialogImpl, int which) -> {
            String bookmarkName = editText.getText().toString();

            if (bookmarkName.isEmpty()) {
                Toast.makeText(mContext, R.string.empty_field_error, Toast.LENGTH_LONG).show();
                return;
            }

            BookmarksDBHelper dbHelper = new BookmarksDBHelper(mContext, BookmarksDBHelper.BOOKMARKS_DB, BookmarksDBHelper.DATABASE_VERSION);
            try {
                dbHelper.saveBookmark(RouteSettingsPresenter.sService.getRoutes(), bookmarkName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        };

        dialog.setPositiveButton(R.string.okay, addInBookmarks);
        dialog.show();
    }

    private void handleShareLocation() {
        Location location = currentLocation.getLocation();

        if (location == null) {
            Toast.makeText(mContext, R.string.could_not_get_location, Toast.LENGTH_LONG).show();
            return;
        }

        String coordinates = location.getLatitude() + ", " + location.getLongitude();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, coordinates);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        mContext.startActivity(shareIntent);
    }

    private void goToCoordinates() {
        EditTextDialog dialog = new EditTextDialog(mContext, new EditTextDialogImpl() {

            @Override
            public void getDialogResult(String value) {
                if (!MapUtil.isCoordinates(value)) {
                    Toast.makeText(FakeGPSApplication.getAppContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                MapUtil.goToCoordinates(mMap, value);
            }
        });

        dialog.show(mContext.getString(R.string.go_to_coordinates), mContext.getString(R.string.enter_coodinates));

    }

    private void joystickMode() {
        Dialog.OnClickListener grant = (DialogInterface dialogInterface, int i) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));
            mActivity.startActivityForResult(intent, JoystickOverlay.ACTION_OVERLAYS_PERMISSION);
        };
        Dialog.OnClickListener cancel = (DialogInterface dialogInterface, int i) -> dialogInterface.dismiss();

        if (!PermissionManager.canDrawOverlays(mContext)) {
            mUserInterface.alertDialog(mContext.getString(R.string.draw_overlays_permission), mContext.getString(R.string.draw_overlays_summary),
                    true, mContext.getString(R.string.grant), grant, mContext.getString(R.string.cancel), cancel, R.drawable.ic_outline_layers_48);
            return;
        }

        startJoystickActivity();
    }

    public void startJoystickActivity() {
        if (!MainServiceControl.isRouteSpoofingServiceRunning(mContext) && !MainServiceControl.isFixedSpoofingServiceRunning(mContext))
            mContext.startActivity(new Intent(mContext, JoystickActivity.class)
                    .putExtra(ListickApp.LATITUDE, mMap.getMapCenter().getLatitude())
                    .putExtra(ListickApp.LONGITUDE, mMap.getMapCenter().getLongitude()));
        else {
            DialogInterface.OnClickListener click = (DialogInterface dialogInterface, int i) -> dialogInterface.cancel();
            DialogInterface.OnClickListener stopSpoofing = (DialogInterface dialogInterface, int i) -> {
                handleStop();
                startJoystickActivity();
            };

            mUserInterface.alertDialog(mContext.getString(R.string.warning),
                    mContext.getString(R.string.joystick_stop_mocking), true, mContext.getString(R.string.stop),
                    stopSpoofing, mContext.getString(R.string.cancel), click, R.drawable.ic_round_warning_48);
        }
    }

    private void restoreRoute() {

        if (MainServiceControl.isFixedSpoofingServiceRunning(mContext)) {
            setFixedMode();
        }

        ISpooferService service = RouteSettingsPresenter.sService;
        if (!MainServiceControl.isRouteSpoofingServiceRunning(mContext) || service == null)
            return;

        try {
            List<MultipleRoutesInfo> allRoutes = service.getRoutes();
            for (MultipleRoutesInfo route : allRoutes) {
                setRouteOriginDestMarkers(route);
                MapUtil.drawPath(mMap, route.getRoute());
            }

            setRoutingMode();
            mUserInterface.lockSearchBar(true);
            mUserInterface.setPauseIcon(mServiceControl.isPaused(service));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initSearch() {
        mUserInterface.getAddress(mContext.getString(R.string.failed_to_define_address));
    }


    private void setRouteOriginDestMarkers(MultipleRoutesInfo route) {
        OriginAndDestMarker markers = route.getRouteMarker(mContext);
        mMarkerList.add(markers);
        mMap.getOverlays().add(markers.origin);
        mMap.getOverlays().add(markers.dest);
        mMap.invalidate();
    }

    private void removeRouteMarkers(int routePosition) {
        if (!MainServiceControl.isRouteSpoofingServiceRunning(mContext))
            return;
        OriginAndDestMarker markers = mMarkerList.get(routePosition);
        mMarkerList.remove(markers);
        mMap.getOverlays().remove(markers.origin);
        mMap.getOverlays().remove(markers.dest);
        mMap.invalidate();
    }

    @Override
    public void onPause() {
        removeLocationUpdates();
        mMap.onPause();
        mContext.unregisterReceiver(updateUIReciver);
    }

    @Override
    public void onResume() {
        registerLocationUpdates();
        mMap.onResume();
        mContext.registerReceiver(updateUIReciver, filter);

        if (PermissionManager.isServiceRunning(mContext, JoystickService.class))
            mUserInterface.setJoystickMsgVisiblity(View.VISIBLE);
        else
            mUserInterface.setJoystickMsgVisiblity(View.GONE);

        if (LocationServices.isLocationEnabled(mContext))
            mUserInterface.setLocationDisabledNotification(View.GONE);
        else if (!LocationServices.isLocationEnabled(mContext) && (MainServiceControl.isFixedSpoofingServiceRunning(mContext) || MainServiceControl.isRouteSpoofingServiceRunning(mContext)))
            mUserInterface.setLocationDisabledNotification(View.VISIBLE);

    }

    public void loadLocationMarker() {
        currentLocation = new CurrentLocation(mContext, mMap);
    }

    public void destroyLocationMarker() {
        currentLocation.onDestroy();
    }

    public void registerLocationUpdates() {
        currentLocation.registerUpdates();
    }

    public void removeLocationUpdates() {
        currentLocation.removeUpdates();
    }

    public void saveCurrentLocation() {
        if (PermissionManager.isPermissionGranted(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) || PermissionManager.isPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Location androidLocations = currentLocation.getLocation(LocationManager.PASSIVE_PROVIDER);
            Location location = (androidLocations != null) ? androidLocations : currentLocation.getLocation();
            if (location != null)
                mContext.getSharedPreferences(ListickApp.LOCATION_PREFERENCES, Context.MODE_PRIVATE).edit()
                        .putFloat(ListickApp.LATITUDE, (float) location.getLatitude())
                        .putFloat(ListickApp.LONGITUDE, (float) location.getLongitude())
                        .putFloat(ListickApp.ZOOM, (float) mMap.getZoomLevelDouble())
                        .apply();
        }
    }


}
