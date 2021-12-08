package project.listick.fakegps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import project.listick.fakegps.Interfaces.IJoystickListener;

import static android.content.Context.WINDOW_SERVICE;

public class JoystickOverlay {

    public static final int ACTION_OVERLAYS_PERMISSION = 3;
    private static final String SCREEN_X = "screen_x";
    private static final String SCREEN_Y = "screen_y";


    private final IJoystickListener mListener;
    private final Context context;

    private WindowManager mWindowManager;
    private View mFloatingView;

    public JoystickOverlay(Context context, IJoystickListener listener){
        this.context = context;
        this.mListener = listener;
    }



    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    public void drawOverlay(){
        mFloatingView = LayoutInflater.from(context).inflate(R.layout.joystick_view, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }


        mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (mWindowManager == null) {
            Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_LONG).show();
            return;
        }
        mWindowManager.addView(mFloatingView, params);

        View view = mFloatingView.findViewById(R.id.collapsed_iv);
        ImageView moveJoystick = mFloatingView.findViewById(R.id.move_joystick);
        final RelativeLayout container = mFloatingView.findViewById(R.id.container);

        JoystickControl joystickControl = new JoystickControl();
        view.setOnTouchListener(joystickControl);
        joystickControl.setContainer(container);
        joystickControl.setOnMoveListener(mListener);

        final SharedPreferences joystickPrefs = context.getSharedPreferences(JoystickControl.JOYSTICK_PREFERENCES, Context.MODE_PRIVATE);

        int joystickX = joystickPrefs.getInt(SCREEN_X, params.x);
        int joystickY = joystickPrefs.getInt(SCREEN_Y, params.y);

        params.x = joystickX;
        params.y = joystickY;

        moveJoystick.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        joystickPrefs.edit().putInt(SCREEN_X, params.x)
                            .putInt(SCREEN_Y, params.y)
                            .apply();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
        mWindowManager.updateViewLayout(mFloatingView, params);

    }

    public void removeViews(){
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

}
