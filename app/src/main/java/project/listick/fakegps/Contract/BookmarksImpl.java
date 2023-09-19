package project.listick.fakegps.Contract;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.RouteCoordinateMgr;

public interface BookmarksImpl {

    interface UI {
        void showRouteBookmarks(ArrayList<RouteCoordinateMgr> coordinates, ArrayList<RouteCoordinateMgr.PlaceAddress> addressList, ArrayList<String> routeNames);
        void showStaticBookmarks(ArrayList<GeoPoint> coordinates, ArrayList<String> addressList, ArrayList<String> names);
        void showBlankFragment();
        void setCurrentTab(int tab);
    }

    interface Presenter {
        void onActivityLoad();
        void onStaticSpoofList();
        void onRouteSpoofList();
        void onItemSelected(int position);
        void setCurrentTab(int tab);
    }

    interface Model {
        ArrayList<RouteCoordinateMgr.PlaceAddress> getRouteAddress();
        ArrayList<RouteCoordinateMgr> getRouteCoordinates();
        ArrayList<GeoPoint> getStaticCoordinates();
        ArrayList<String> getStaticAddress();
        ArrayList<String> getStaticNames();
        ArrayList<String> getRouteNames();
        ArrayList<ERouteTransport> getRouteTransport();

        ArrayList<Integer> getRouteRowIds();
        ArrayList<Integer> getStaticRowIds();
        void removeRouteBookmark(long rowId);
        void removeStaticBookmark(long rowId);
    }

}
