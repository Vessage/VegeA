package cn.bahamut.common;

import android.app.Activity;
import android.view.WindowManager;

/**
 * Created by alexchow on 2016/10/2.
 */
public class ActivityHelper {
    public static void fullScreen(Activity activity, boolean enable) {
        WindowManager.LayoutParams p = activity.getWindow().getAttributes();
        if (enable) {

            p.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;//|=：或等于，取其一

        } else {
            p.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);//&=：与等于，取其二同时满足，     ~ ： 取反

        }
        activity.getWindow().setAttributes(p);
    }
}
