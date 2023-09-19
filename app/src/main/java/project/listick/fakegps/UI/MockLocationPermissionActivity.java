package project.listick.fakegps.UI;

import static project.listick.fakegps.ROMUtils.MIUI;
import static project.listick.fakegps.ROMUtils.ONEUI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.PermissionManager;
import project.listick.fakegps.Presenter.MockLocationPermissionPresenter;
import project.listick.fakegps.R;
import project.listick.fakegps.ROMUtils;

public class MockLocationPermissionActivity extends Activity {

    public static final int ML_GRANTED_REQUEST_CODE = 8;

    private boolean isInitialized = false;
    private MockLocationPermissionPresenter presenter;

    @Override
    protected void onResume() {
        super.onResume();

        if (isInitialized && PermissionManager.isMockLocationsEnabled(this)) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_location_permission);
        isInitialized = true;

        presenter = new MockLocationPermissionPresenter(this);
        TextView ignore = findViewById(R.id.ignore);
        Button developerSettings = findViewById(R.id.btn_continue);

        View.OnClickListener finish = v -> presenter.onContinue();

        developerSettings.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                int developerSettings = Settings.Secure.getInt(getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);

                if (developerSettings == 0) {
                    final int VENDOR_UI = ROMUtils.getVendorUI();

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MockLocationPermissionActivity.this);
                    builder.setTitle(R.string.developer_options_disabled)
                            .setMessage(VENDOR_UI == MIUI ? Html.fromHtml(getString(R.string.developer_options_instruction_miui)) : Html.fromHtml(getString(R.string.developer_options_instruction)))
                            .setCancelable(false)
                            .setPositiveButton(R.string.open_video, (dialog, which) -> {
                                String url;

                                if (VENDOR_UI == MIUI)
                                    url = getString(R.string.miui_developer_instruction_link);
                                else if (VENDOR_UI == ONEUI)
                                    url = getString(R.string.oneui_developer_instruction_link);
                                else
                                    url = getString(R.string.aosp_developer_instruction_link);

                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);

                            })
                            .setNeutralButton(R.string.back, (dialog, which) -> dialog.cancel())
                            .setNegativeButton(R.string.open_settings,
                                    (dialog, id) -> {
                                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    })
                            .setIcon(R.drawable.ic_baseline_developer_mode_24)
                            .show();

                } else
                    presenter.onDeveloperSettings();
            }
        });

        ignore.setOnClickListener(finish);


    }
}
