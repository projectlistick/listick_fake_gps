package project.listick.fakegps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.osmdroid.util.GeoPoint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import project.listick.fakegps.API.OpenRouteService;
import project.listick.fakegps.API.YoursRoutingApi;
import project.listick.fakegps.Enumerations.EDirectionService;
import project.listick.fakegps.Enumerations.ERouteTransport;

/*
 * Created by LittleAngry on 01.01.19 (macOS 10.12)
 * */
public class BuildRoute extends AsyncTask<Void, Void, Void> {

    private double sourceLat;
    private double sourceLong;
    private double destLat;
    private double destLong;
    public double distance;

    private ArrayList<GeoPoint> geoPoints;
    private EDirectionService directionService;
    private ERouteTransport transport;
    private WeakReference<Context> mContext;
    private WeakReference<View> mProgressDialog;
    private WeakReference<View> mView;
    private BuildRouteListener mListener;

    public interface BuildRouteListener {
        void onRouteBuilt(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport);
        void onRouteError(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport);
        void onCancel();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mView != null) {
            mProgressDialog = new WeakReference<>(mView.get().findViewById(R.id.loading));
        } else {
            Activity activity = (Activity) mContext.get();
            mProgressDialog = new WeakReference<>(activity.findViewById(R.id.loading));
        }

        View cancel = mProgressDialog.get().findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            mListener.onCancel();
            mProgressDialog.get().setVisibility(View.GONE);
            BuildRoute.this.cancel(false);
        } );

        mProgressDialog.get().setVisibility(View.VISIBLE);
    }

    public BuildRoute(double sourceLat, double sourceLong, double destLat, double destLong, EDirectionService directionService, Context context, View view, ERouteTransport transport, BuildRouteListener listener) {
        this.sourceLat = sourceLat;
        this.sourceLong = sourceLong;
        this.destLat = destLat;
        this.destLong = destLong;
        this.directionService = directionService;
        this.mContext = new WeakReference<>(context);
        this.transport = transport;
        this.mListener = listener;
        this.mView = view == null ? null : new WeakReference<>(view);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        mProgressDialog.get().setVisibility(View.GONE);

        if (geoPoints == null || geoPoints.isEmpty()) {
            geoPoints = new ArrayList<>();
            geoPoints.add(new GeoPoint(sourceLat, sourceLong));
            geoPoints.add(new GeoPoint(destLat, destLong));

            mListener.onRouteError(geoPoints, sourceLat, sourceLong, destLat, destLong, distance, transport);
        } else {
            mListener.onRouteBuilt(geoPoints, sourceLat, sourceLong, destLat, destLong, distance, transport);
        }

        MapUtil.pointsBackup = geoPoints;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        if (NetworkUtils.isNetworkAvailable())
            buildRoute(directionService);
        return null;
    }

    private void buildRoute(EDirectionService directionService){
        String content;

        if (directionService == EDirectionService.OPEN_ROUTE_SERVICE){
            OpenRouteService.Directions openRoute = new OpenRouteService.Directions(sourceLat, sourceLong, destLat, destLong, transport);
            content = openRoute.getContent();
            geoPoints = openRoute.downloadRoute(content, true);

            if (geoPoints == null) {
                buildRoute(EDirectionService.YOURS_ROUTING_API);
            }
        }
        if (directionService == EDirectionService.YOURS_ROUTING_API) {
            YoursRoutingApi yoursRoutingApi = new YoursRoutingApi(sourceLat, sourceLong, destLat, destLong);
            content = yoursRoutingApi.getContent();
            geoPoints = yoursRoutingApi.downloadRoute(content);
        }
    }

}
