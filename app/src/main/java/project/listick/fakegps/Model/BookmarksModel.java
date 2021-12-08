package project.listick.fakegps.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import okhttp3.Route;
import project.listick.fakegps.Contract.BookmarksImpl;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.RouteCoordinateMgr;

import static project.listick.fakegps.Model.BookmarksDBHelper.BOOKMARKS_DB;
import static project.listick.fakegps.Model.BookmarksDBHelper.DATABASE_VERSION;
import static project.listick.fakegps.Model.BookmarksDBHelper.ROUTES_TABLE;
import static project.listick.fakegps.Model.BookmarksDBHelper.STATIC_TABLE;

public class BookmarksModel implements BookmarksImpl.Model {

    private BookmarksDBHelper mDbHelper;
    private SQLiteDatabase mDb;

    public BookmarksModel(Context context) {
        this.mDbHelper = new BookmarksDBHelper(context, BOOKMARKS_DB, DATABASE_VERSION);
        this.mDb = mDbHelper.getReadableDatabase();
    }


    @Override
    public ArrayList<RouteCoordinateMgr.PlaceAddress> getRouteAddress() {
        Cursor addressCursor = mDb.query(BookmarksDBHelper.ROUTES_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() == 0)
            return null;

        int originAddressIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_ADDRESS);
        int destAddressIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_DEST_ADDRESS);

        ArrayList<RouteCoordinateMgr.PlaceAddress> addressList = new ArrayList<>();
        addressCursor.moveToFirst();
        do {
            String originAddress = addressCursor.getString(originAddressIndex);
            String destAddress = addressCursor.getString(destAddressIndex);
            addressList.add(new RouteCoordinateMgr.PlaceAddress(originAddress, destAddress));
        } while (addressCursor.moveToNext());

        addressCursor.close();
        return addressList;
    }

    @Override
    public ArrayList<ERouteTransport> getRouteTransport() {
        Cursor transportCursor = mDb.query(BookmarksDBHelper.ROUTES_TABLE, null, null, null, null, null, null);
        if (transportCursor == null || transportCursor.getCount() == 0)
            return null;

        int transportIndex = transportCursor.getColumnIndex(BookmarksDBHelper.KEY_ADDRESS);

        ArrayList<ERouteTransport> transportList = new ArrayList<>();
        transportCursor.moveToFirst();
        do {
            int transport = transportCursor.getInt(transportIndex);

            if (transport == BookmarksDBHelper.TRANSPORT_CAR)
                transportList.add(ERouteTransport.ROUTE_CAR);
            if (transport == BookmarksDBHelper.TRANSPORT_BIKE)
                transportList.add(ERouteTransport.ROUTE_BIKE);
            if (transport == BookmarksDBHelper.TRANSPORT_WALK)
                transportList.add(ERouteTransport.ROUTE_WALK);
        } while (transportCursor.moveToNext());

        transportCursor.close();
        return transportList;
    }

    @Override
    public ArrayList<Integer> getRouteRowIds() {
        ArrayList<Integer> rowIdList = new ArrayList<>();
        Cursor addressCursor = mDb.query(BookmarksDBHelper.ROUTES_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() == 0)
            return rowIdList;

        int rowIdIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_ROWID);

        addressCursor.moveToFirst();
        do {
            int rowId = Integer.parseInt(addressCursor.getString(rowIdIndex));
            rowIdList.add(rowId);
        } while (addressCursor.moveToNext());
        addressCursor.close();

        return rowIdList;
    }

    @Override
    public ArrayList<Integer> getStaticRowIds() {
        ArrayList<Integer> rowIdList = new ArrayList<>();
        Cursor addressCursor = mDb.query(STATIC_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() == 0)
            return rowIdList;

        int rowIdIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_ROWID);

        addressCursor.moveToFirst();
        do {
            int rowId = Integer.parseInt(addressCursor.getString(rowIdIndex));

            rowIdList.add(rowId);
        } while (addressCursor.moveToNext());
        addressCursor.close();

        return rowIdList;
    }

    @Override
    public ArrayList<RouteCoordinateMgr> getRouteCoordinates() {
        ArrayList<RouteCoordinateMgr> coordinatesList = new ArrayList<>();

        ArrayList<GeoPoint> origins = new ArrayList<>();
        ArrayList<GeoPoint> destinations = new ArrayList<>();

        Cursor addressCursor = mDb.query(BookmarksDBHelper.ROUTES_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() <= 0)
            return null;

        int coordinatesIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_COORDINATES);

        addressCursor.moveToFirst();
        do {
            String coordinatesString = addressCursor.getString(coordinatesIndex);
            JSONArray coordinates = null;
            try {
                coordinates = new JSONArray(coordinatesString);


                int cLength = coordinates.length();

                for (int i = 0; i < cLength; i++) {
                    String cShared = coordinates.getString(i);

                    String[] cArr = cShared.split("&");
                    String cOrigin = cArr[0];

                    String[] cOriginLatLng = cOrigin.split(",");

                    double lat = Double.parseDouble(cOriginLatLng[0]);
                    double lng = Double.parseDouble(cOriginLatLng[1]);
                    GeoPoint origin = new GeoPoint(lat, lng);

                    String cDest = cArr[1];
                    String[] cDestLatLng = cDest.split(",");

                    double destLat = Double.parseDouble(cDestLatLng[0]);
                    double destLng = Double.parseDouble(cDestLatLng[1]);
                    GeoPoint destination = new GeoPoint(destLat, destLng);

                    origins.add(origin);
                    destinations.add(destination);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } while (addressCursor.moveToNext());
        addressCursor.close();

        coordinatesList.add(new RouteCoordinateMgr(origins, destinations));

        return coordinatesList;
    }

    @Override
    public ArrayList<GeoPoint> getStaticCoordinates() {
        Cursor addressCursor = mDb.query(BookmarksDBHelper.STATIC_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() == 0)
            return null;

        int latIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_LATITUDE);
        int lngIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_LONGITUDE);

        ArrayList<GeoPoint> points = new ArrayList<>();
        addressCursor.moveToFirst();
        do {
            double lat = addressCursor.getDouble(latIndex);
            double lng = addressCursor.getDouble(lngIndex);
            GeoPoint place = new GeoPoint(lat, lng);

            points.add(place);
        } while (addressCursor.moveToNext());
        addressCursor.close();

        return points;
    }

    @Override
    public ArrayList<String> getStaticAddress() {
        Cursor addressCursor = mDb.query(BookmarksDBHelper.STATIC_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() == 0)
            return null;

        int addressIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_ADDRESS);

        ArrayList<String> addressList = new ArrayList<>();
        addressCursor.moveToFirst();
        do {
            String address = addressCursor.getString(addressIndex);
            addressList.add(address);
        } while (addressCursor.moveToNext());

        addressCursor.close();

        return addressList;
    }

    @Override
    public ArrayList<String> getStaticNames() {
        Cursor addressCursor = mDb.query(BookmarksDBHelper.STATIC_TABLE, null, null, null, null, null, null);

        if (addressCursor == null || addressCursor.getCount() == 0)
            return null;

        int labelIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_LABEL);

        ArrayList<String> names = new ArrayList<>();
        addressCursor.moveToFirst();
        do {
            String label = addressCursor.getString(labelIndex);
            names.add(label);
        } while (addressCursor.moveToNext());

        addressCursor.close();

        return names;
    }

    @Override
    public ArrayList<String> getRouteNames() {
        Cursor addressCursor = mDb.query(BookmarksDBHelper.ROUTES_TABLE, null, null, null, null, null, null);
        if (addressCursor == null || addressCursor.getCount() == 0)
            return null;

        int labelIndex = addressCursor.getColumnIndex(BookmarksDBHelper.KEY_LABEL);

        ArrayList<String> names = new ArrayList<>();
        addressCursor.moveToFirst();
        do {
            String label = addressCursor.getString(labelIndex);
            names.add(label);
        } while (addressCursor.moveToNext());

        addressCursor.close();

        return names;
    }


    @Override
    public void removeRouteBookmark(long rowId) {
        mDb.delete(ROUTES_TABLE, BookmarksDBHelper.KEY_ROWID + "=" + rowId, null);
    }

    @Override
    public void removeStaticBookmark(long rowId) {
        mDb.delete(STATIC_TABLE, BookmarksDBHelper.KEY_ROWID + "=" + rowId, null);
    }

}
