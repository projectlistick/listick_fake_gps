package project.listick.fakegps.UI;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import project.listick.fakegps.R;

public class PrettyToast {


    public static void show(Activity activity, String text, @DrawableRes int id) {
        View inflatedView = View.inflate(activity, R.layout.pretty_toast, (ViewGroup) activity.getWindow().getDecorView());
        View toast = inflatedView.findViewById(R.id.toast);
        toast.setVisibility(View.VISIBLE);

        TextView textView = inflatedView.findViewById(R.id.text);
        textView.setText(text);

        if (id != -1) {
            ImageView imageView = inflatedView.findViewById(R.id.icon);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(activity.getDrawable(id));
        }

        Animation toastIn = AnimationUtils.loadAnimation(activity, R.anim.toast_in);
        toastIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(() -> {
                    Animation toastOut = AnimationUtils.loadAnimation(activity, R.anim.toast_out);
                    toastOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ((ViewGroup) activity.getWindow().getDecorView()).removeView(inflatedView);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    toast.startAnimation(toastOut);
                }, 3000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        toast.startAnimation(toastIn);
    }

    public static void error(Context context, @StringRes int resId, int duration) {
        Toast toast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        View view = toast.getView();
        TextView view1 = view.findViewById(android.R.id.message);
        view1.setTextColor(context.getColor(R.color.uicolor_text_high));
        view.setBackgroundResource(R.drawable.toast_error);
        toast.show();
    }

    public static void error(Context context, String text, int duration) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        View view = toast.getView();
        TextView view1 = view.findViewById(android.R.id.message);
        view1.setTextColor(context.getColor(R.color.uicolor_text_high));
        view.setBackgroundResource(R.drawable.toast_error);
        toast.show();
    }

}
