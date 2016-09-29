package cn.bahamut.vessage.conversation.vessagehandler;

import android.view.MotionEvent;

/**
 * Created by alexchow on 2016/9/29.
 */

public interface VessageGestureHandler {

    public static class FlingDerection{
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int UP = 2;
        public static final int DOWN = 3;
    }

    void onFling(int direction,float velocityX,float velocityY);
    void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
}
