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
    public static void startAnimation(Context context, View view, int animationResId){
        Animation a = AnimationUtils.loadAnimation(context,animationResId);
        view.setAnimation(a);
        a.start();
    }
}
