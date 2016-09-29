package cn.bahamut.vessage.conversation.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.ViewHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;

/**
 * Created by alexchow on 16/9/7.
 */
public class FaceTextView {
    private ViewGroup container;
    private TextBubbleContainer bubbleViewContainer;
    private ImageView faceImageView;
    private ProgressBar progressBar;
    private Activity context;
    private String faceId;

    public FaceTextView(Activity context, ViewGroup container){
        this.context = context;
        this.container = container;
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.face_text_container,null);
        faceImageView = (ImageView)vg.findViewById(R.id.face_image_view);
        progressBar = (ProgressBar) vg.findViewById(R.id.vsg_progress);
        container.addView(vg);
        progressBar.setVisibility(View.INVISIBLE);
        bubbleViewContainer = new TextBubbleContainer(context,container);
    }

    public void scrollBubbleText(int y) {
        bubbleViewContainer.scrollBubbleText(y);
    }

    public void setFaceText(String faceId,String bubbleText){
        this.faceId = faceId;
        bubbleViewContainer.setBubbleText(bubbleText);
        progressBar.setVisibility(View.VISIBLE);
        container.removeView(bubbleViewContainer);
        ImageHelper.setImageByFileIdOnView(faceImageView,this.faceId,R.raw.default_face,onSetImageCallback);
    }

    private ImageHelper.OnSetImageCallback onSetImageCallback = new ImageHelper.OnSetImageCallback(){
        @Override
        public void onSetImageFail() {
            super.onSetImageFail();
            bubbleViewContainer.setVisibility(View.VISIBLE);
            Bitmap bitmap = ((BitmapDrawable) faceImageView.getDrawable()).getBitmap();
            Point center = new Point();
            center.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            setBubblePosition(getImageViewPoint(center));
        }

        @Override
        public void onSetImageSuccess() {
            super.onSetImageSuccess();
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
                Point point = getImageViewPoint(pointF);
                setBubblePosition(point);
            }else {
                Point center = new Point();
                center.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                setBubblePosition(getImageViewPoint(center));
            }
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

        private void setBubblePosition(Point point) {

            //showDebugMouthView(point);

            Point bubbleStartPoint = bubbleViewContainer.getBubbleStartPoint();
            Point movePoint = new Point(point.x - bubbleStartPoint.x,point.y - bubbleStartPoint.y );
            bubbleViewContainer.setX(movePoint.x);
            bubbleViewContainer.setY(movePoint.y);
            container.addView(bubbleViewContainer);
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
            int scaledHeight = container.getLayoutParams().height;
            int scaledWidth = container.getLayoutParams().width;

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
    };


}
