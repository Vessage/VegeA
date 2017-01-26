package cn.bahamut.vessage.conversation.chat.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import cn.bahamut.common.BTSize;

/**
 * Created by alexchow on 2016/11/3.
 */

public class BubbleVessageContainer extends ViewGroup {
    private BezierBubbleView bubbleView;
    private View contentView;
    private float contentViewPadding = 40;

    public BubbleVessageContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBubbleView();
    }
    public BubbleVessageContainer(Context context) {
        super(context);
        initBubbleView();
    }

    private void initBubbleView() {
        bubbleView = new BezierBubbleView(getContext());
        this.addView(bubbleView);
        View v = new View(getContext());
        v.setBackgroundColor(Color.BLACK);
        setContentView(v);
    }

    public float getContentViewPadding() {
        return contentViewPadding + getStartMarkMidLine();
    }

    public int getFillColor() {
        return bubbleView.getFillColor();
    }

    public void setFillColor(int fillColor) {
        bubbleView.setFillColor(fillColor);
        forceLayout();
    }

    public float getBubbleCornerRadius() {
        return bubbleView.getBubbleCornerRadius();
    }

    public void setBubbleCornerRadius(float bubbleCornerRadius) {
        bubbleView.setBubbleCornerRadius(bubbleCornerRadius);
        forceLayout();
    }

    public float getStartMarkMidLine() {
        return bubbleView.getStartMarkMidLine();
    }

    public void setStartMarkMidLine(float startMarkMidLine) {
        bubbleView.setStartMarkMidLine(startMarkMidLine);
        this.forceLayout();
    }

    public float getStartRatio() {
        return bubbleView.getStartRatio();
    }

    public void setStartRatio(float startRatio) {
        bubbleView.setStartRatio(startRatio);
        this.forceLayout();
    }

    public BezierBubbleView.BezierBubbleDirection getDirection() {
        return bubbleView.getDirection();
    }

    public void setDirection(BezierBubbleView.BezierBubbleDirection direction) {
        bubbleView.setDirection(direction);
        this.forceLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        bubbleView.measure(widthMeasureSpec,heightMeasureSpec);
        if (contentView != null){
            float h = bubbleView.getMeasuredHeight() - contentViewPadding;
            float w = bubbleView.getMeasuredWidth() - contentViewPadding;
            float sl = bubbleView.getStartMarkMidLine();

            switch (bubbleView.getDirection()){
                case Up:
                case Down:
                    contentView.measure(MeasureSpec.makeMeasureSpec((int)w,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec((int)(h - sl),MeasureSpec.EXACTLY));
                    break;
                case Left:
                case Right:
                    contentView.measure(MeasureSpec.makeMeasureSpec((int)(w - sl),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec((int)h,MeasureSpec.EXACTLY));
                    break;
            }
        }
    }

    public View getContentView() {
        return contentView;
    }

    public void setContentView(View contentView) {

        if (this.contentView != null){
            this.removeView(this.contentView);
        }
        if (contentView != null){
            this.addView(contentView);
        }
        this.contentView = contentView;
        forceLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        bubbleView.layout(l,t,r,b);
        if (contentView != null){
            int h = contentView.getMeasuredHeight();
            int w = contentView.getMeasuredWidth();
            int sl = (int)(bubbleView.getStartMarkMidLine());
            int cl = 0;
            int ct = 0;
            int padding = (int)contentViewPadding / 2;
            switch (bubbleView.getDirection()){
                case Up:
                    cl = padding;
                    ct = padding;
                    break;
                case Down:
                    cl = padding;
                    ct = sl + padding;
                    break;
                case Left:
                    cl = padding;
                    ct = padding;
                    break;
                case Right:
                    cl = sl + padding;
                    ct = padding;
                    break;
            }
            int cr = cl + w;
            int cb = ct + h;
            contentView.layout(cl,ct,cr,cb);
        }
    }

    public BTSize sizeOfContentSize(BTSize contentSize, BezierBubbleView.BezierBubbleDirection direction) {
        switch (direction) {
            case Up:
            case Down:
                return new BTSize(contentSize.width + contentViewPadding, contentSize.height + contentViewPadding + bubbleView.getStartMarkMidLine());
            case Left:
            case Right:
                return new BTSize(contentSize.width + contentViewPadding + bubbleView.getStartMarkMidLine(), contentSize.height + contentViewPadding);
        }
        return BTSize.ZERO;
    }
}
