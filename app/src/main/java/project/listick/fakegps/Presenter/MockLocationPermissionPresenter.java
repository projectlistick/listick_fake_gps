package project.listick.fakegps.Presenter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import project.listick.fakegps.R;

import static project.listick.fakegps.Contract.MockLocationPermissionImpl.Presenter;

/*
 * Created by LittleAngry on 27.12.18 (macOS 10.12)
 * */
public class MockLocationPermissionPresenter implements Presenter {

    private Activity mActivity;

    public MockLocationPermissionPresenter(Activity activity){
        this.mActivity = activity;
    }


    @Override
    public void onDeveloperSettings() {
        openDeveloperSettings();
    }

    private void openDeveloperSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);

            View decorView = mActivity.getWindow().getDecorView();
            Snackbar.make(decorView, mActivity.getString(R.string.dev_settings_error), Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void onContinue() {
        mActivity.finish();
    }
}
