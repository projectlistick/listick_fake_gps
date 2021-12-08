package project.listick.fakegps.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import project.listick.fakegps.Contract.BookmarksImpl;
import project.listick.fakegps.Presenter.BookmarksPresenter;
import project.listick.fakegps.R;
import project.listick.fakegps.RouteCoordinateMgr;
public class BookmarksActivity extends FragmentActivity implements BookmarksImpl.UI {

    public static final int STATIC = 0;
    public static final int ROUTE = 1;
    public static final int BOOKMARKS_REQUEST_CODE = 4;

    private BookmarksPresenter mPresenter;
    private ListView mBookmarksList;
    private TextView mBookmarksTab;
    private View mBlankView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        mPresenter = new BookmarksPresenter(this, this);
        mBookmarksList = findViewById(R.id.list);
        mBlankView = findViewById(R.id.blank);
        mBookmarksTab = findViewById(R.id.bookmarks_tab);
        View back = findViewById(R.id.back);

        BottomNavigationView navigationView = findViewById(R.id.navigation);

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.static_spoof:
                    mPresenter.onStaticSpoofList();
                    break;

                case R.id.route_spoof:
                    mPresenter.onRouteSpoofList();
                    break;
            }
            return false;
        });

        mBookmarksList.setOnItemClickListener((parent, view, position, id) -> mPresenter.onItemSelected(position));
        back.setOnClickListener(v -> finish());

        showBlankFragment();
        mPresenter.onActivityLoad();
    }

    @Override
    public void showRouteBookmarks(ArrayList<RouteCoordinateMgr> coordinates, ArrayList<RouteCoordinateMgr.PlaceAddress> addressList, ArrayList<String> routeNames) {
        ArrayAdapter<String> placeNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, routeNames);
        mBookmarksList.setAdapter(placeNamesAdapter);
        showListView();
    }

    @Override
    public void showStaticBookmarks(ArrayList<GeoPoint> coordinates, ArrayList<String> addressList, ArrayList<String> names) {
        ArrayAdapter<String> placeNamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        mBookmarksList.setAdapter(placeNamesAdapter);
        mBookmarksTab.setText(R.string.static_spoof);
        showListView();
    }

    public void setCurrentTab(int tab) {
        if (tab == STATIC) {
            mBookmarksTab.setText(R.string.static_spoof);
            mPresenter.setCurrentTab(STATIC);
        }  else {
            mBookmarksTab.setText(R.string.route_spoof);
            mPresenter.setCurrentTab(ROUTE);
        }
    }

    @Override
    public void showBlankFragment() {
        mBookmarksList.setVisibility(View.GONE);
        mBlankView.setVisibility(View.VISIBLE);
    }

    public void showListView() {
        mBookmarksList.setVisibility(View.VISIBLE);
        mBlankView.setVisibility(View.GONE);
    }

}
