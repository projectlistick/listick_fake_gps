package project.listick.fakegps.Presenter;

import android.app.Activity;
import android.content.Intent;

import project.listick.fakegps.Contract.SplashImpl;
import project.listick.fakegps.UI.MapsActivity;

/*
 * Created by LittleAngry on 27.12.18 (macOS 10.12)
 * */
public class SplashPresenter implements SplashImpl.Presenter {

    private final Activity mActivity;

    public SplashPresenter(SplashImpl.UI userInterface){
        this.mActivity = (Activity) userInterface;
    }

    @Override
    public void onAppLoad() {
        hideSplash();
    }

    private void hideSplash(){
        mActivity.startActivity(new Intent(mActivity, MapsActivity.class));
        mActivity.finish();
    }
}
