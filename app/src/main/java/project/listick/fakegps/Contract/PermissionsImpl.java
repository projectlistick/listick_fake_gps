package project.listick.fakegps.Contract;

/*
 * Created by LittleAngry on 27.12.18 (macOS 10.12)
 * */
public interface PermissionsImpl {

    interface Presenter {
        void onActivityLoad();
        void onPermissionRequest();
        void onPermissionGranted();
        void onPermissionDenied();
    }

    interface UI {
        void setButtonError();
    }

}
