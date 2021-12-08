package project.listick.fakegps.Presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import project.listick.fakegps.Contract.SearchImpl;
import project.listick.fakegps.Contract.SearchImpl.UI;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.R;
import project.listick.fakegps.SpoofingPlaceInfo;
import project.listick.fakegps.UI.SelectPointActivity;

/*
 * Created by LittleAngry on 13.01.19 (macOS 10.12)
 * */
public class SearchPresenter implements SearchImpl.Presenter {

    public static final String OPEN_SEARCH = "open_search";

    private static final int ORIGIN = 1;
    private static final int DESTINATION = 2;

    private SearchImpl.UI mUserInterface;
    private Activity mActivity;

    private double mDestLat;
    private double mDestLong;

    private double mOriginLat;
    private double mOriginLong;

    private String mDestAddress;
    private String mOriginAddress;

    private ERouteTransport mTransport;

    private boolean preparedForFinish;

    public SearchPresenter(UI userInterface){
        mUserInterface = userInterface;
        mActivity = (Activity) userInterface;
    }

    @Override
    public void onActivityLoad() {
        getOriginAddress();
        onTransport(ERouteTransport.ROUTE_CAR);
    }

    @Override
    public void onDestination() {
        findOnMap(DESTINATION);
    }

    @Override
    public void onOrigin() {
        findOnMap(ORIGIN);
    }

    @Override
    public void onContinue() {
        sendResults();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null){
            if (requestCode == DESTINATION) {
                this.preparedForFinish = true;
                mDestLat = data.getDoubleExtra(ListickApp.LATITUDE, 0d);
                mDestLong = data.getDoubleExtra(ListickApp.LONGITUDE, 0d);
                mDestAddress = data.getStringExtra(SpoofingPlaceInfo.ADDRESS);

                mUserInterface.setDestAddress(mDestAddress);
            } else if (requestCode == ORIGIN) {
                mOriginLat = data.getDoubleExtra(ListickApp.LATITUDE, 0d);
                mOriginLong = data.getDoubleExtra(ListickApp.LONGITUDE, 0d);
                mOriginAddress = data.getStringExtra(SpoofingPlaceInfo.ADDRESS);

                mUserInterface.setOriginAddress(mOriginAddress);
            }
        }
    }

    @Override
    public void selectOnMap() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = mActivity.getCurrentFocus();
        if (view == null) { view = new View(mActivity); }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        view.clearFocus();

        Intent intent = mActivity.getIntent();
        double originLat = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        double originLong = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);

        mActivity.startActivityForResult(new Intent(mActivity, SelectPointActivity.class)
                .putExtra(ListickApp.LATITUDE,  originLat)
                .putExtra(ListickApp.LONGITUDE, originLong), DESTINATION); // 1 is dest request code
    }

    private void findOnMap(int field){
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = mActivity.getCurrentFocus();
        if (view == null) view = new View(mActivity);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();

        Intent intent = mActivity.getIntent();

        double originLat = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        double originLong = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);

        mActivity.startActivityForResult(new Intent(mActivity, SelectPointActivity.class)
                    .putExtra(ListickApp.LATITUDE,  originLat)
                    .putExtra(ListickApp.LONGITUDE, originLong)
                    .putExtra(OPEN_SEARCH, true), field); // 1 is dest request code

    }

    private void sendResults() {
        Intent intent = new Intent();

        intent.putExtra(SpoofingPlaceInfo.ORIGIN_LAT, mOriginLat);
        intent.putExtra(SpoofingPlaceInfo.ORIGIN_LNG, mOriginLong);

        intent.putExtra(SpoofingPlaceInfo.DEST_LAT, mDestLat);
        intent.putExtra(SpoofingPlaceInfo.DEST_LNG, mDestLong);

        intent.putExtra(SpoofingPlaceInfo.ORIGIN_ADDRESS, mOriginAddress);
        intent.putExtra(SpoofingPlaceInfo.DEST_ADDRESS, mDestAddress);
        intent.putExtra(SpoofingPlaceInfo.TRANSPORT, mTransport);


        if (preparedForFinish) {
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finishAfterTransition();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.you_dont_select_dest)
                    .setMessage(R.string.please_select_dest)
                    .setCancelable(true)
                    .setPositiveButton(mActivity.getString(R.string.okay), (dialog, which) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();

        }
    }


    @Override
    public void onTransport(ERouteTransport transport) {
        changeTransport(transport);
    }

    private void changeTransport(ERouteTransport transport) {
        this.mTransport = transport;
        mUserInterface.removeTransport(transport);
        mUserInterface.setTransport(transport);
    }

    private void getOriginAddress() {
        Intent intent = mActivity.getIntent();
        mUserInterface.setOriginAddress(intent.getStringExtra(SpoofingPlaceInfo.ORIGIN_ADDRESS));

        mOriginLat = intent.getDoubleExtra(ListickApp.LATITUDE, 0d);
        mOriginLong = intent.getDoubleExtra(ListickApp.LONGITUDE, 0d);
    }

}
