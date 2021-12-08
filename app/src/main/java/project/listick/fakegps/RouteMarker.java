package project.listick.fakegps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/*
 * Created by LittleAngry on 11.01.19 (macOS 10.12)
 * */
public class RouteMarker extends Overlay {

    public enum Type {
        SOURCE, DEST
    }

    public static class OriginAndDestMarker {
        public RouteMarker origin;
        public RouteMarker dest;
    }

    private GeoPoint position;
    private Point point;
    private Type type;

    public RouteMarker(Context context, RouteMarker.Type type){
        this.point = new Point();
        this.type = type;
    }

    public void setPosition(double latitude, double longitude){
        this.position = new GeoPoint(latitude, longitude);
    }

    private Bitmap setSize(Bitmap bitmap, int width, int height){
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private Bitmap getBitmapByType(Type type){
        return (type == Type.SOURCE) ? ImageUtils.getBitmapFromVectorDrawable(R.drawable.source_marker) : ImageUtils.getBitmapFromVectorDrawable(R.drawable.dest_marker);
    }

    @Override
    public void draw(Canvas canvas, MapView osmv, boolean shadow) {
        Projection projection = osmv.getProjection();
        projection.toPixels(position, point);

        Bitmap bitmap = getBitmapByType(type);
        bitmap = setSize(bitmap, bitmap.getWidth(), bitmap.getHeight());

        canvas.drawBitmap(bitmap, point.x - bitmap.getWidth() / 2, point.y - bitmap.getHeight() / 2, null);
    }
}
