package cn.bahamut.vessage.conversation.chat;

import android.view.View;
import android.view.animation.Animation;

import com.nguyenhoanglam.imagepicker.activity.ImagePicker;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.vessage.R;

/**
 * Created by cplov on 2017/1/25.
 */

public class BottomViewsManager extends ConversationViewManagerBase {

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        initViewsListener();
    }

    private void initViewsListener() {
        findViewById(R.id.new_image_btn).setOnClickListener(onClickSendButtons);
        findViewById(R.id.new_text_btn).setOnClickListener(onClickSendButtons);
    }

    private View.OnClickListener onClickSendButtons = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.new_text_btn:sendNewText(v);break;
                case R.id.new_image_btn:sendNewImage(v);break;
            }
        }
    };

    private void sendNewImage(View v) {
        AnimationHelper.startAnimation(getConversationViewActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                showSelectImageSource();
            }
        });

    }

    private void showSelectImageSource() {
        getConversationViewActivity().sendMoreTypeVessageManager.showVessageTypesHub();
    }

    private void sendNewText(View v) {
        AnimationHelper.startAnimation(getConversationViewActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                getConversationViewActivity().messageInputViewManager.addKeyboardNotification();
                getConversationViewActivity().messageInputViewManager.showImageChatInputView();
            }
        });
    }

}
