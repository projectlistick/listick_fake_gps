package project.listick.fakegps;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

public class AsyncGeocoder {

    private final Geocoder mGeocoder;
    private final Activity mActivity;

    public interface Callback {
        void onSuccess(List<Address> locations);
        void onError();
    }

    public AsyncGeocoder(Activity activity){
        this.mGeocoder = new Geocoder(activity);
        this.mActivity = activity;
    }

    public void getFromAddress(@NonNull String address, @NonNull Callback callback) {

        Runnable runnable = () -> {
            try {
                List<Address> result = mGeocoder.getFromLocationName(address, 5);

                if (result == null || result.isEmpty()) {
                    mActivity.runOnUiThread(callback::onError);
                    return;
                }

                mActivity.runOnUiThread(() -> callback.onSuccess(result));
            } catch (IOException e) {
                mActivity.runOnUiThread(callback::onError);
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void getLocationAddress(double latitude, double longitude, @NonNull Callback callback){

        Runnable runnable = () -> {
            try {
                List<Address> result = mGeocoder.getFromLocation(latitude, longitude, 5);

                if (result == null || result.isEmpty()) {
                    mActivity.runOnUiThread(callback::onError);
                    return;
                }
                mActivity.runOnUiThread(() -> callback.onSuccess(result));
            } catch (IOException e) {
                mActivity.runOnUiThread(callback::onError);
                android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void autocomplete(String address, double latitude, double longitude, Callback callback) {

//        AsyncWebClient webClient = new AsyncWebClient(mActivity, true);
//        webClient.connect(LFGSimpleApi.Geocoder.getAutcompleteURL(address, latitude, longitude), new AsyncWebClient.Callback() {
//            @Override
//            public void onSuccess(ResponseBody responseBody) {
//                String body = null;
//                try {
//                    body = responseBody.string();
//                } catch (IOException e) {
//                    android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
//                    onError();
//                }
//                if (body == null) {
//                    onError();
//                    return;
//                }
//
//                try {
//                    JSONObject contentObject = new JSONObject(body);
//                    JSONArray features = contentObject.getJSONArray("features");
//
//                    List<Address> addressList = new ArrayList<>();
//                    for (int i = 0; i < features.length(); i++) {
//                        JSONObject arrays = features.getJSONObject(i);
//
//                        JSONObject geometry = arrays.getJSONObject("geometry");
//                        JSONArray coordinates = geometry.getJSONArray("coordinates");
//
//                        double latitude = coordinates.getDouble(1);
//                        double longitude = coordinates.getDouble(0);
//
//                        String a = mGeocoder.getFromLocation(latitude, longitude, 1).get(0).getAddressLine(0);
//                        Address address = new Address(Locale.getDefault());
//                        address.setAddressLine(0, a);
//
//                        address.setLatitude(latitude);
//                        address.setLongitude(longitude);
//
//                        addressList.add(address);
//
//                        if (i == 3) {
//                            break;
//                        }
//                    }
//
//                    callback.onSuccess(addressList);
//                } catch (Exception e) {
//                    android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
//                }
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        });
    }

}
