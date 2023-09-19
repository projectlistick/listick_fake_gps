package project.listick.fakegps;

import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

public class MyLocationPoint {

    public static final String TAG = MyLocationPoint.class.getName();

    private final LocationMarker mMarker;
    private final Polygon mAccuracyPolygon;

    public MyLocationPoint() {
        this.mMarker = new LocationMarker();
        this.mAccuracyPolygon = new Polygon();

        mAccuracyPolygon.getFillPaint().setColor(FakeGPSApplication.getAppContext().getColor(R.color.alpha_blue));
        mAccuracyPolygon.getOutlinePaint().setColor(FakeGPSApplication.getAppContext().getColor(R.color.alpha_blue));
        mAccuracyPolygon.getOutlinePaint().setStrokeWidth(0);
        mAccuracyPolygon.setInfoWindow(null);
    }

    public void updateMyLocationPoint(MapView mapView, GeoPoint myLocationPoint, float accuracy, float bearing) {
        mapView.getOverlayManager().remove(mMarker);
        mapView.getOverlayManager().remove(mAccuracyPolygon);

        mMarker.setBearing(bearing);
        mMarker.setPosition(myLocationPoint);
        try {
            mAccuracyPolygon.setPoints(Polygon.pointsAsCircle(myLocationPoint, accuracy));
            mapView.getOverlayManager().add(mAccuracyPolygon);
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, null, e);
        }

        mapView.getOverlayManager().add(mMarker);

        mapView.invalidate();
    }

}
