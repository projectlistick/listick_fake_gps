package project.listick.fakegps.UI;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import project.listick.fakegps.AppWindowManager;

public class Edge2EdgeActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        AppWindowManager.makeEdge2Edge(getWindow());
        AppWindowManager.makeDarkNavbarButtons(getWindow(), theme);
        AppWindowManager.drawColoredIcons(getWindow(), theme);
    }
}
