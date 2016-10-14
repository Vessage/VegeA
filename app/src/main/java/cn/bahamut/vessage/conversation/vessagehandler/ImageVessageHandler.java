package cn.bahamut.vessage.conversation.vessagehandler;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public class ImageVessageHandler extends VessageHandlerBase{
    private View mImageViewContainer;
    private ProgressBar progressBar;
    private TextView dateTextView;
    private ImageView imageView;
    private ImageButton mCenterButton;

    public ImageVessageHandler(ConversationViewPlayManager playVessageManager, ViewGroup vessageContainer) {
        super(playVessageManager,vessageContainer);
        initImageView();
    }

    @Override
    public void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage) {
        super.onPresentingVessageSeted(oldVessage, newVessage);
        if (oldVessage == null || oldVessage.typeId != newVessage.typeId){
            vessageContainer.removeAllViews();
            vessageContainer.addView(mImageViewContainer);
        }
        setVideoPlayerContainerLayoutParams();
        refreshImage();
        updateDateTextView();
    }

    private void refreshImage(){
        progressBar.setVisibility(View.VISIBLE);
        mCenterButton.setVisibility(View.INVISIBLE);
        ImageHelper.setImageByFileIdOnView(imageView,presentingVessage.fileId,R.raw.conversation_bcg_3,onSetImageCompleted);
    }

    private ImageHelper.OnSetImageCallback onSetImageCompleted = new ImageHelper.OnSetImageCallback(){
        @Override
        public void onSetImageFail() {
            super.onSetImageFail();
            progressBar.setVisibility(View.INVISIBLE);
            mCenterButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSetImageSuccess() {
            super.onSetImageSuccess();
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    private void setVideoPlayerContainerLayoutParams() {
        Point size = new Point();
        playVessageManager.getConversationViewActivity().getWindowManager().getDefaultDisplay().getSize(size);
        ViewGroup.LayoutParams params = mImageViewContainer.getLayoutParams();
        params.height = size.y / 2;
        params.width = params.height * 3 / 4;
        mImageViewContainer.setLayoutParams(params);
    }

    private void initImageView() {
        mImageViewContainer = playVessageManager.getConversationViewActivity().getLayoutInflater().inflate(R.layout.image_vessage_container,null);
        dateTextView = (TextView) mImageViewContainer.findViewById(R.id.date_tv);
        imageView = (ImageView)mImageViewContainer.findViewById(R.id.imageView);
        mCenterButton = (ImageButton) mImageViewContainer.findViewById(R.id.center_btn);
        progressBar = (ProgressBar) mImageViewContainer.findViewById(R.id.progress);
        mCenterButton.setOnClickListener(onClickCenterButton);
    }

    private View.OnClickListener onClickCenterButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshImage();
        }
    };

    private void updateDateTextView() {
        if (presentingVessage != null){
            Date sendTime = DateHelper.stringToAccurateDate(presentingVessage.sendTime);
            String friendlyDateString = AppUtil.dateToFriendlyString(playVessageManager.getConversationViewActivity(),sendTime);
            dateTextView.setText(friendlyDateString);
            dateTextView.bringToFront();
        }
    }

}
