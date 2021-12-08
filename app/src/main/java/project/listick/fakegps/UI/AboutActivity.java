package project.listick.fakegps.UI;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import project.listick.fakegps.BuildConfig;
import project.listick.fakegps.R;

/*
 * Created by LittleAngry on 03.03.19 (macOS 10.12)
 * */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        TextView version = findViewById(R.id.version);
        TextView logo = findViewById(R.id.logo);

        version.setText(BuildConfig.VERSION_NAME);
        logo.setText(BuildConfig.VERSION_NAME);
    }
}
