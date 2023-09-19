package project.listick.fakegps;

import static project.listick.fakegps.API.LFGSimpleApi.CODE_BAD_RECAPTCHA_RESPONSE;
import static project.listick.fakegps.API.LFGSimpleApi.CODE_CONNECTION_FAILED;
import static project.listick.fakegps.API.LFGSimpleApi.CODE_RECAPTCHA_RESPONSE;
import static project.listick.fakegps.API.LFGSimpleApi.CODE_SUCCESS;
import static project.listick.fakegps.API.LFGSimpleApi.CODE_UNKNOWN_ERROR;

import android.app.Activity;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import project.listick.fakegps.API.LFGSimpleApi;
import project.listick.fakegps.Enumerations.ERouteTransport;

public class RouteBuilder {

    public static final String TAG = "RouteBuilder";

    private final Activity activity;

    private final double originLat;
    private final double originLng;
    private final double destLat;
    private final double destLng;

    private final ERouteTransport transport;

    private final String captchaResult;
    private boolean canceled;

    public interface IRouteBuilder {
        void prepare();
        void onRouteBuilt(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport);
        void onRouteError(ArrayList<GeoPoint> points, double sourceLat, double sourceLong, double destLat, double destLong, double distance, ERouteTransport transport);
        void captchaResponse();
    }

    public RouteBuilder(Activity activity, double originLat, double originLng, double destLat, double destLng, ERouteTransport transport, String captchaResult) {
        this.activity = activity;
        this.captchaResult = captchaResult;

        this.originLat = originLat;
        this.originLng = originLng;
        this.destLat = destLat;
        this.destLng = destLng;

        this.transport = transport;
        this.canceled = false;
    }

    public void build(IRouteBuilder listener) {
        listener.prepare();

        LFGSimpleApi.Directions directionsApi = new LFGSimpleApi.Directions(originLat, originLng, destLat, destLng, transport, captchaResult);

        directionsApi.downloadRoute(response -> {
            if (canceled)
                return;

            if (response.code == CODE_SUCCESS) {
                response.distance = 0;

                activity.runOnUiThread(() -> listener.onRouteBuilt(response.result, originLat, originLng, destLat, destLng, response.distance, transport));
            } else if (response.code == CODE_CONNECTION_FAILED || response.code == CODE_UNKNOWN_ERROR) {

                response.result = new ArrayList<>();
                response.result.add(new GeoPoint(originLat, originLng));
                response.result.add(new GeoPoint(destLat, destLng));
                response.distance = 0;

                activity.runOnUiThread(() -> listener.onRouteError(response.result, originLat, originLng, destLat, destLng, response.distance, transport));
            } else if (response.code == CODE_RECAPTCHA_RESPONSE || response.code == CODE_BAD_RECAPTCHA_RESPONSE) {
                activity.runOnUiThread(listener::captchaResponse);
            }
        });


    }

    public void cancel() {
        this.canceled = true;
    }

}
