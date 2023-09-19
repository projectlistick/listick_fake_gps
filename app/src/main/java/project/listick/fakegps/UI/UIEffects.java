package project.listick.fakegps.UI;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.animation.AnimationUtils;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import project.listick.fakegps.R;

public class UIEffects {
    static class TextView {
        public static void attachErrorWithShake(Context context, android.widget.TextView textView, Runnable afterEffectAction) {

            textView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake));
            textView.setTextColor(context.getColor(R.color.black));
            textView.setBackground(ContextCompat.getDrawable(context, R.drawable.uisearchbar_error));


            CountDownTimer timer = new CountDownTimer(800, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    textView.setBackground(ContextCompat.getDrawable(context, R.drawable.uisearchbar));
                    textView.clearAnimation();
                    textView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.attenuation));
                    textView.setTextColor(context.getColor(R.color.uisearch_textcolor));

                    afterEffectAction.run();
                }
            };
            timer.start();
        }

    }

    static class Button {
        public static void attachErrorWithShake(Context context, MaterialButton button, Runnable afterEffectAction) {

            button.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake));
            button.setTextColor(context.getColor(R.color.black));
            button.setBackgroundColor(context.getColor(R.color.red_tonal_button));


            CountDownTimer timer = new CountDownTimer(800, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    button.setBackgroundColor(context.getColor(R.color.primaryColor));
                    button.clearAnimation();
                    button.startAnimation(AnimationUtils.loadAnimation(context, R.anim.attenuation));
                    button.setTextColor(context.getColor(R.color.white));

                    afterEffectAction.run();
                }
            };
            timer.start();
        }
    }

}
