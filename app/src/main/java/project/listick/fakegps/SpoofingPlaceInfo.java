package project.listick.fakegps;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import project.listick.fakegps.Enumerations.ERouteTransport;

public class SpoofingPlaceInfo {

    public static final String CLOSED_ROUTE_MOTION_INVERT = "closed_route_motion_invert";
    public static final String ORIGIN_LAT = "org_lat";
    public static final String ORIGIN_LNG = "org_lng";

    public static final String DEST_LAT = "dst_lat";
    public static final String DEST_LNG = "dst_lng";

    public static final String ORIGIN_ADDRESS = "org_addr";
    public static final String DEST_ADDRESS = "dst_addr";

    public static final String ADDRESS = "address";

    public static final String TRANSPORT = "transport";

    public static double sourceLat;
    public static double sourceLng;
    public static double destLat;
    public static double destLng;

    public static String originAddress;
    public static String destAddress;

    public static ERouteTransport transport;

    public static double latitude;
    public static double longtiude;
    public static String address;

    private final MapView mMap;

    public SpoofingPlaceInfo(MapView mMap){
        this.mMap = mMap;
    }

    public void removeRoute(){
        for (Overlay overlay : mMap.getOverlays()) {
            if (!(overlay instanceof LocationMarker))
                mMap.getOverlays().remove(overlay);
        }
        mMap.invalidate();

        latitude = 0d;
        longtiude = 0d;
        address = null;
    }
}
