package project.listick.fakegps.Presenter;

import android.Manifest;
import android.app.Activity;

import project.listick.fakegps.Contract.PermissionsImpl;
import project.listick.fakegps.Contract.PermissionsImpl.Presenter;
import project.listick.fakegps.UI.PermissionsActivity;

/*
 * Created by LittleAngry on 27.12.18 (macOS 10.12)
 * */
public class PermissionsPresenter implements Presenter {

    private Activity mActivity;
    private PermissionsImpl.UI mUserInterface;

    public PermissionsPresenter(PermissionsImpl.UI userInterface) {
        this.mActivity = (Activity) userInterface;
        this.mUserInterface = userInterface;
    }

    @Override
    public void onActivityLoad() {

    }

    @Override
    public void onPermissionRequest() {
        requestPermissions();
    }

    @Override
    public void onPermissionGranted() {
        mActivity.finish();
    }

    @Override
    public void onPermissionDenied() {
        mUserInterface.showErrorOnButton();
    }

    private void requestPermissions() {
        mActivity.requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PermissionsActivity.PERMISSION_REQUEST_CODE);
    }

}
