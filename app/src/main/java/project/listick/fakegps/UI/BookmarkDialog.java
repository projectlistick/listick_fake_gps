package project.listick.fakegps.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

import project.listick.fakegps.BuildRoute;
import project.listick.fakegps.Enumerations.EDirectionService;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.LocationMarker;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.MapUtil;
import project.listick.fakegps.Model.BookmarksDBHelper;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.R;
import project.listick.fakegps.RouteMarker;

import static android.view.View.GONE;

/*
 * Created by LittleAngry on 02.01.19 (macOS 10.12)
 * */
public class BookmarkDialog {

    private Context context;
    private String mAddress;
    private String mOriginAddress;
    private String mDestAddress;

    private ERouteTransport mTransport;

    private ArrayList<GeoPoint> mOriginList;
    private ArrayList<GeoPoint> mDestList;

    private double mLatitude;
    private double mLongitude;

    private int mPosition;
    private BookmarkInterface mListener;

    public interface BookmarkInterface {
        void onBookmarkRemoved(int position, boolean isRoute);
    }

    public BookmarkDialog(Context context, BookmarkInterface listener){
        this.context = context;
        this.mListener = listener;
    }

    public void show(String name, final boolean isRoute) {
        final Dialog dialog = new Dialog(context);
        final Activity activity = (Activity) context;

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.bookmark_dialog);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawableResource(android.R.color.transparent);
        MapView mapView = dialog.findViewById(R.id.map);
        MapLoader mapLoader = new MapLoader(dialog.getContext());
        mapLoader.load(mapView, dialog.findViewById(R.id.copyright_txt));

        TextView nameField = dialog.findViewById(R.id.name);
        TextView whereToField = dialog.findViewById(R.id.where_to);
        TextView whereFromField = dialog.findViewById(R.id.where_from);

        RelativeLayout remove = dialog.findViewById(R.id.remove);
        Button okButton = dialog.findViewById(R.id.btn_action);
        Button cancelButton = dialog.findViewById(R.id.cancel_action);

        nameField.setText(name);

        if (isRoute) {
            whereFromField.append(": " + mOriginAddress);
            whereToField.append(" " + mDestAddress);
            GeoPoint origin = mOriginList.get(0);
            mapView.getController().animateTo(new GeoPoint(origin.getLatitude(), origin.getLongitude()), 15d, 500L);

            BuildRoute.BuildRouteListener routeListener = new BuildRoute.BuildRouteListener() {
                @Override
                public void onRouteBuilt(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport) {
                    MapUtil.drawPath(mapView, points);

                    RouteMarker sourceMarker = new RouteMarker(context, RouteMarker.Type.SOURCE);
                    sourceMarker.setPosition(sourceLat, sourceLong);
                    RouteMarker destMarker = new RouteMarker(context, RouteMarker.Type.DEST);
                    destMarker.setPosition(destLat, destLong);


                    mapView.getOverlays().add(sourceMarker);
                    mapView.getOverlays().add(destMarker);
                }

                @Override
                public void onRouteError(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport) {
                    Toast.makeText(context, R.string.failed_to_build_route, Toast.LENGTH_LONG).show();
                    onRouteBuilt(points, sourceLat, sourceLong, destLat, destLong, distance, transport);
                }

                @Override
                public void onCancel() {

                }
            };


            //route = new BuildRoute(mOriginLat, mOriginLng, mDestLat, mDestLng, EDirectionService.OPEN_ROUTE_SERVICE, dialog.getContext(), dialog.getWindow().getDecorView(), ERouteTransport.ROUTE_CAR, routeListener);
            //route.execute();
        } else {
            RouteMarker marker = new RouteMarker(activity, RouteMarker.Type.DEST);
            marker.setPosition(mLatitude, mLongitude);

            whereToField.setVisibility(GONE);
            whereFromField.setText(R.string.address);
            whereFromField.append(" " + mAddress);
            mapView.getController().animateTo(new GeoPoint(mLatitude, mLongitude), 17d, 500L);
            mapView.getOverlayManager().add(marker);

        }

        cancelButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                dialog.cancel();
            }
        });

        okButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent result = new Intent(context, MapsActivity.class);
                if (isRoute) {
                    result.putExtra(BookmarksDBHelper.KEY_ADDRESS, mOriginAddress);
                    result.putExtra(BookmarksDBHelper.KEY_DEST_ADDRESS, mDestAddress);
                    // fixme
                    // result.putExtra(BookmarksDBHelper.KEY_ORIGIN_LATIUTDE, mOriginLat);
                    // result.putExtra(BookmarksDBHelper.KEY_ORIGIN_LONGITUDE, mOriginLng);
                    // result.putExtra(BookmarksDBHelper.KEY_DEST_LATIUTDE, mDestLat);
                    // result.putExtra(BookmarksDBHelper.KEY_DEST_LONGITUDE, mDestLng);
                    result.putExtra(BookmarksDBHelper.KEY_TRANSPORT, mTransport);
                    activity.setResult(BookmarksActivity.ROUTE, result);
                } else {
                    result.putExtra(BookmarksDBHelper.KEY_ADDRESS, mAddress);
                    result.putExtra(BookmarksDBHelper.KEY_LATITUDE, mLatitude);
                    result.putExtra(BookmarksDBHelper.KEY_LONGITUDE, mLongitude);
                    activity.setResult(BookmarksActivity.STATIC, result);
                }
                activity.finish();

                dialog.cancel();
            }
        });

        remove.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                mListener.onBookmarkRemoved(mPosition, isRoute);
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public void setDestAddress(String mDestAddress) {
        this.mDestAddress = mDestAddress;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public void setOriginAddress(String mOriginAddress) {
        this.mOriginAddress = mOriginAddress;
    }

    public void setOriginsList(ArrayList<GeoPoint> origins) {
        this.mOriginList = origins;
    }

    public void setDestinationsList(ArrayList<GeoPoint> destinationsList) {
        this.mDestList = destinationsList;
    }

    public void setTransport(ERouteTransport mTransport) {
        this.mTransport = mTransport;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

}

