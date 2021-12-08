package project.listick.fakegps;


/*
 * Created by LittleAngry on 07.07.2019 (macOS 10.14)
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

public class AppUtils {

    private Context context;

    public AppUtils(Context context){
        this.context = context;
    }

    public static String getPackageName(Context context){
        return context.getPackageName();
    }

    public void makeNonSystemApp(){
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> makeNonSysAppTask = new AsyncTask<Void, Void, Void>() {
            private ProgressDialog waitTaskDialog;
            String appDir;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                appDir = context.getPackageCodePath();
                waitTaskDialog = ProgressDialog.show(context, "", context.getString(R.string.please_wait), true);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String dirName = appDir.replace("/base.apk", "");
                    dirName = dirName.split("/")[3];

                    Process mount = Runtime.getRuntime().exec(new String[] {"su", "-c", "mount -o rw,remount /system"});
                    mount.waitFor();
                    Process move = Runtime.getRuntime().exec(new String[] {"su", "-c",  "cp -f -r " + appDir.replace("/base.apk", "") + " /data/app/" + dirName});
                    move.waitFor();
                    Process removeSysApp = Runtime.getRuntime().exec(new String[] {"su", "-c", "rm -r -f " + appDir.replace("/base.apk", "")});
                    removeSysApp.waitFor();
                    Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
                } catch (IOException | InterruptedException e) {
                    android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                waitTaskDialog.cancel();
            }
        };

        makeNonSysAppTask.execute();
    }

    public void makeSystemApp() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> makeSysAppTask = new AsyncTask<Void, Void, Void>() {

            private String appDir;
            private ProgressDialog waitTaskDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                appDir = context.getApplicationContext().getPackageCodePath();
                waitTaskDialog = ProgressDialog.show(context, "", context.getString(R.string.please_wait), true);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String dirName = appDir.replace("/base.apk", "");
                    dirName = dirName.split("/")[3];

                    Process mount = Runtime.getRuntime().exec(new String[] {"su", "-c", "mount -o rw,remount /system"});
                    int result = mount.waitFor();

                    if (result != 0){
                        Toast.makeText(context, context.getString(R.string.error_grant_su_permission), Toast.LENGTH_LONG).show();
                    } else {
                        Process move = Runtime.getRuntime().exec(new String[]{"su", "-c", "cp -f -r " + appDir.replace("/base.apk", "") + " " + "/system/priv-app/" + dirName});
                        move.waitFor();
                        Process removeApp = Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -r -f " + appDir.replace("/base.apk", "")}); // remove app folder
                        removeApp.waitFor();
                        Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
                    }
                } catch (Exception e) {
                    android.util.Log.d(project.listick.fakegps.BuildConfig.APPLICATION_ID, null, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                waitTaskDialog.cancel();
            }
        };

        makeSysAppTask.execute();

    }
}
