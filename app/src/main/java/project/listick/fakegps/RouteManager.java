package project.listick.fakegps;


import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by LittleAngry on 04.01.19 (macOS 10.12)
 * */
public class RouteManager {

    public static List<MultipleRoutesInfo> routes = new ArrayList<>();

    public static int getLatestElement() {
        return RouteManager.routes.size() - 1;
    }

    public static void startMotion(List<GeoPoint> points, ArrayList<GeoPoint> buffer) {
        for (int i = 0; i <= points.size() - 1; i++) {
            if ((i + 1) != points.size()) {
                float[] results = new float[1];
                Location.distanceBetween(points.get(i).getLatitude(), points.get(i).getLongitude(), points.get(i + 1).getLatitude(), points.get(i + 1).getLongitude(), results);
                int p = (int) results[0];
                segmentPoints(points.get(i), points.get(i + 1), p, buffer);
            }
        }
    }

    private static void segmentPoints(GeoPoint paramLatLng1, GeoPoint paramLatLng2, int paramInt, ArrayList<GeoPoint> buffer) {
        for (int i = paramInt; i >= 0; i--) {
            double d1 = paramLatLng1.getLatitude();
            double d2 = paramInt - i;
            double d3 = paramLatLng2.getLatitude();
            double d4 = paramLatLng1.getLatitude();
            double elevation = paramLatLng1.getAltitude();
            GeoPoint geo = new GeoPoint(d1 + (d3 - d4) * d2 / paramInt, paramLatLng1.getLongitude() + d2 * (paramLatLng2.getLongitude() - paramLatLng1.getLongitude()) / paramInt, elevation);
            buffer.add(geo);
        }
    }
}
