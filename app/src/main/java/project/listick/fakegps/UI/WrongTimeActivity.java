package project.listick.fakegps.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;

import com.google.android.material.button.MaterialButton;

import project.listick.fakegps.R;

public class WrongTimeActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong_time);

        MaterialButton adjustClock = findViewById(R.id.adjust_clock);

        adjustClock.setOnClickListener(view -> {
            startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
            finish();
        });
    }
}
