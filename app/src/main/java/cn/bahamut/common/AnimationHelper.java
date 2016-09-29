package cn.bahamut.common;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

/**
 * Created by alexchow on 2016/9/28.
 */

public class AnimationHelper {

    public static class AnimationListenerAdapter implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    public static void startAnimation(Context context, View view, int animationResId) {
        startAnimation(context, view, animationResId, null);
    }

    public static void startAnimation(Context context, View view, int animationResId, Animation.AnimationListener animationListener){
        Animation a = AnimationUtils.loadAnimation(context,animationResId);
        if(animationListener != null){
            a.setAnimationListener(animationListener);
        }
        view.clearAnimation();
        view.startAnimation(a);
    }
}
