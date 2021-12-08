package project.listick.fakegps.Contract;

import android.content.Intent;

import project.listick.fakegps.Enumerations.ERouteTransport;

/*
 * Created by LittleAngry on 13.01.19 (macOS 10.12)
 * */
public interface SearchImpl {

    interface Presenter {
        void onActivityLoad();
        void onDestination();
        void onOrigin();
        void onContinue();
        void onActivityResult(int requestCode, int resultCode, Intent data);
        void selectOnMap();
        void onTransport(ERouteTransport transport);
    }

    interface UI {
        void setOriginAddress(String address);
        void setDestAddress(String address);
        void setTransport(ERouteTransport transport);
        void removeTransport(ERouteTransport transport);
    }
}
