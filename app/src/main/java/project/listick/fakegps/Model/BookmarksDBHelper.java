package project.listick.fakegps.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.Base64;
import java.util.List;

import project.listick.fakegps.Daemons.ISpooferService;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.MainServiceControl;
import project.listick.fakegps.MultipleRoutesInfo;
import project.listick.fakegps.RouteManager;
import project.listick.fakegps.SpoofingPlaceInfo;

public class BookmarksDBHelper extends SQLiteOpenHelper {

    public static final int TRANSPORT_CAR = 0;
    public static final int TRANSPORT_BIKE = 1;
    public static final int TRANSPORT_WALK = 2;

    public static final String BOOKMARKS_DB = "bookmarks";
    public static final int DATABASE_VERSION = 2;

    public static final String STATIC_TABLE = "static";
    public static final String ROUTES_TABLE = "routes";

    public static final String KEY_ROWID = "id";
    public static final String KEY_LABEL = "label";

    public static final String KEY_ADDRESS = "dest_address";
    public static final String KEY_DEST_ADDRESS = "address";

    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public static final String KEY_COORDINATES = "coordinates";

    public static final String KEY_ORIGIN_LATIUTDE = "originLat";
    public static final String KEY_ORIGIN_LONGITUDE = "originLng";

    public static final String KEY_DEST_LATIUTDE = "destLat";
    public static final String KEY_DEST_LONGITUDE = "destLng";
    public static final String KEY_TRANSPORT = "transport";

    private Context mContext;

    public BookmarksDBHelper(@Nullable Context context, String dbName, int version) {
        super(context, dbName, null, version);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Static faking table
        db.execSQL("create table " + STATIC_TABLE + " (id integer primary key, " + KEY_LATITUDE + " FLOAT, "
                + KEY_LONGITUDE + " FLOAT, " + KEY_ADDRESS + " TEXT, " + KEY_LABEL + " TEXT)");

        // Route table
        db.execSQL("create table " + ROUTES_TABLE + " (id integer primary key, " + KEY_COORDINATES + " TEXT, " + KEY_DEST_ADDRESS + " TEXT, " + KEY_ADDRESS + " TEXT, " + KEY_LABEL + " TEXT, " + KEY_TRANSPORT + " INTEGER" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // добавить колонки
            db.execSQL("ALTER TABLE " + ROUTES_TABLE + " ADD COLUMN " + KEY_COORDINATES + " TEXT");
        }
    }

    // Save bookmark
    // @param routes - null if fixed route
    public void saveBookmark(List<MultipleRoutesInfo> routes, String bookmarkName) {
        ContentValues cv = new ContentValues();

        if (routes == null) {
            cv.put(BookmarksDBHelper.KEY_LATITUDE, SpoofingPlaceInfo.latitude);
            cv.put(BookmarksDBHelper.KEY_LONGITUDE, SpoofingPlaceInfo.longtiude);
            cv.put(BookmarksDBHelper.KEY_ADDRESS, SpoofingPlaceInfo.address);
        }

        JSONArray coordinates = new JSONArray();
        JSONArray transports = new JSONArray();

        for (MultipleRoutesInfo info : routes) {
            List<GeoPoint> points = info.getRoute();
            GeoPoint origin = points.get(0);
            GeoPoint dest = points.get(points.size() - 1);

            String coordinateLine = origin.getLatitude() + "," + origin.getLongitude() + "&" + dest.getLatitude() + "," + dest.getLongitude();
            coordinates.put(coordinateLine);

            int transport = info.getTransport().ordinal();
            transports.put(transport);
        }

        // TODO ORIGIN ADDRESS
        String originAddress = routes.get(0).getAddress();
        String destAddress = routes.get(RouteManager.getLatestElement()).getAddress();

        if (MainServiceControl.isRouteSpoofingServiceRunning(mContext)) {
            cv.put(BookmarksDBHelper.KEY_COORDINATES, coordinates.toString());
            cv.put(BookmarksDBHelper.KEY_TRANSPORT, transports.toString());
            cv.put(BookmarksDBHelper.KEY_ADDRESS, originAddress);
            cv.put(BookmarksDBHelper.KEY_DEST_ADDRESS, destAddress);
        }

        cv.put(BookmarksDBHelper.KEY_LABEL, bookmarkName);

        if (MainServiceControl.isRouteSpoofingServiceRunning(mContext))
            getWritableDatabase().insert(BookmarksDBHelper.ROUTES_TABLE, null, cv);
        else
            getWritableDatabase().insert(BookmarksDBHelper.STATIC_TABLE, null, cv);

    }

}
