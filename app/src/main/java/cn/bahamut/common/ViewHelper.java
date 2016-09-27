package cn.bahamut.common;

import android.view.View;

/**
 * Created by alexchow on 2016/9/24.
 */
public class ViewHelper {
    public static void setViewFrame(View view, double x, double y, double width, double height) {
        view.setX((int)x);
        view.setY((int)y);
        view.getLayoutParams().height = (int)height;
        view.getLayoutParams().width = (int)width;
    }
}
