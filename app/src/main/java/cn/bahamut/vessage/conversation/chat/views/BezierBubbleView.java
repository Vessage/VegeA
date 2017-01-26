package cn.bahamut.vessage.conversation.chat.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by alexchow on 2016/11/3.
 */

public class BezierBubbleView extends View {


    public float getAbsoluteStartMarkPoint() {
        return absoluteStartMarkPoint;
    }

    public void setAbsoluteStartMarkPoint(float absoluteStartMarkPoint) {
        this.absoluteStartMarkPoint = absoluteStartMarkPoint;
    }

    public enum BezierBubbleDirection {
        Up, Down, Left, Right
    }

    private final Paint mGesturePaint = new Paint();
    private final Path mBubblePath = new Path();
    private final Path mStartMarkPath = new Path();
    private float startRatio = 0.1f;
    private float absoluteStartMarkPoint = -1f;
    private BezierBubbleDirection direction = BezierBubbleDirection.Up;
    private float startMarkMidLine = 30f;
    private float bubbleCornerRadius = 16f;

    public BezierBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public BezierBubbleView(Context context) {
        super(context);
        initPaint();
    }

    private void initPaint() {
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Paint.Style.STROKE);
        mGesturePaint.setStrokeWidth(0);
        mGesturePaint.setColor(Color.BLUE);
        mGesturePaint.setStyle(Paint.Style.FILL);
        mBubblePath.setFillType(Path.FillType.WINDING);
        mStartMarkPath.setFillType(Path.FillType.WINDING);
    }

    public int getFillColor() {
        return mGesturePaint.getColor();
    }

    public void setFillColor(int fillColor) {
        mGesturePaint.setColor(fillColor);
        forceLayout();
    }

    public float getBubbleCornerRadius() {
        return bubbleCornerRadius;
    }

    public void setBubbleCornerRadius(float bubbleCornerRadius) {
        this.bubbleCornerRadius = bubbleCornerRadius;
        forceLayout();
    }

    public float getStartMarkMidLine() {
        return startMarkMidLine;
    }

    public void setStartMarkMidLine(float startMarkMidLine) {
        this.startMarkMidLine = startMarkMidLine;
        this.forceLayout();
    }

    public float getStartRatio() {
        return startRatio;
    }

    public void setStartRatio(float startRatio) {
        this.startRatio = startRatio;
        this.forceLayout();
    }

    public BezierBubbleDirection getDirection() {
        return direction;
    }

    public void setDirection(BezierBubbleDirection direction) {
        this.direction = direction;
        this.forceLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBubblePath.reset();
        mStartMarkPath.reset();

        float ratio = 0;
        switch (direction) {
            case Up:
                ratio = absoluteStartMarkPoint > 0 ? absoluteStartMarkPoint / getWidth() : startRatio;
                drawBezierUpBubble((float) getWidth(),(float) getHeight(),ratio,mBubblePath,mStartMarkPath);
                break;
            case Down:
                ratio = absoluteStartMarkPoint > 0 ? absoluteStartMarkPoint / getWidth() : startRatio;
                drawBezierDownBubble((float) getWidth(),(float) getHeight(),ratio,mBubblePath,mStartMarkPath);
                break;
            case Left:
                ratio = absoluteStartMarkPoint > 0 ? absoluteStartMarkPoint / getHeight() : startRatio;
                drawBezierLeftBubble((float) getWidth(),(float) getHeight(),ratio,mBubblePath,mStartMarkPath);
                break;
            case Right:
                ratio = absoluteStartMarkPoint > 0 ? absoluteStartMarkPoint / getHeight() : startRatio;
                drawBezierRightBubble((float) getWidth(),(float) getHeight(),ratio,mBubblePath,mStartMarkPath);
                break;
        }
        canvas.drawPath(mStartMarkPath, mGesturePaint);
        canvas.drawPath(mBubblePath, mGesturePaint);
    }

    static public PointF getStartPointWith(float width, float height, float startRatio,BezierBubbleDirection direction){
        switch (direction) {
            case Up:return new PointF(width * startRatio, height);
            case Down:return new PointF(width * startRatio, 0);
            case Left:return new PointF(width, height * startRatio);
            case Right:return new PointF(0, height * startRatio);
        }
        return new PointF(width * startRatio, height);
    }

    private PointF drawBezierRightBubble(float width, float height, float startYRatio, Path bubblePath, Path startMarkPath) {

        RectF bubbleRect = new RectF(startMarkMidLine, 0, width, height);
        bubblePath.addRoundRect(bubbleRect, bubbleCornerRadius, bubbleCornerRadius, Path.Direction.CCW);

        PointF startPoint = getStartPointWith(width,height,startYRatio,direction);
        startMarkPath.moveTo(startPoint.x, startPoint.y);
        startMarkPath.lineTo(startMarkMidLine, height * startYRatio - startMarkMidLine / 2);
        startMarkPath.lineTo(startMarkMidLine, height * startYRatio + startMarkMidLine / 2);
        startMarkPath.close();

        return startPoint;
    }

    private PointF drawBezierLeftBubble(float width, float height, float startYRatio, Path bubblePath, Path startMarkPath) {
        RectF bubbleRect = new RectF(0, 0, width - startMarkMidLine, height);
        bubblePath.addRoundRect(bubbleRect, bubbleCornerRadius, bubbleCornerRadius, Path.Direction.CCW);
        PointF startPoint = getStartPointWith(width,height,startYRatio,direction);
        startMarkPath.moveTo(startPoint.x, startPoint.y);
        startMarkPath.lineTo(width - startMarkMidLine, height * startYRatio - startMarkMidLine / 2);
        startMarkPath.lineTo(width - startMarkMidLine, height * startYRatio + startMarkMidLine / 2);
        startMarkPath.close();

        return startPoint;
    }

    private PointF drawBezierUpBubble(float width, float height, float startXRatio, Path bubblePath, Path startMarkPath) {
        RectF bubbleRect = new RectF(0, 0, width, height - startMarkMidLine);
        bubblePath.addRoundRect(bubbleRect, bubbleCornerRadius, bubbleCornerRadius, Path.Direction.CCW);
        PointF startPoint = getStartPointWith(width,height,startXRatio,direction);
        startMarkPath.moveTo(startPoint.x, startPoint.y);
        startMarkPath.lineTo(width * startXRatio - startMarkMidLine / 2, height - startMarkMidLine);
        startMarkPath.lineTo(width * startXRatio + startMarkMidLine / 2, height - startMarkMidLine);
        startMarkPath.close();
        return startPoint;
    }

    private PointF drawBezierDownBubble(float width, float height, float startXRatio, Path bubblePath, Path startMarkPath) {

        RectF bubbleRect = new RectF(0, startMarkMidLine, width, height);
        bubblePath.addRoundRect(bubbleRect, bubbleCornerRadius, bubbleCornerRadius, Path.Direction.CCW);
        PointF startPoint = getStartPointWith(width,height,startXRatio,direction);
        startMarkPath.moveTo(startPoint.x, startPoint.y);
        startMarkPath.lineTo(width * startXRatio - startMarkMidLine / 2, startMarkMidLine);
        startMarkPath.lineTo(width * startXRatio + startMarkMidLine / 2, startMarkMidLine);
        startMarkPath.close();

        return startPoint;
    }
}
