package project.listick.fakegps.UI;

import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;

import project.listick.fakegps.R;
import project.listick.fakegps.UI.Fragments.PreferenceFragment;

public class SettingsActivity extends Edge2EdgeActivity {
    public static final String FAKEGPS_SETTINGS = "fakegps_settings";

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true);
        setContentView(R.layout.activity_preferences);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PreferenceFragment()).commitNow();
        }
    }
}

