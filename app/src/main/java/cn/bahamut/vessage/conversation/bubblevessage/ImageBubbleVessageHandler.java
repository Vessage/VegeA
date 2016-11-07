package cn.bahamut.vessage.conversation.bubblevessage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import cn.bahamut.common.BTSize;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.DensityUtil;
import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 2016/11/4.
 */

public class ImageBubbleVessageHandler implements BubbleVessageHandler {
    private ImageView imageView;
    private View centerButton;
    private View progressBar;
    private Vessage presentingVessage;
    private Activity context;
    private TextView dateTextView;

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        return new ImageBubbleVessageHandler();
    }

    @Override
    public BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView) {
        float defaultWidth = DensityUtil.dip2px(context, 240);
        float defaultHeight = DensityUtil.dip2px(context, 240);

        if (maxLimitedSize.width >= defaultWidth && maxLimitedSize.height >= defaultHeight) {
            return new BTSize(defaultWidth, defaultHeight);
        } else if (maxLimitedSize.height > maxLimitedSize.width) {
            return new BTSize(maxLimitedSize.width, maxLimitedSize.width * defaultHeight / defaultWidth);
        } else if (maxLimitedSize.width > maxLimitedSize.height) {
            return new BTSize(maxLimitedSize.height * defaultWidth / defaultHeight, maxLimitedSize.height);
        }
        return BTSize.ZERO;
    }

    @Override
    public ViewGroup getContentView(final Activity context, Vessage vessage) {
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.vessage_content_image,null);

        imageView = (ImageView) vg.findViewById(R.id.imageView);
        centerButton = vg.findViewById(R.id.center_btn);
        progressBar = vg.findViewById(R.id.progress);
        dateTextView = (TextView) vg.findViewById(R.id.date_tv);
        centerButton.setOnClickListener(onClickCenterButton);
        imageView.setOnClickListener(onClickImageView);

        return vg;
    }

    @Override
    public void presentContent(Activity context, Vessage oldVessage, Vessage newVessage, View contentView) {
        this.presentingVessage = newVessage;
        this.context = context;
        refreshImage();
        updateDateTextView();
    }

    @Override
    public void onUnloadVessage(Activity context) {
        this.imageView = null;
        this.centerButton = null;
        this.progressBar = null;
        this.presentingVessage = null;
        this.context = null;
        this.dateTextView = null;
    }

    @Override
    public void onPrepareVessage(Activity context, Vessage vessage) {

    }

    private void refreshImage(){
        if (presentingVessage.isMySendingVessage()){
            Drawable drawable = Drawable.createFromPath(presentingVessage.fileId);
            imageView.setImageDrawable(drawable);
            progressBar.setVisibility(View.INVISIBLE);
            centerButton.setVisibility(View.INVISIBLE);
        }else {
            progressBar.setVisibility(View.VISIBLE);
            centerButton.setVisibility(View.INVISIBLE);
            ImageHelper.setImageByFileIdOnView(imageView,presentingVessage.fileId,R.raw.conversation_bcg_3,onSetImageCompleted);
        }
    }

    private ImageHelper.OnSetImageCallback onSetImageCompleted = new ImageHelper.OnSetImageCallback(){
        @Override
        public void onSetImageFail() {
            super.onSetImageFail();
            progressBar.setVisibility(View.INVISIBLE);
            centerButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSetImageSuccess() {
            super.onSetImageSuccess();
            progressBar.setVisibility(View.INVISIBLE);
            ServicesProvider.getService(VessageService.class).readVessage(presentingVessage);
        }
    };

    private View.OnClickListener onClickImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (progressBar.getVisibility() == View.INVISIBLE && centerButton.getVisibility() ==View.INVISIBLE){
                Intent intent = new Intent(context, FullScreenImageViewer.class);
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                byte[] bytes = ImageHelper.bitmap2Bytes(bitmap);
                intent.putExtra("data",bytes);
                context.startActivity(intent);
            }
        }
    };

    private View.OnClickListener onClickCenterButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshImage();
        }
    };

    private void updateDateTextView() {
        if (presentingVessage != null){
            Date sendTime = DateHelper.stringToAccurateDate(presentingVessage.sendTime);
            String friendlyDateString = AppUtil.dateToFriendlyString(context,sendTime);
            dateTextView.setText(friendlyDateString);
            dateTextView.bringToFront();
        }
    }
}
