package project.listick.fakegps;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class RouteCoordinateMgr {

    private ArrayList<GeoPoint> mOrigins;
    private ArrayList<GeoPoint> mDestinations;

    public RouteCoordinateMgr(ArrayList<GeoPoint> origin, ArrayList<GeoPoint> destination) {
        this.mOrigins = origin;
        this.mDestinations = destination;
    }

    public ArrayList<GeoPoint> getDestination() {
        return mDestinations;
    }

    public ArrayList<GeoPoint> getOrigin() {
        return mOrigins;
    }

    public static class PlaceAddress {
        String mOrigin;
        String mDest;

        public PlaceAddress(String origin, String dest) {
            this.mOrigin = origin;
            this.mDest = dest;
        }

        public String getDest() {
            return mDest;
        }

        public String getOrigin() {
            return mOrigin;
        }
    }

}
