package project.listick.fakegps.Presenter;

import android.content.Context;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import project.listick.fakegps.Contract.BookmarksImpl;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.Model.BookmarksModel;
import project.listick.fakegps.RouteCoordinateMgr;
import project.listick.fakegps.UI.BookmarkDialog;
import project.listick.fakegps.UI.BookmarksActivity;

import static project.listick.fakegps.UI.BookmarksActivity.ROUTE;
import static project.listick.fakegps.UI.BookmarksActivity.STATIC;

public class BookmarksPresenter implements BookmarksImpl.Presenter {

    private Context mContext;
    private BookmarksImpl.UI mUserInterface;
    private BookmarksModel mModel;
    private int mTab = ROUTE;

    private ArrayList<RouteCoordinateMgr> mRouteCoordinates = null;
    private ArrayList<RouteCoordinateMgr.PlaceAddress> mRouteAddresses = null;
    private ArrayList<String> mRouteNames = null;
    private ArrayList<ERouteTransport> mRouteTransport = null;

    private ArrayList<String> mStaticNames = null;
    private ArrayList<GeoPoint> mStaticCoordinates = null;
    private ArrayList<String> mStaticAddresses = null;


    public BookmarksPresenter(Context context, BookmarksImpl.UI listener) {
        this.mContext = context;
        this.mUserInterface = listener;
        this.mModel = new BookmarksModel(context);
    }

    @Override
    public void onActivityLoad() {
        onRouteSpoofList();
    }

    @Override
    public void onStaticSpoofList() {
        mStaticAddresses = mModel.getStaticAddress();
        mStaticCoordinates = mModel.getStaticCoordinates();
        mStaticNames = mModel.getStaticNames();

        mUserInterface.setCurrentTab(STATIC);

        if (mStaticAddresses == null || mStaticCoordinates == null || mStaticNames == null) {
            mUserInterface.showBlankFragment();
            return;
        }

        mUserInterface.showStaticBookmarks(mStaticCoordinates, mStaticAddresses, mStaticNames);
    }

    @Override
    public void onRouteSpoofList() {

        mRouteCoordinates = mModel.getRouteCoordinates();
        mRouteAddresses = mModel.getRouteAddress();
        mRouteNames = mModel.getRouteNames();
        mRouteTransport = mModel.getRouteTransport(); // DB: 1

        mUserInterface.setCurrentTab(ROUTE);
        if (mRouteCoordinates == null || mRouteAddresses == null || mRouteNames == null) {
            mUserInterface.showBlankFragment();
            return;
        }

        mUserInterface.showRouteBookmarks(mRouteCoordinates, mRouteAddresses, mRouteNames);

    }

    @Override
    public void onItemSelected(int position) {
        if (position > mModel.getRouteRowIds().size() && mTab == ROUTE)
            return;
        else if (position > mModel.getStaticRowIds().size() && mTab == STATIC)
            return;

        BookmarkDialog dialog = new BookmarkDialog(mContext, (position1, isRoute) -> {
            if (isRoute) {
                mModel.removeRouteBookmark(mModel.getRouteRowIds().get(position1));
                onRouteSpoofList(); // re-init listview for getting "emove from listview" effect
            } else {
                mModel.removeStaticBookmark(mModel.getStaticRowIds().get(position1));
                onStaticSpoofList();
            }
        });

        dialog.setPosition(position);
        if (mTab == ROUTE) {
            dialog.setOriginAddress(mRouteAddresses.get(position).getOrigin());
            dialog.setDestAddress(mRouteAddresses.get(position).getDest());


            // fixme
            // dialog.setOriginLat(mRouteCoordinates.get(position).get);
            // dialog.setOriginLng(mRouteCoordinates.get(position).getOrigin().getLongitude());

            // dialog.setDestLat(mRouteCoordinates.get(position).getDestination().getLatitude());
            // dialog.setDestLng(mRouteCoordinates.get(position).getDestination().getLongitude());
            dialog.setTransport(mRouteTransport.get(position));

            dialog.show(mRouteNames.get(position), true);
        } else {
            dialog.setLatitude(mStaticCoordinates.get(position).getLatitude());
            dialog.setLongitude(mStaticCoordinates.get(position).getLongitude());
            dialog.setAddress(mStaticAddresses.get(position));
            dialog.show(mStaticNames.get(position), false);
        }

    }

    @Override
    public void setCurrentTab(int tab) {
        this.mTab = tab;
    }
}
