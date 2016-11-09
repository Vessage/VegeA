package cn.bahamut.vessage.conversation.chat;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.SoftKeyboardStateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.chat.views.ChattersBoard;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/9/26.
 */

public class SendImageChatMessageManager {


    private ConversationViewActivity activity;
    private ViewGroup mImageChatInputView;
    private ViewGroup mImageChatInputViewContainer;
    private EditText mMessageEditText;
    private ProgressBar mSendingProgress;

    public SendImageChatMessageManager(ConversationViewActivity activity){
        this.activity = activity;
        initImageChatInputView();
    }

    public boolean isTyping() {
        if(softKeyboardHelper == null){
            return false;
        }
        return softKeyboardHelper.isSoftKeyboardOpened();
    }

    private ConversationViewActivity getActivity() {
        return activity;
    }

    private PlayVessageManager getPlayManager(){
        return activity.playManager;
    }

    public void showImageChatInputView() {
        View v = this.mImageChatInputView.findViewById(R.id.et_msg);
        if(this.mImageChatInputView.getParent() == null){
            mImageChatInputViewContainer.addView(this.mImageChatInputView);
        }
        v.setVisibility(View.VISIBLE);
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    public void hideImageChatInputView(){
        View v = this.mImageChatInputView.findViewById(R.id.et_msg);
        v.setVisibility(View.INVISIBLE);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        v.clearFocus();
        mImageChatInputViewContainer.removeView(this.mImageChatInputView);
    }

    private int cachedBottomChatterBoardHeight = -1;
    private ChattersBoard.ChatterItem[] cachedTopChatterBoardItems;

    private SoftKeyboardStateHelper softKeyboardHelper;
    private SoftKeyboardStateHelper.SoftKeyboardStateListener onSoftKeyboardStateChanged = new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
        @Override
        public void onSoftKeyboardOpened(int keyboardHeightInPx) {
            if (cachedBottomChatterBoardHeight < 0){
                cachedBottomChatterBoardHeight = getPlayManager().getBottomChattersBoard().getLayoutParams().height;
            }
            getActivity().getSupportActionBar().setShowHideAnimationEnabled(true);
            getActivity().getSupportActionBar().hide();
            getPlayManager().getBottomChattersBoard().getLayoutParams().height = (int)(cachedBottomChatterBoardHeight * 0.8);
            cachedTopChatterBoardItems = getPlayManager().getTopChattersBoard().clearAllChatters(true);
            getPlayManager().getBottomChattersBoard().addChatters(cachedTopChatterBoardItems);
            updateVessageBubble();

        }
        @Override
        public void onSoftKeyboardClosed() {
            hideImageChatInputView();
            getActivity().getSupportActionBar().setShowHideAnimationEnabled(true);
            getActivity().getSupportActionBar().show();
            getPlayManager().getBottomChattersBoard().getLayoutParams().height = cachedBottomChatterBoardHeight;
            getPlayManager().getBottomChattersBoard().removeChatters(cachedTopChatterBoardItems);
            getPlayManager().getTopChattersBoard().addChatters(cachedTopChatterBoardItems);
            updateVessageBubble();
        }

        private void updateVessageBubble(){
            getPlayManager().hideVessageBubbleView();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPlayManager().relayoutCurrentVessage();
                }
            },666);
        }
    };

    private void initImageChatInputView(){
        this.mImageChatInputViewContainer = (ViewGroup) getActivity().findViewById(R.id.image_chat_input_view_container);
        this.mImageChatInputView = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.face_text_input_view,null);
        this.mImageChatInputView.findViewById(R.id.btn_send).setOnClickListener(onClickImageChatInputViews);
        this.mImageChatInputView.setOnClickListener(onClickImageChatInputViews);
        this.mMessageEditText = ((EditText)this.mImageChatInputView.findViewById(R.id.et_msg));
        this.mMessageEditText.setOnFocusChangeListener(onETMessageFocusChanged);
        this.mMessageEditText.addTextChangedListener(onETMessageChanged);
        this.mMessageEditText.setOnEditorActionListener(onETMessageAction);
        this.mMessageEditText.getBackground().setAlpha(0);
        this.mSendingProgress = (ProgressBar)mImageChatInputView.findViewById(R.id.progress_sending);
        this.mSendingProgress.setVisibility(View.INVISIBLE);
    }

    public void addKeyboardNotification(){
        if (softKeyboardHelper == null) {
            softKeyboardHelper = new SoftKeyboardStateHelper(getActivity().findViewById(R.id.activity_root_view));
            softKeyboardHelper.addSoftKeyboardStateListener(onSoftKeyboardStateChanged);
        }
    }

    private TextView.OnEditorActionListener onETMessageAction = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_SEND){
                onClickSend();
                return true;
            }
            return false;
        }
    };

    private void onClickSend() {
        sendVessage();
    }

    public void sending(int progress) {
        Log.i("PROGESS",String.valueOf(progress));
        if (progress >= 0) {
            mSendingProgress.setProgress(progress);
        } else if (softKeyboardHelper.isSoftKeyboardOpened()) {
            Toast.makeText(getActivity(), R.string.send_vessage_failure, Toast.LENGTH_SHORT).show();
        }
        //mSendingProgress.setVisibility(progress >= 0 && progress <= 100 ? View.VISIBLE : View.INVISIBLE);
    }

    private void sendVessage(){
        String textMessage = mMessageEditText.getEditableText().toString();
        String selectedChatImageId = getPlayManager().getSelectedChatImageId(); //chatImagesGralleryAdapter.getSelecetedImageId();
        if (StringHelper.isNullOrEmpty(textMessage)){
            Toast.makeText(getActivity(),R.string.no_text_message,Toast.LENGTH_SHORT).show();
        }else if(!StringHelper.isNullOrEmpty(getPlayManager().getConversation().chatterId)){
            getActivity().startSendingProgress();
            Vessage vessage = new Vessage();
            vessage.isGroup = getPlayManager().getConversation().type == Conversation.TYPE_GROUP_CHAT;
            vessage.typeId = Vessage.TYPE_FACE_TEXT;
            vessage.extraInfo = getActivity().getSendVessageExtraInfo();
            vessage.ts = DateHelper.getUnixTimeSpan();
            vessage.fileId = selectedChatImageId;
            vessage.isRead = true;
            vessage.isReady = true;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("textMessage",textMessage);
                vessage.body = jsonObject.toString(0);
                String receiver = getPlayManager().getConversation().chatterId;
                SendVessageQueue.getInstance().pushSendVessageTask(receiver,vessage, SendVessageTaskSteps.SEND_NORMAL_VESSAGE_STEPS,null);
                mMessageEditText.setText(null);
            } catch (JSONException e) {
                Toast.makeText(getActivity(),R.string.sendDataError,Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getActivity(),R.string.noChatterId,Toast.LENGTH_SHORT).show();
        }
    }

    private TextWatcher onETMessageChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private View.OnFocusChangeListener onETMessageFocusChanged = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(!hasFocus){
                hideImageChatInputView();
            }
        }
    };

    private View.OnClickListener onClickImageChatInputViews = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (v.getId() == R.id.root_view){
                hideImageChatInputView();
                return;
            }
            AnimationHelper.startAnimation(getActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    switch (v.getId()){
                        case R.id.btn_send:onClickSend();break;
                    }
                }
            });

        }
    };

    public void onDestory(){

    }
}
