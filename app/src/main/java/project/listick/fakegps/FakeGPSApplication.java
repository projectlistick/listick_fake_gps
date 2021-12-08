package project.listick.fakegps;

import android.app.Application;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class FakeGPSApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static Context getAppContext() {
        return context;
    }

    public static InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

}
