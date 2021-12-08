package project.listick.fakegps.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import project.listick.fakegps.Contract.PermissionsImpl;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Presenter.PermissionsPresenter;
import project.listick.fakegps.R;

public class PermissionsActivity extends Activity implements PermissionsImpl.UI {

    public static final int PERMISSION_REQUEST_CODE = 0;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;


    private PermissionsPresenter presenter;
    private Button mRequestPermissions;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            finishAffinity();
            return;
        }
        else { Toast.makeText(getBaseContext(), getString(R.string.doubletap_to_exit), Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        mRequestPermissions = findViewById(R.id.btn_continue);

        presenter = new PermissionsPresenter(this);

        presenter.onActivityLoad();

        mRequestPermissions.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.onPermissionRequest();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                presenter.onPermissionGranted();
            else
                presenter.onPermissionDenied();
        }
    }

    @Override
    public void setButtonError() {
        mRequestPermissions.setBackground(getDrawable(R.drawable.uisearchbar_error));
        mRequestPermissions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.attenuation));
        mRequestPermissions.setTextColor(getColor(R.color.white));


        CountDownTimer timer = new CountDownTimer(800, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mRequestPermissions.setBackground(getDrawable(R.drawable.uisearchbar));

                mRequestPermissions.setTextColor(getColor(R.color.uisearch_textcolor));

                mRequestPermissions.startAnimation(AnimationUtils.loadAnimation(PermissionsActivity.this, R.anim.attenuation));
                mRequestPermissions.setOnTouchListener(null);
            }
        };
        timer.start();
    }
}
