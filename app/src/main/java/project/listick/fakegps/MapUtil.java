package project.listick.fakegps;

import android.graphics.Paint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by LittleAngry on 03.01.19 (macOS 10.12)
 * */
public class MapUtil {
    public static ArrayList<GeoPoint> pointsBackup;
    private static ArrayList<GeoPoint> resultList;

    private static final List<Polyline> sPolylines = new ArrayList<Polyline>();

    // Returns path distance
    public static double drawPath(MapView map, List<GeoPoint> points) {
        int color = FakeGPSApplication.getAppContext().getColor(R.color.uicolor);

        int width = (int) ImageUtils.convertDpToPixel(4);
        Polyline polyline = new Polyline();
        sPolylines.add(polyline);
        polyline.setPoints(points);
        polyline.getOutlinePaint().setColor(color);
        polyline.getOutlinePaint().setStrokeWidth(width);
        polyline.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
        map.getOverlays().add(polyline);

        map.invalidate();

        return polyline.getDistance();
    }


    public static boolean isCoordinates(String str) {
        return str.matches("^([-+]?)([\\d]{1,2})(((\\.)(\\d+)(,)))(\\s*)(([-+]?)([\\d]{1,3})((\\.)(\\d+))?)$");
    }

    public static void goToCoordinates(MapView map, String coordinates) {
        coordinates = coordinates.trim();
        String[] latLng;

        latLng = coordinates.split(",");
        double latitude = Double.parseDouble(latLng[0].trim());
        double longitude = Double.parseDouble(latLng[1].trim());

        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        map.getController().animateTo(geoPoint, 17d, MapLoader.ZOOM_ANIMATION_SPEED);
    }


    public static void removeLatestRoute(MapView map) {
        map.getOverlays().remove(sPolylines.get(sPolylines.size() - 1));

        map.invalidate();
    }

    public static ArrayList<GeoPoint> optimizePoly(double zoomLevel) {
        resultList = new ArrayList<>();
        douglasPeucker(pointsBackup, 0, pointsBackup.size(), zoomLevel);
        return resultList;
    }

    private static void douglasPeucker(List<GeoPoint> points, int start, int end, double zoomLevel) {
        double epsilon = 1 / (zoomLevel * 2);

        final int latestIndex = end - 1;

        int index = 0;
        double dmax = 0;

        for (int i = start + 1; i < latestIndex; i++) {

            final double px = points.get(i).getLatitude();
            final double py = points.get(i).getLongitude();

            final double vx = points.get(start).getLatitude();
            final double vy = points.get(start).getLongitude();

            final double wx = points.get(latestIndex).getLatitude();
            final double wy = points.get(latestIndex).getLongitude();

            double d = Geometry.perpendicularDistance(px, py, vx, vy, wx, wy);

            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }

        if (dmax > epsilon) {
            douglasPeucker(points, start, index, zoomLevel);
            douglasPeucker(points, index, end, zoomLevel);
        } else {
            if ((latestIndex - start) > 0) {
                resultList.add(points.get(start));
                resultList.add(points.get(latestIndex));
            } else {
                resultList.add(points.get(start));
            }
        }
    }

}
