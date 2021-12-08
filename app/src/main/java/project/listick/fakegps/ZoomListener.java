package project.listick.fakegps;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;

public class ZoomListener implements MapListener {
    private final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mMapZoom;
    private double mFirstZoomLevel;
    private IZoomListener mListener;

    public ZoomListener(IZoomListener listener) {
        this.mListener = listener;
    }

    @Override
    public boolean onScroll(ScrollEvent event) {

        if (mMapZoom + TIME_INTERVAL > System.currentTimeMillis())
        {
            mFirstZoomLevel = event.getSource().getZoomLevelDouble();
            return false;
        }


        double endZoomLevel = event.getSource().getZoomLevelDouble();

        if (mFirstZoomLevel != endZoomLevel) {
            mListener.onZoomChanged(mFirstZoomLevel, endZoomLevel);
        }

        mMapZoom = System.currentTimeMillis();
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }


    public interface IZoomListener {
        void onZoomChanged(double startZoomLevel, double endZoomLevel);
    }

}
