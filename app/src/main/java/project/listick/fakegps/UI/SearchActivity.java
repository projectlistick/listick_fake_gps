package project.listick.fakegps.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import project.listick.fakegps.Contract.SearchImpl;
import project.listick.fakegps.Enumerations.ERouteTransport;
import project.listick.fakegps.ListickApp;
import project.listick.fakegps.OnSingleClickListener;
import project.listick.fakegps.Presenter.SearchPresenter;
import project.listick.fakegps.R;
import project.listick.fakegps.SpoofingPlaceInfo;

/*
 * Created by LittleAngry on 13.01.19 (macOS 10.12)
 * */
public class SearchActivity extends Activity implements SearchImpl.UI {

    public static final String ADD_MORE_ROUTE = "add_more_route";
    public static final int ACTIVITY_REQUEST_CODE = 1;

    private EditText origin;
    private EditText destination;

    private SearchPresenter presenter;

    private ImageView car;
    private ImageView bike;
    private ImageView walk;

    @Override
    public void onBackPressed() {
        setReturnSharedElementTransition();
        super.onBackPressed();
    }

    public static void startActivity(Activity activity, String originAddress, double latitude, double longitude, boolean addRoute, Bundle options) {
        activity.startActivityForResult(new Intent(activity, SearchActivity.class).putExtra(SpoofingPlaceInfo.ORIGIN_ADDRESS, originAddress)
                .putExtra(ListickApp.LATITUDE, latitude)
                .putExtra(ListickApp.LONGITUDE, longitude)
                .putExtra(ADD_MORE_ROUTE, addRoute), ACTIVITY_REQUEST_CODE, options);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        presenter = new SearchPresenter(this);

        origin = findViewById(R.id.origin);
        destination = findViewById(R.id.destination);
        car = findViewById(R.id.transport_car);
        bike = findViewById(R.id.transport_bike);
        walk = findViewById(R.id.transport_walk);

        RelativeLayout selectOnMap = findViewById(R.id.select_on_map);
        Button next = findViewById(R.id.next_action);

        car.setOnClickListener(v -> presenter.onTransport(ERouteTransport.ROUTE_CAR));
        bike.setOnClickListener(v -> presenter.onTransport(ERouteTransport.ROUTE_BIKE));
        walk.setOnClickListener(v -> presenter.onTransport(ERouteTransport.ROUTE_WALK));

        destination.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                presenter.onDestination();
            }
            return true;
        });

        origin.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                presenter.onOrigin();
            }
            return false;
        });

        next.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                setReturnSharedElementTransition();
                presenter.onContinue();
            }
        });

        selectOnMap.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                presenter.selectOnMap();
            }
        });

        LinearLayout back = findViewById(R.id.back);

        back.setOnClickListener(view -> {
            setReturnSharedElementTransition();
            finishAfterTransition();
        });

        presenter.onActivityLoad();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setOriginAddress(String address) {
        if (address != null && !address.isEmpty())
            origin.setText(address);
    }

    @Override
    public void setDestAddress(String address) {
        if (address != null && !address.isEmpty())
            destination.setText(address);
    }

    @Override
    public void setTransport(ERouteTransport transport) {
        if (transport == ERouteTransport.ROUTE_CAR)
            car.setBackground(getDrawable(R.drawable.transportview_pressed));
        if (transport == ERouteTransport.ROUTE_BIKE)
            bike.setBackground(getDrawable(R.drawable.transportview_pressed));
        if (transport == ERouteTransport.ROUTE_WALK)
            walk.setBackground(getDrawable(R.drawable.transportview_pressed));

    }

    @Override
    public void removeTransport(ERouteTransport transport) {
        car.setBackground(null);
        bike.setBackground(null);
        walk.setBackground(null);
    }

    public void setReturnSharedElementTransition() {
        Transition t = getWindow().getSharedElementReturnTransition();
        t = t.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                RelativeLayout sharedElement = (RelativeLayout) findViewById(R.id.home);
                sharedElement.setBackground(getDrawable(R.drawable.rounded_corner_square));
                sharedElement.removeAllViews();
                sharedElement.setAlpha(0.5f);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
        getWindow().setSharedElementExitTransition(t);
    }

}
