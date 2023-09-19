package project.listick.fakegps;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeviceUtils {

    public static Map<String, String> getCPUInfo() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));

        String str;

        Map<String, String> output = new HashMap<>();

        while ((str = br.readLine()) != null) {
            String[] data = str.split(":");

            if (data.length > 1) {
                String key = data[0].trim().replace(" ", "_");
                if (key.equals("model_name")) key = "cpu_model";

                output.put(key, data[1].trim());
            }
        }

        br.close();

        return output;
    }

    public static long getTotalRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) FakeGPSApplication.getAppContext().getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        return mi.totalMem;
    }


}
