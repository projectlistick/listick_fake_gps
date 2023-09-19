package project.listick.fakegps;

import android.os.SystemClock;
import android.view.View;

/*
* Created by LittleAngry on 03.10.2019 (Windows 10)
* */

public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long MIN_CLICK_INTERVAL = 600;

    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime-mLastClickTime;
        mLastClickTime = currentClickTime;

        if(elapsedTime <= MIN_CLICK_INTERVAL)
            return;

        onSingleClick(v);
    }

}

// https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button