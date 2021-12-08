package project.listick.fakegps;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import project.listick.fakegps.Interfaces.IJoystickListener;

public class JoystickControl implements View.OnTouchListener {

    public static final String JOYSTICK_PREFERENCES = "joystick_preferences";
    public static final String JOYSTICK_MAX_SPEED = "joystick_max_speed";

    private int containerRadius;
    private int viewRadius;

    private int centerX;
    private int centerY;
    private int angle;
    private int strength;
    private int direction;


    private IJoystickListener mListener;
    private View container;
    private Handler handler;


    private boolean fingerHold;

    JoystickControl(){
        this.handler = new Handler();
        Thread joystickThread = new Thread(runnable);
        joystickThread.start();
    }

    void setContainer(View view){
        this.container = view;
    }

    void setOnMoveListener(IJoystickListener listener){
        this.mListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int containerMax;

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                centerX = (int) v.getX();
                centerY = (int) v.getY();

                containerRadius = container.getWidth() / 2;
                viewRadius = v.getWidth() / 2;
                centerX = (container.getWidth() - v.getWidth()) / 2;
                centerY = (container.getHeight() - v.getHeight()) / 2;

                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100);
                break;
            case MotionEvent.ACTION_UP:
                v.animate().x(centerX).y(centerY).setDuration(100).start();
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                fingerHold = false;
                break;

            case MotionEvent.ACTION_MOVE:
                int x = (int) (v.getX() + event.getX() - (v.getWidth() / 2));
                int y = (int) (v.getY() + event.getY() - (v.getWidth() / 2));

                double abs = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));

                containerMax = (containerRadius - viewRadius);

                if (abs > containerMax) {
                    x = (int) ((x - centerX) * containerMax / abs + centerX);
                    y = (int) ((y - centerY) * containerMax / abs + centerY);
                }

                v.animate().x(x).y(y).setDuration(0).start();

                 angle = calculateAngle(x, centerX, y, centerY);
                 strength = calculateStrength(abs, containerMax);
                 direction = 0;

                 fingerHold = true;
                 break;
        }
        v.performClick();
        return true;
    }

    private int calculateStrength(double abs, int containerMax){
        return (abs <= containerMax) ? (int) ((100 * abs) / containerMax) : 100;
    }

    private int calculateAngle(int x, int centerX, int y, int centerY){
        int dX = x - centerX;
        int dY = y - centerY;

        return (int) Math.toDegrees(Math.atan2(dY, dX));
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (fingerHold)
                mListener.onJoystickMoved(direction, strength, angle);

            handler.postDelayed(this, 20);
        }
    };

}
