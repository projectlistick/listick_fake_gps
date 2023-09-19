package project.listick.fakegps.API;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.WebClient;

// Created by LittleAngry
// powered by OpenRouteService
public class LFGSimpleApi {

    public static final String API_BASE_URL = "https://littleangry.ru/lfg/";

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_CONNECTION_FAILED = -1;
    public static final int CODE_RECAPTCHA_RESPONSE = -2;
    public static final int CODE_BAD_RECAPTCHA_RESPONSE = -3;
    public static final int CODE_UNKNOWN_ERROR = -4;


    public LFGSimpleApi() {
    }

    public static class Geocoder {
        public static String getAutcompleteURL(String address, double latitude, double longitude) {

            StringBuilder builder = new StringBuilder();
            builder.append(API_BASE_URL)
                    .append("autocomplete.php")
                    .append("?text=")
                    .append(address)
                    .append("&focus_point_lon=")
                    .append(longitude)
                    .append("&focus_point_lat=")
                    .append(latitude);


            return builder.toString();
        }
    }

    public static class Elevation {
        private final File mCacheDir;

        public interface ElevationCallback {
            void onRequestSuccess(float altitude);
            void onCaptchaResult();
            void onRequestError();
        }

        public Elevation(File cacheDir) {
            this.mCacheDir = cacheDir;
        }

        private String getElevationServiceURL(double latitude, double longitude) {
            StringBuilder builder = new StringBuilder();
            builder.append(API_BASE_URL)
                    .append("get_altitude.php")
                    .append("?geometry=")
                    .append(longitude).append(",").append(latitude);
            return builder.toString();
        }

        public void getElevation(double latitude, double longitude, String challengeResult, ElevationCallback callback) {

            JSONObject root = new JSONObject();
            try {
                root.put("challenge_result", challengeResult);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(root.toString(), JSON);

                Request request = new Request.Builder()
                        .url(getElevationServiceURL(latitude, longitude))
                        .post(body)
                        .build();

                WebClient.getInstance().makeRequest(request, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        callback.onRequestError();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String body = response.body().string();

                        try {
                            JSONObject root = new JSONObject(body);

                            if (root.has("error_text") && root.has("error_code")) {
                                int code = root.getInt("error_code");
                                if (code == CODE_RECAPTCHA_RESPONSE || code == CODE_BAD_RECAPTCHA_RESPONSE) {
                                    callback.onCaptchaResult();
                                    return;
                                }
                            }

                            JSONArray geometry = root.getJSONArray("geometry");
                            float altitude = (float) geometry.getDouble(2);
                            callback.onRequestSuccess(altitude);
                        } catch (JSONException e) {
                            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
                            callback.onRequestError();
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Directions {
        private final double sourcelat;
        private final double sourcelong;
        private final double destlat;
        private final double destlong;

        private double distance;

        private final ERouteTransport transport;

        private final String captchaResult;

        public static class DirectionsResponse {
            public String error;
            public int code;

            public ArrayList<GeoPoint> result;
            public double distance;
        }

        public interface DirectionsCallback {
            void onResult(DirectionsResponse response);
        }

        public Directions(double sourcelat, double sourcelong, double destlat, double destlong, ERouteTransport transport, String captchaResult) {
            this.sourcelat = sourcelat;
            this.sourcelong = sourcelong;
            this.destlat = destlat;
            this.destlong = destlong;
            this.transport = transport;
            this.captchaResult = captchaResult;
        }

        private String getRouteBuilderUrl() {
            return API_BASE_URL + "build_route.php?profile=" + transport.name();
        }

        public void downloadRoute(DirectionsCallback callback) {
            DirectionsResponse response = new DirectionsResponse();

            Map<String, Object[]> coordinates = new HashMap<>();
            coordinates.put("coordinates", new Object[]{
                    new Object[]{sourcelong, sourcelat},
                    new Object[]{destlong, destlat},
            });

            JSONObject root = new JSONObject();
            JSONObject data = new JSONObject(coordinates);

            try {
                data.put("elevation", String.valueOf(true));
                root.put("data", data);
                if (captchaResult != null) {
                    root.put("challenge_result", captchaResult);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(root.toString(), JSON);

            Request request = new Request.Builder()
                    .url(getRouteBuilderUrl())
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .post(body)
                    .build();

            WebClient.getInstance().makeRequest(request, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("RouteBuilder", "onFailure: ", e);
                    response.error = e.getMessage();
                    response.code = CODE_CONNECTION_FAILED;
                    callback.onResult(response);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response _response) throws IOException {
                    try {
                        String responseString = _response.body().string();
                        Log.d("RouteBuilder", "onResponse: " + responseString);
                        JSONObject contentObject = new JSONObject(responseString);

                        if (contentObject.has("error_text") && contentObject.has("error_code")) {
                            response.code = contentObject.getInt("error_code");
                            response.error = contentObject.getString("error_text");
                            callback.onResult(response);
                            return;
                        }

                        JSONArray routeArray = contentObject.getJSONArray("routes");
                        JSONObject routes = routeArray.getJSONObject(0);
                        String encodedString = routes.getString("geometry");
                        response.result = decodePolyline(encodedString, true);

                        JSONObject jsonObject = routes.getJSONObject("summary");
                        response.distance = distance = jsonObject.getDouble("distance");

                        response.code = CODE_SUCCESS;
                    } catch (Exception e) {
                        android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
                        response.code = CODE_UNKNOWN_ERROR;
                        response.error = e.getMessage();
                    }

                    callback.onResult(response);
                }
            });
        }


        public double getDistance() {
            return distance;
        }

        public static ArrayList<GeoPoint> decodePolyline(String encoded, boolean is3D) {
            ArrayList<GeoPoint> poly = new ArrayList<>();
            int index = 0;
            int len = encoded.length();
            int lat = 0, lng = 0, ele = 0;
            while (index < len) {
                // latitude
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int deltaLatitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += deltaLatitude;

                // longitude
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int deltaLongitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += deltaLongitude;

                if (is3D) {
                    // elevation
                    shift = 0;
                    result = 0;
                    do {
                        b = encoded.charAt(index++) - 63;
                        result |= (b & 0x1f) << shift;
                        shift += 5;
                    } while (b >= 0x20);
                    int deltaElevation = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                    ele += deltaElevation;

                    GeoPoint p = new GeoPoint((double) lat / 1e5, (double) lng / 1e5, (double) ele / 100);
                    poly.add(p);
                } else {
                    GeoPoint p = new GeoPoint((double) lat / 1e5, (double) lng / 1e5, Integer.MIN_VALUE);
                    poly.add(p);
                }
            }
            return poly;
        }


    }

}

