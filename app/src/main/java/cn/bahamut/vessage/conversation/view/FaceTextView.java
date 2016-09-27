package cn.bahamut.vessage.conversation.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;

/**
 * Created by alexchow on 16/9/7.
 */
public class FaceTextView {
    private ViewGroup container;
    private ViewGroup bubbleViewContainer;
    private ImageView faceImageView;
    private ProgressBar progressBar;
    private TextView bubbleTextView;
    private ImageView bubbleImageView;

    private Activity context;

    private String faceId;
    private String bubbleText;

    public FaceTextView(Activity context, ViewGroup container){
        this.context = context;
        this.container = container;
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.face_text_container,null);

        faceImageView = (ImageView)vg.findViewById(R.id.face_image_view);
        progressBar = (ProgressBar) vg.findViewById(R.id.vsg_progress);
        container.addView(vg);
        progressBar.setVisibility(View.INVISIBLE);

        bubbleViewContainer = (ViewGroup) this.context.getLayoutInflater().inflate(R.layout.face_text_bubble_text_view,null);
        bubbleImageView = (ImageView) bubbleViewContainer.findViewById(R.id.bubble_image);
        bubbleTextView = (TextView) bubbleViewContainer.findViewById(R.id.bubble_text);

    }

    public void setFaceText(String faceId,String bubbleText){
        this.faceId = faceId;
        this.bubbleText = bubbleText;
        progressBar.setVisibility(View.VISIBLE);
        container.removeView(bubbleViewContainer);
        ImageHelper.setImageByFileIdOnView(faceImageView,this.faceId,R.raw.default_face,onSetImageCallback);
    }

    private ImageHelper.OnSetImageCallback onSetImageCallback = new ImageHelper.OnSetImageCallback(){
        @Override
        public void onSetImageFail() {
            super.onSetImageFail();
            progressBar.setVisibility(View.INVISIBLE);
            bubbleTextView.setText(bubbleText);
            Bitmap bitmap = ((BitmapDrawable) faceImageView.getDrawable()).getBitmap();
            Point center = new Point();
            center.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            setBubblePosition(getImageViewPoint(faceImageView,center));
        }

        @Override
        public void onSetImageSuccess() {
            super.onSetImageSuccess();
            progressBar.setVisibility(View.INVISIBLE);
            Bitmap bitmap = ((BitmapDrawable)faceImageView.getDrawable()).getBitmap();
            Bitmap bitmapForDetectFaces = bitmap.copy(Bitmap.Config.RGB_565,true);
            FaceDetector.Face[] faces = new FaceDetector.Face[1];
            FaceDetector faceDetector = new FaceDetector(bitmapForDetectFaces.getWidth(),bitmapForDetectFaces.getHeight(),1);
            faceDetector.findFaces(bitmapForDetectFaces,faces);
            if (faces[0] != null){
                FaceDetector.Face face = faces[0];
                PointF pointF = new PointF();
                face.getMidPoint(pointF);
                pointF.y = pointF.y * 1.8f;
                Point point = getImageViewPoint(faceImageView,pointF);
                setBubblePosition(point);
            }else {
                Point center = new Point();
                center.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                setBubblePosition(getImageViewPoint(faceImageView,center));
            }
            bubbleTextView.setText(bubbleText);
        }

        private void setBubblePosition(Point point) {
            float w = container.getLayoutParams().width;
            float h = container.getLayoutParams().height;
            Point bubbleStartPoint = new Point((int) (w * 6 / 12),(int)(h / 6));
            Point movePoint = new Point(point.x - bubbleStartPoint.x,point.y - bubbleStartPoint.y );
            bubbleViewContainer.setLayoutParams(container.getLayoutParams());
            bubbleViewContainer.setX(movePoint.x);
            bubbleViewContainer.setY(movePoint.y);
            container.addView(bubbleViewContainer);
        }

        private Point getImageViewPoint(ImageView imageView, Point point){
            PointF pointF = new PointF(point.x,point.y);
            return getImageViewPoint(imageView,pointF);
        }

        private Point getImageViewPoint(ImageView imageView, PointF pointF) {
            Drawable drawable = imageView.getDrawable();
            Bitmap bitmap = ((BitmapDrawable)faceImageView.getDrawable()).getBitmap();

            Rect imageBounds = drawable.getBounds();

//初始化bitmap的宽高
            int intrinsicHeight = bitmap.getHeight();
            int intrinsicWidth = bitmap.getWidth();

//可见image的宽高
            int scaledHeight = container.getLayoutParams().height;
            int scaledWidth = container.getLayoutParams().width;

//使用fitXY
            float heightRatio = 1.0f * scaledHeight / intrinsicHeight;
            float widthRatio = 1.0f * scaledWidth / intrinsicWidth;

            return new Point((int)(pointF.x * widthRatio),(int)(pointF.y * heightRatio));
        }
    };
}
