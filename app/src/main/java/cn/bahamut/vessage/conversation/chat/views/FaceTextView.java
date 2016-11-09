package cn.bahamut.vessage.conversation.chat.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;

/**
 * Created by alexchow on 16/9/7.
 */
public class FaceTextView extends ViewGroup{
    private static final String TAG = "FaceTextView";
    private ViewGroup container;
    private TextBubbleContainer bubbleViewContainer;
    private ImageView faceImageView;
    private ProgressBar progressBar;
    private Activity context;
    private String faceId;
    private int faceImageState = 0;
    private Point mouthPoint = new Point();

    public FaceTextView(Activity context, ViewGroup container){
        super(context);
        this.context = context;
        this.container = container;
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.face_text_container,null);
        faceImageView = (ImageView)vg.findViewById(R.id.face_image_view);
        progressBar = (ProgressBar) vg.findViewById(R.id.vsg_progress);
        container.addView(vg);
        container.addView(this);
        progressBar.setVisibility(View.INVISIBLE);
        bubbleViewContainer = new TextBubbleContainer(context,this);
    }

    public void scrollBubbleText(int y) {
        bubbleViewContainer.scrollBubbleText(y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
        bubbleViewContainer.measure(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        bubbleViewContainer.layout(l, t, r, b);
        updateBubbleTextContainer();
    }

    public void setFaceText(String faceId, String bubbleText){
        this.faceId = faceId;
        progressBar.setVisibility(View.VISIBLE);
        faceImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bubbleViewContainer.setBubbleText(bubbleText);
        fetchFaceImage();
    }

    public void setFaceTextWithResId(int faceResId,String bubbleText,ImageView.ScaleType imageScaleMode){
        progressBar.setVisibility(View.INVISIBLE);
        faceImageView.setScaleType(imageScaleMode);
        faceImageView.setImageResource(faceResId);
        bubbleViewContainer.setBubbleText(bubbleText);
        faceImageState = 2;
        measure(container.getMeasuredWidth(),container.getMeasuredHeight());
        layout(0,0,container.getMeasuredWidth(),container.getMeasuredHeight());
        setCenterBubble();
        updateBubbleTextContainer();
    }

    private void fetchFaceImage() {
        faceImageState = 0;
        ImageHelper.setImageByFileIdOnView(faceImageView,this.faceId,R.raw.default_face,onSetImageCallback);
    }

    private void setCenterBubble(){
        mouthPoint.set(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
    }

    private Point getCenterPoint(){
        return new Point(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
    }

    private void setDetectFaceMouthBubble() {
        progressBar.setVisibility(View.INVISIBLE);
        faceImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Bitmap bitmap = ((BitmapDrawable)faceImageView.getDrawable()).getBitmap();
        Bitmap bitmapForDetectFaces = bitmap.copy(Bitmap.Config.RGB_565,true);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        FaceDetector faceDetector = new FaceDetector(bitmapForDetectFaces.getWidth(),bitmapForDetectFaces.getHeight(),1);
        faceDetector.findFaces(bitmapForDetectFaces,faces);
        if (faces[0] != null){
            FaceDetector.Face face = faces[0];
            PointF pointF = new PointF();
            face.getMidPoint(pointF);

            pointF.y = pointF.y + face.eyesDistance() * 1.6f;
            mouthPoint.set((int) pointF.x,(int) pointF.y);
        }else {
            setCenterBubble();
        }
    }

    private void updateBubbleTextContainer() {
        //showDebugMouthView(point);
        Point mp = getImageViewPoint(mouthPoint);
        if (faceImageState == 0){
            bubbleViewContainer.setVisibility(INVISIBLE);
            return;
        }else if(faceImageState == 2){
            mp = getCenterPoint();
        }
        bubbleViewContainer.setVisibility(VISIBLE);
        Log.d(TAG,"Mouth Point:" + mouthPoint);
        Point bubbleStartPoint = bubbleViewContainer.getBubbleStartPoint();
        Point movePoint = new Point(mp.x - bubbleStartPoint.x,mp.y - bubbleStartPoint.y );
        FaceTextView.this.removeView(bubbleViewContainer);
        FaceTextView.this.addView(bubbleViewContainer);
        bubbleViewContainer.setX(movePoint.x);
        bubbleViewContainer.setY(movePoint.y);
        forceLayout();
    }

    private Point getImageViewPoint(Point point){
        PointF pointF = new PointF(point.x,point.y);
        return getImageViewPoint(pointF);
    }

    private Point getImageViewPoint(PointF pointF) {
        Bitmap bitmap = ((BitmapDrawable)faceImageView.getDrawable()).getBitmap();
//初始化bitmap的宽高
        int intrinsicHeight = bitmap.getHeight();
        int intrinsicWidth = bitmap.getWidth();

//可见image的宽高
        int scaledHeight = getMeasuredHeight();
        int scaledWidth = getMeasuredWidth();

        float heightRatio = 1.0f * scaledHeight / intrinsicHeight;
        float widthRatio = 1.0f * scaledWidth / intrinsicWidth;
        if(faceImageView.getScaleType().equals(ImageView.ScaleType.CENTER_CROP)) {
            float ratio = Math.max(heightRatio, widthRatio);
            PointF imageCenter = new PointF(intrinsicWidth * ratio / 2f, intrinsicHeight * ratio / 2f);
            PointF preivewCenter = new PointF(scaledWidth / 2, scaledHeight / 2);
            PointF delta = new PointF(preivewCenter.x - imageCenter.x, preivewCenter.y - imageCenter.y);
            //CENTER_CROP
            return new Point((int) (pointF.x * ratio) + (int) delta.x, (int) (pointF.y * ratio) + (int) delta.y);
        }

        //使用fitXY
        return new Point((int)(pointF.x * widthRatio),(int)(pointF.y * heightRatio));
    }

    private ImageHelper.OnSetImageCallback onSetImageCallback = new ImageHelper.OnSetImageCallback(){
        @Override
        public void onSetImageFail() {
            super.onSetImageFail();
            faceImageState = -1;
            setCenterBubble();
            updateBubbleTextContainer();
        }

        @Override
        public void onSetImageSuccess() {
            super.onSetImageSuccess();
            faceImageState = 1;
            setDetectFaceMouthBubble();
            updateBubbleTextContainer();
        }

        /*
        private View mouthMark = null;
        private void showDebugMouthView(Point point) {
            if(AndroidHelper.isApkDebugable(context)) {
                if (mouthMark == null) {
                    mouthMark = new View(context);
                    mouthMark.setBackgroundColor(Color.BLUE);
                }
                container.removeView(mouthMark);
                container.addView(mouthMark);
                ViewHelper.setViewFrame(mouthMark, point.x, point.y, 30, 30);
            }
        }
        */
    };


}
