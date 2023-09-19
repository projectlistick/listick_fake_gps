package project.listick.fakegps.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import project.listick.fakegps.AsyncGeocoder;
import project.listick.fakegps.ImageUtils;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.MapLoader;
import project.listick.fakegps.MapUtil;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Presenter.SearchPresenter;
import project.listick.fakegps.R;
import project.listick.fakegps.RouteMarker;
import project.listick.fakegps.SpoofingPlaceInfo;

/*
 * Created by LittleAngry on 11.01.19 (macOS 10.12)
 * */
public class SelectPointActivity extends Edge2EdgeActivity {

    private String address;
    private MapView mapView;
    private EditText addressLabel;
    private AsyncGeocoder geocoder;
    private InputMethodManager mKeyboard;

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_point);

        mapView = findViewById(R.id.map);
        MapLoader mapLoader = new MapLoader(this);
        mapLoader.load(mapView, findViewById(R.id.copyright_txt));
        geocoder = new AsyncGeocoder(this);

        mKeyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Intent intent = getIntent();
        double sourceLat = intent.getDoubleExtra("latitude", 0d);
        double sourceLong = intent.getDoubleExtra("longitude", 0d);
        boolean openSearch = intent.getBooleanExtra(SearchPresenter.OPEN_SEARCH, false);
        boolean joystickSelectDest = intent.getBooleanExtra(JoystickActivity.JOYSTICK_SELECT_DEST, false);

        if (!joystickSelectDest) {
            RouteMarker routeMarker = new RouteMarker(RouteMarker.Type.SOURCE);
            routeMarker.setPosition(sourceLat, sourceLong);
            mapView.getOverlayManager().add(routeMarker);
        }

        TextView continueBtn = findViewById(R.id.continue_action);
        TextView search = findViewById(R.id.search);
        addressLabel = findViewById(R.id.search_destination);

        mapView.getController().animateTo(new GeoPoint(sourceLat, sourceLong));
        mapView.getController().setZoom(16d);

        search.setOnClickListener(v -> {
            String address = addressLabel.getText().toString();

            if (MapUtil.isCoordinates(address)) {
                MapUtil.goToCoordinates(mapView, address);
                return;
            }

            geocoder.getFromAddress(address, new AsyncGeocoder.Callback() {
                @Override
                public void onSuccess(List<Address> locations) {
                    double latitude = locations.get(0).getLatitude();
                    double longitude = locations.get(0).getLongitude();

                    addressLabel.getText().clear(); // clear label for show hint (map center address in hint)
                    mapView.getController().animateTo(new GeoPoint(latitude, longitude), 17d, MapLoader.ZOOM_ANIMATION_SPEED);
                }

                @Override
                public void onError() {
                    PrettyToast.show(SelectPointActivity.this, getString(R.string.failed_to_define_address), R.drawable.ic_search);
                }
            });

            mKeyboard.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        });

        continueBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (address != null && address.isEmpty()) {
                    address = getString(R.string.unknown);
                } else if (address == null) {
                    address = getString(R.string.unknown);
                }

                Intent result = new Intent(SelectPointActivity.this, MapsActivity.class);
                result.putExtra(SpoofingPlaceInfo.ORIGIN_LAT, sourceLat);
                result.putExtra(SpoofingPlaceInfo.ORIGIN_LNG, sourceLong);
                result.putExtra(ListickApp.LATITUDE, mapView.getMapCenter().getLatitude());
                result.putExtra(ListickApp.LONGITUDE, mapView.getMapCenter().getLongitude());
                result.putExtra(SpoofingPlaceInfo.ADDRESS, address);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        addressLabel.setOnTouchListener((view, motionEvent) -> {
            EditText field = (EditText) view;
            field.setHint(R.string.enter_address);
            return false;
        });

        addressLabel.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                search.performClick();
                return true;
            }
            return false;
        });

        mapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                geocoder.getLocationAddress(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude(), new AsyncGeocoder.Callback() {
                    @Override
                    public void onSuccess(List<Address> locations) {
                        String locationName = locations.get(0).getAddressLine(0);
                        address = locationName;
                        addressLabel.setHint(locationName);
                    }

                    @Override
                    public void onError() {
                        addressLabel.setHint(getString(R.string.failed_to_define_address));
                    }
                });
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        }, 100));


        RelativeLayout activityHeader = findViewById(R.id.header);
        RelativeLayout bar = findViewById(R.id.searchbar);
        LinearLayout bottomboardContainer = findViewById(R.id.bottomboard_container);

        TextView back = findViewById(R.id.back);

        back.setOnClickListener(view -> finish());

        if (openSearch) {
            addressLabel.requestFocus();
            addressLabel.postDelayed(() -> {
                if (mKeyboard == null) return;
                mKeyboard.showSoftInput(addressLabel, 0);
            }, 200);

        }

        AsyncGeocoder promptsGeocoder = new AsyncGeocoder(this);
        ListView promptsList = findViewById(R.id.results);
        addressLabel.addTextChangedListener(new TextWatcher() {

            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (s.toString().isEmpty())
                            return;


                        Log.d("Autocomplete", "Geocode: " + s.toString());
                        promptsGeocoder.autocomplete(s.toString(), mapView.getMapCenter().getLatitude(),
                                mapView.getMapCenter().getLongitude(), new AsyncGeocoder.Callback() {
                                    @Override
                                    public void onSuccess(List<Address> locations) {
                                        String[] prompts = new String[locations.size()];
                                        for (int i = 0; i < locations.size(); i++) {
                                            prompts[i] = locations.get(i).getAddressLine(0);
                                        }

                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectPointActivity.this,
                                                android.R.layout.simple_list_item_1, prompts);
                                        promptsList.setAdapter(adapter);
                                        promptsList.setVisibility(View.VISIBLE);
                                        promptsList.setOnItemClickListener((parent, view, position, id) -> {
                                            double latitude = locations.get(position).getLatitude();
                                            double longitude = locations.get(position).getLongitude();

                                            GeoPoint selectedPrompt = new GeoPoint(latitude, longitude);
                                            mapView.getController().animateTo(selectedPrompt, 17d, MapLoader.ZOOM_ANIMATION_SPEED);
                                            promptsList.setVisibility(View.GONE);
                                        });
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });

                    }
                }, 500L);


            }
        });

        mapView.setOnTouchListener((v, event) -> {
            promptsList.setVisibility(View.GONE);
            return false;
        });

        int activityHeaderHeight = activityHeader.getLayoutParams().height;
        ViewCompat.setOnApplyWindowInsetsListener(activityHeader, (v, insets) -> {
            int statusbarHeight = insets.getSystemWindowInsetTop();
            int navbarHeight = insets.getSystemWindowInsetBottom();
            bottomboardContainer.setPadding(0, 0, 0, navbarHeight + (int) ImageUtils.convertDpToPixel(8.0f));

            activityHeader.getLayoutParams().height = activityHeaderHeight + statusbarHeight;

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bar.getLayoutParams();
            params.topMargin = statusbarHeight;
            return insets.consumeSystemWindowInsets();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }

}
