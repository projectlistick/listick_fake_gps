package project.listick.fakegps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/*
 * Created by LittleAngry on 08.01.19 (macOS 10.12)
 * */
public class LocationMarker extends Overlay {

    private GeoPoint geoPoint;
    private Point mDrawPixel = new Point();
    private Bitmap bitmap;

    private Bitmap mFixedIcon;
    private Bitmap mRouteIcon;

    public LocationMarker() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFixedIcon = ImageUtils.getBitmapFromVectorDrawable(R.drawable.ic_vector_dot);
                mRouteIcon = ImageUtils.getBitmapFromVectorDrawable(R.drawable.ic_dot_route);

                int iconSize = (int) ImageUtils.convertDpToPixel(36);

                mFixedIcon = Bitmap.createScaledBitmap(mFixedIcon, iconSize, iconSize, true);
                mRouteIcon = Bitmap.createScaledBitmap(mRouteIcon, iconSize, iconSize, true);

                bitmap = mFixedIcon;

            }
        }, 1000);

    }

    void setPosition(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    void setBearing(float bearing) {
        Matrix matrix = new Matrix();
        matrix.postRotate(bearing);

        // TODO: ROUTE_ICON
        //bitmap = (SpoofingPlaceInfo.isRoute) ? mRouteIcon : mFixedIcon;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bitmap = mRouteIcon;
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        }, 1000);

    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {

        Projection pj = osmv.getProjection();
        pj.toPixels(geoPoint, mDrawPixel);

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        p.setDither(true);


        if (bitmap != null)
            c.drawBitmap(bitmap, mDrawPixel.x - bitmap.getWidth() / 2, mDrawPixel.y - bitmap.getHeight() / 2, p);

        osmv.invalidate();

    }

}