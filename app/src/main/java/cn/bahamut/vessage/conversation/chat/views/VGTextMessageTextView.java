package cn.bahamut.vessage.conversation.chat.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by cplov on 2017/1/26.
 */

public class VGTextMessageTextView extends TextView {
    public VGTextMessageTextView(Context context) {
        super(context);
    }

    public VGTextMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VGTextMessageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VGTextMessageTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        TextView tv = this;
        int h = View.MeasureSpec.getSize(tv.getMeasuredHeight());
        if (h <= tv.getMinHeight()){
            tv.setGravity(Gravity.CENTER);
        }else {
            tv.setGravity(Gravity.LEFT);
        }
    }
}
