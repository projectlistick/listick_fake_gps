package project.listick.fakegps.API;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import project.listick.fakegps.AsyncWebClient;

/*
 * Created by LittleAngry on 30.01.19 (macOS 10.12)
 * */

public class YoursRoutingApi {

    private double originLat;
    private double originLng;
    private double destLat;
    private double destLng;

    private int attempts = 1;

    public YoursRoutingApi(double originLat, double originLng, double destLat, double destLng) {
        this.originLat = originLat;
        this.originLng = originLng;
        this.destLat = destLat;
        this.destLng = destLng;

        getContent();

    }

    private String buildUrl() {

        StringBuilder url = new StringBuilder();
        url.append("http://www.yournavigation.org/api/1.0/gosmore.php?flat=");
        url.append(originLat);
        url.append("&flon=");
        url.append(originLng);
        url.append("&tlat=");
        url.append(destLat);
        url.append("&tlon=");
        url.append(destLng);
        url.append("&format=geojson");

        return url.toString();
    }

    public String getContent() {
        return new AsyncWebClient.Sync(buildUrl()).connect();
    }

    public ArrayList<GeoPoint> downloadRoute(String content) {
        if (content == null)
            return null;

        ArrayList<GeoPoint> pointsList = new ArrayList<GeoPoint>();

        try {

            JSONObject object = new JSONObject(content);
            JSONArray pointsArray = object.getJSONArray("coordinates");

            for (int i = 0; i < pointsArray.length(); i++) {
                pointsList.add(new GeoPoint(
                        Double.parseDouble(pointsArray.getString(i).split(",")[1].replace("[", "").replace("]", "")),
                        Double.parseDouble(pointsArray.getString(i).split(",")[0].replace("[", "").replace("]", "")), Integer.MIN_VALUE));
            }
            return pointsList;
        } catch (Exception e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            if (attempts++ <= 3)
                downloadRoute(content);
            return null;
        }
    }

}
