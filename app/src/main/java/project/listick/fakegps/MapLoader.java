package project.listick.fakegps;

import android.content.Context;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;

/*
 * Created by LittleAngry on 11.01.19 (macOS 10.12)
 * */
public class MapLoader {

    public static final String DEFAULT_TILE_PROVIDER = "default_tile_provider";
    public static final int DEFAULT_TILES = 0;
    public static final int WIKIMEDIA_TILES = 1;
    public static final long ZOOM_ANIMATION_SPEED = 1000L;
    public static final double OPTIMIZED_ZOOM_LVL = 17d;
    public static final double MIN_ZOOM_LVL = 15d;

    private final Context mContext;

    public MapLoader(Context context) {
        this.mContext = context;
    }

    public void load(MapView map, TextView copyright) {
        IConfigurationProvider conf = Configuration.getInstance();
        conf.setUserAgentValue(mContext.getPackageName());
        conf.setOsmdroidBasePath(new File(mContext.getCacheDir(), "mapkit"));
        conf.setOsmdroidTileCache(new File(mContext.getCacheDir(), "maptile"));

        map.setMultiTouchControls(true);

        map.setTileSource(TileSourceFactory.MAPNIK);

        if (AppPreferences.getMapTileProvider(mContext) == WIKIMEDIA_TILES) {
            map.setTileSource(new XYTileSource("Wikimedia",
                    1, 19, 256, ".png", new String[] {
                    "https://maps.wikimedia.org/osm-intl/" },
                    "Wikimedia maps | Map data © OpenStreetMap contributors",
                    new TileSourcePolicy(1,
                            TileSourcePolicy.FLAG_NO_BULK
                                    | TileSourcePolicy.FLAG_NO_PREVENTIVE
                                    | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                                    | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
                    )));
            copyright.append(" " + mContext.getString(R.string.wikimedia_contributors));
        }

        int nightModeFlags = mContext.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);

        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMinZoomLevel(3d);
    }
}