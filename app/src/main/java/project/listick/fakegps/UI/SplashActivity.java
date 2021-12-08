package project.listick.fakegps.UI;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;

import project.listick.fakegps.Contract.SplashImpl;
import project.listick.fakegps.Presenter.SplashPresenter;

/*
 * Created by LittleAngry on 27.12.18 (macOS 10.12)
 * */
public class SplashActivity extends Activity implements SplashImpl.UI {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new SplashPresenter(this).onAppLoad();
    }
}
