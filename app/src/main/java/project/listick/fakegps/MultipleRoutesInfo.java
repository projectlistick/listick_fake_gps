package project.listick.fakegps;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.RouteMarker.OriginAndDestMarker;

public class MultipleRoutesInfo implements Parcelable {

    private OriginAndDestMarker originAndDestMarkers = new OriginAndDestMarker();

    private ArrayList<GeoPoint> mRoute;
    private int mPauseSeconds = -1;
    private int mSpeed = -1;
    private int mSpeedDiff = -1;

    private float mElevation = -1;
    private float mElevationDiff = -1;

    private String mAddress;
    private double mDistance;
    private ERouteTransport mTransport;

    public MultipleRoutesInfo() {

    }

    MultipleRoutesInfo(Parcel in) {
        //in.readList(mRoute, GeoPoint.class.getClassLoader());
        mRoute = (ArrayList<GeoPoint>) in.readSerializable();
        mPauseSeconds = in.readInt();
        mSpeed = in.readInt();
        mSpeedDiff = in.readInt();
        mElevation = in.readFloat();
        mElevationDiff = in.readFloat();
        mAddress = in.readString();
        mDistance = in.readDouble();
        mTransport = (ERouteTransport) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(mRoute);
        parcel.writeInt(mPauseSeconds);
        parcel.writeInt(mSpeed);
        parcel.writeInt(mSpeedDiff);
        parcel.writeFloat(mElevation);
        parcel.writeFloat(mElevationDiff);
        parcel.writeString(mAddress);
        parcel.writeDouble(mDistance);
        parcel.writeSerializable(mTransport);
    }

    public int getStartingPauseTime() {
        return mPauseSeconds;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getSpeedDiff() {
        return mSpeedDiff;
    }

    public float getElevation() {
        return mElevation;
    }

    public double getDistance() {
        return mDistance;
    }

    public float getElevationDiff() {
        return mElevationDiff;
    }

    public List<GeoPoint> getRoute() {
        return mRoute;
    }

    public void setStartingPauseTime(int pauseTime) {
        this.mPauseSeconds = pauseTime;
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public void setSpeedDiff(int speedDiff) {
        this.mSpeedDiff = speedDiff;
    }

    public void setElevation(float elevation) {
        this.mElevation = elevation;
    }

    public void setElevationDiff(float elevationDiff) {
        this.mElevationDiff = elevationDiff;
    }

    public void setRoute(List<GeoPoint> route) {
        this.mRoute = (ArrayList<GeoPoint>) route;
    }

    public void setDistance(double distance) {
        this.mDistance = distance;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public void setTransport(ERouteTransport transport) {
        this.mTransport = transport;
    }

    public ERouteTransport getTransport() {
        return mTransport;
    }

    public String getAddress() {
        return mAddress;
    }

    public OriginAndDestMarker getRouteMarker(Context context) {
        RouteMarker origin = new RouteMarker(context, RouteMarker.Type.SOURCE);
        origin.setPosition(mRoute.get(0).getLatitude(), mRoute.get(0).getLongitude());

        RouteMarker dest = new RouteMarker(context, RouteMarker.Type.DEST);
        dest.setPosition(mRoute.get(mRoute.size() - 1).getLatitude(), mRoute.get(mRoute.size() - 1).getLongitude());

        originAndDestMarkers.origin = origin;
        originAndDestMarkers.dest = dest;

        return originAndDestMarkers;
    }

    public static Creator<MultipleRoutesInfo> CREATOR = new Creator<MultipleRoutesInfo>() {
        public MultipleRoutesInfo createFromParcel(Parcel parcel) {
            return new MultipleRoutesInfo(parcel);
        }

        public MultipleRoutesInfo[] newArray(int size) {
            return new MultipleRoutesInfo[size];
        }
    };


}
