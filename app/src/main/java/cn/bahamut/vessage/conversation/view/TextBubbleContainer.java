package cn.bahamut.vessage.conversation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 2016/9/29.
 */

public class TextBubbleContainer extends ViewGroup {
    private static String TAG = "TextBubbleContainer";
    private View container;

    private TextView bubbleTextView;
    private ScrollView scrollView;
    private ImageView bubbleImageView;
    private Bitmap bubbleImage;

    //private String bubbleText;

    private int bubbleTextChanged = 0;

    private float bubbleTextSize = 14;

    public TextBubbleContainer(Context context, View container){
        super(context);
        this.container = container;
        bubbleImageView = new ImageView(context);
        scrollView = new ScrollView(context);
        bubbleTextView = new TextView(context);
        bubbleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,bubbleTextSize);
        bubbleTextView.setTextColor(Color.BLACK);
        bubbleTextView.setGravity(Gravity.CENTER);
        this.addView(bubbleImageView);
        this.addView(scrollView);
        scrollView.setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
        scrollView.addView(bubbleTextView);
        bubbleImage = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.cloud_bubble));
        bubbleImageView.setImageBitmap(bubbleImage);

        /* Debug Background
        bubbleImageView.setBackgroundColor(Color.parseColor("#eeff0000"));
        scrollView.setBackgroundColor(Color.parseColor("#ee00ff00"));
        bubbleTextView.setBackgroundColor(Color.parseColor("#ee00ffff"));
        */
    }

    public void scrollBubbleText(int y){
        scrollView.scrollTo(0,scrollView.getScrollY() - y / 3);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureViewSize();
        setMeasuredValues();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "############### onLayout ###################");
        bubbleImageView.layout(0,0,bubbleImageView.getMeasuredWidth(),bubbleImageView.getMeasuredHeight());

        Log.d(TAG, "bubbleImageView:" + bubbleImageView.getMeasuredWidth() +"," + bubbleImageView.getMeasuredHeight());

        scrollView.layout(scrollViewFinalPos.x,scrollViewFinalPos.y,scrollViewFinalPos.x + scrollView.getMeasuredWidth(),scrollViewFinalPos.y + scrollView.getMeasuredHeight());

        Log.d(TAG,String.format("scrollView x:%d,y:%d -> w:%d,h%d",scrollViewFinalPos.x,scrollViewFinalPos.y,scrollView.getMeasuredWidth(),scrollView.getMeasuredHeight()));

        int bubbleTextViewY =  (scrollView.getMeasuredHeight() - bubbleTextView.getMeasuredHeight()) / 2;
        if (bubbleTextViewY < 0){
            bubbleTextViewY = 0;
        }
        bubbleTextView.layout(0,bubbleTextViewY,bubbleTextView.getMeasuredWidth(),bubbleTextViewY + bubbleTextView.getMeasuredHeight());

        Log.d(TAG, "bubbleTextView:" + bubbleTextView.getMeasuredWidth() +"," + bubbleTextView.getMeasuredHeight());

        Log.d(TAG, "############### onLayout ###################");
    }

    public void setBubbleText(String bubbleText) {
        bubbleTextChanged = 0;
        bubbleTextView.setText(bubbleText);
    }

    public Point getBubbleStartPoint() {
        return new Point(bubbleStartPoint);
    }

    static private PointF bubbleOriginSize = new PointF(793,569);
    static private PointF bubbleStartPointRatio = new PointF(420 / bubbleOriginSize.x,0);
    static private Rect scrollViewOriginRect = new Rect(156,156,630,470);
    static private float bubbleTextViewRatio = 1f * scrollViewOriginRect.height() / scrollViewOriginRect.width();
    static private float bubbleMinRadio = 0.3f;
    static private float bubbleMaxRadio = 0.6f;

    private Point scrollViewFinalPos = new Point();
    private float textViewFinalWidth = 0;
    private float textViewFinalHeight = 0;
    private float scrollViewFinalHeight = 0;
    private float finalRatio = bubbleMinRadio;
    private float finalImageViewWidth = 0;
    private float finalImageViewHeight = 0;
    private Point bubbleStartPoint = new Point();

    private void setMeasuredValues(){
        bubbleTextView.measure(MeasureSpec.makeMeasureSpec((int) textViewFinalWidth,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) textViewFinalHeight,MeasureSpec.EXACTLY));
        scrollView.measure(MeasureSpec.makeMeasureSpec((int) textViewFinalWidth,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) scrollViewFinalHeight,MeasureSpec.EXACTLY));
        bubbleImageView.measure(MeasureSpec.makeMeasureSpec((int) finalImageViewWidth,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) finalImageViewHeight,MeasureSpec.EXACTLY));
        setMeasuredDimension(MeasureSpec.makeMeasureSpec((int) finalImageViewWidth,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) finalImageViewHeight,MeasureSpec.EXACTLY));
    }

    private void measureViewSize() {
        if (bubbleTextChanged >= 1){
            return;
        }
        Log.d(TAG, "------------------Start Measure------------------------");
        int containerWidth = container.getMeasuredWidth();
        Log.d(TAG, "containerWidth:" + containerWidth);
        if (containerWidth <= 0){
            Log.d(TAG, "------------------End Measure Container Is Not Measure------------------------");
            return;
        }
        float widthRadio = bubbleMinRadio;
        TextView tv = bubbleTextView;
        CharSequence bubbleText = tv.getText();
        for (; widthRadio <= bubbleMaxRadio; widthRadio += 0.01f) {
            textViewFinalWidth = (int) (containerWidth * widthRadio);
            textViewFinalHeight = (int) (textViewFinalWidth * bubbleTextViewRatio);

            StaticLayout sl = new StaticLayout(bubbleText,tv.getPaint(),(int) textViewFinalWidth, Layout.Alignment.ALIGN_CENTER,0.1f,0.1f,false);
            Log.d(TAG, "StaticLayoutHeight:" + sl.getHeight());
            Log.d(TAG, "StaticLayoutLines:" + sl.getLineCount());

            float measuredHeight = sl.getHeight() * sl.getLineCount();
            scrollViewFinalHeight = textViewFinalHeight;

            Log.d(TAG, "radio:" + widthRadio);
            Log.d(TAG, "measuredHeight:" + measuredHeight);
            Log.d(TAG, "scrollViewFinalHeight:" + scrollViewFinalHeight);
            Log.d(TAG, "textViewFinalWidth:" + textViewFinalWidth);

            if (measuredHeight <= textViewFinalHeight) {
                Log.d(TAG, "textViewFinalHeight:" + textViewFinalHeight);
                break;
            } else if (widthRadio == bubbleMaxRadio) {
                textViewFinalHeight = measuredHeight;
            }
            Log.d(TAG, "textViewFinalHeight:" + textViewFinalHeight);
        }

        Log.d(TAG, "Select Radio:" + widthRadio);
        finalRatio = textViewFinalWidth / scrollViewOriginRect.width();
        finalImageViewWidth = bubbleOriginSize.x * finalRatio;
        finalImageViewHeight = finalImageViewWidth * bubbleTextViewRatio;
        Log.d(TAG, "finalImageViewWidth:" + finalImageViewWidth);
        Log.d(TAG, "finalImageViewHeight:" + finalImageViewHeight);
        scrollViewFinalPos.set((int) (scrollViewOriginRect.left * finalRatio), (int) (scrollViewOriginRect.top * finalRatio));
        Log.d(TAG, "scrollViewFinalPos:" + scrollViewFinalPos.toString());
        bubbleStartPoint.set((int) (finalImageViewWidth * bubbleStartPointRatio.x), (int) (finalImageViewHeight * bubbleStartPointRatio.y));
        Log.d(TAG, "bubbleStartPoint:" + bubbleStartPoint.toString());
        Log.d(TAG, "------------------End Measure------------------------");
        bubbleTextChanged++;
    }
}
