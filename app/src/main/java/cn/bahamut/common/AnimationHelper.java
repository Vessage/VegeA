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

    private static class DefaultAnimationListener implements Animation.AnimationListener{
        private Animation.AnimationListener listener;
        private View view;

        public DefaultAnimationListener(View view, Animation.AnimationListener animationListener) {
            this.view = view;
            this.listener = animationListener;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (listener != null){
                listener.onAnimationStart(animation);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (listener != null){
                listener.onAnimationEnd(animation);
            }
            if (view != null){
                view.clearAnimation();
            }
            listener = null;
            view = null;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            if (listener != null){
                listener.onAnimationRepeat(animation);
            }
        }
    }

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
        DefaultAnimationListener defaultAnimationListener = new DefaultAnimationListener(view,animationListener);
        a.setAnimationListener(defaultAnimationListener);
        view.clearAnimation();
        view.startAnimation(a);
    }
}
