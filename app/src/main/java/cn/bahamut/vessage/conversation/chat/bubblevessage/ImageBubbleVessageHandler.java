package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

import cn.bahamut.common.BTSize;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.DensityUtil;
import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.common.StringHelper;
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
    private static VessageViewHandlerPool<ImageBubbleVessageHandler> pool = new VessageViewHandlerPool<>();

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        ImageBubbleVessageHandler handler = pool.getHandler(context, vessage);
        if (handler == null) {
            handler = new ImageBubbleVessageHandler();
            pool.registHandler(context, handler);
        }
        return handler;
    }

    @Override
    public BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView) {
        float defaultWidth = DensityUtil.dip2px(context, 180);
        float defaultHeight = DensityUtil.dip2px(context, 180);

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

        imageView = (ImageView) vg.findViewById(R.id.image_view);
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
        pool.recycleHandler(context, this);
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
            if (presentingVessage != null) {
                if (presentingVessage.isMySendingVessage()) {
                    Uri uri = Uri.fromFile(new File(presentingVessage.fileId));
                    new FullScreenImageViewer.Builder(context).setImageUri(uri).show();
                } else if (progressBar.getVisibility() == View.INVISIBLE && centerButton.getVisibility() == View.INVISIBLE && !StringHelper.isStringNullOrWhiteSpace(presentingVessage.fileId)) {
                    new FullScreenImageViewer.Builder(context).setImageFileId(presentingVessage.fileId).show();
                }
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
            Date sendTime = DateHelper.getDateFromUnixTimeSpace(presentingVessage.ts);
            String friendlyDateString = AppUtil.dateToFriendlyString(context,sendTime);
            dateTextView.setText(friendlyDateString);
            dateTextView.bringToFront();
        }
    }
}
