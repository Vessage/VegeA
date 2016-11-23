package cn.bahamut.vessage.conversation.chat;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import cn.bahamut.common.SoftKeyboardStateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/9/26.
 */

public class MessageInputViewManager {

    public interface SendImageChatMessageManagerDelegate extends VessageGestureHandler {
        void onSoftKeyboardOpened(MessageInputViewManager sender, int keyboardHeightInPx);

        void onSoftKeyboardClosed(MessageInputViewManager sender);
    }

    private ConversationViewActivity activity;
    private ViewGroup mImageChatInputView;
    private ViewGroup mImageChatInputViewContainer;
    private EditText mMessageEditText;
    private ProgressBar mSendingProgress;

    private SendImageChatMessageManagerDelegate delegate;

    public MessageInputViewManager(ConversationViewActivity activity) {
        this.activity = activity;
        initImageChatInputView();
    }

    public SendImageChatMessageManagerDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(SendImageChatMessageManagerDelegate delegate) {
        this.delegate = delegate;
    }

    public boolean isTyping() {
        if (softKeyboardHelper == null) {
            return false;
        }
        return softKeyboardHelper.isSoftKeyboardOpened();
    }

    private ConversationViewActivity getActivity() {
        return activity;
    }

    private PlayVessageManager getPlayManager() {
        return activity.playManager;
    }

    public void showImageChatInputView() {
        View v = this.mImageChatInputView.findViewById(R.id.et_msg);
        if (this.mImageChatInputView.getParent() == null) {
            mImageChatInputViewContainer.addView(this.mImageChatInputView);
        }
        v.setVisibility(View.VISIBLE);
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    public void hideImageChatInputView() {
        View v = this.mImageChatInputView.findViewById(R.id.et_msg);
        v.setVisibility(View.INVISIBLE);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();
        mImageChatInputViewContainer.removeView(this.mImageChatInputView);
    }


    private SoftKeyboardStateHelper softKeyboardHelper;
    private SoftKeyboardStateHelper.SoftKeyboardStateListener onSoftKeyboardStateChanged = new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
        @Override
        public void onSoftKeyboardOpened(int keyboardHeightInPx) {
            if (delegate != null) {
                delegate.onSoftKeyboardOpened(MessageInputViewManager.this, keyboardHeightInPx);
            }
        }

        @Override
        public void onSoftKeyboardClosed() {
            hideImageChatInputView();
            if (delegate != null) {
                delegate.onSoftKeyboardClosed(MessageInputViewManager.this);
            }
        }


    };

    private void initImageChatInputView() {
        this.mImageChatInputViewContainer = (ViewGroup) getActivity().findViewById(R.id.image_chat_input_view_container);
        this.mImageChatInputView = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.conversation_face_text_input_view, null);
        this.mImageChatInputView.findViewById(R.id.btn_send).setOnClickListener(onClickImageChatInputViews);
        this.mImageChatInputView.setOnClickListener(onClickImageChatInputViews);
        this.mMessageEditText = ((EditText) this.mImageChatInputView.findViewById(R.id.et_msg));
        this.mMessageEditText.setOnFocusChangeListener(onETMessageFocusChanged);
        this.mMessageEditText.addTextChangedListener(onETMessageChanged);
        this.mMessageEditText.setOnEditorActionListener(onETMessageAction);
        this.mMessageEditText.getBackground().setAlpha(0);
        this.mSendingProgress = (ProgressBar) mImageChatInputView.findViewById(R.id.progress_sending);
        this.mSendingProgress.setVisibility(View.INVISIBLE);

        inputViewGestureDetector = new GestureDetector(getActivity(), inputViewOnGestureListener);
        this.mImageChatInputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return inputViewGestureDetector.onTouchEvent(event);
            }
        });
    }

    private GestureDetector inputViewGestureDetector;
    private GestureDetector.SimpleOnGestureListener inputViewOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (delegate == null) {
                return false;
            }
            float minMove = 160;        //最小滑动距离
            float minVelocity = 0;     //最小滑动速度
            float beginX = e1.getX();
            float endX = e2.getX();
            float beginY = e1.getY();
            float endY = e2.getY();

            if (beginX - endX > minMove && Math.abs(velocityX) > minVelocity) {  //左滑
                return delegate.onFling(VessageGestureHandler.FlingDerection.LEFT, velocityX, velocityY);
            } else if (endX - beginX > minMove && Math.abs(velocityX) > minVelocity) {  //右滑
                return delegate.onFling(VessageGestureHandler.FlingDerection.RIGHT, velocityX, velocityY);
            } else if (beginY - endY > minMove && Math.abs(velocityY) > minVelocity) {  //上滑
                return delegate.onFling(VessageGestureHandler.FlingDerection.UP, velocityX, velocityY);
            } else if (endY - beginY > minMove && Math.abs(velocityY) > minVelocity) {  //下滑
                return delegate.onFling(VessageGestureHandler.FlingDerection.DOWN, velocityX, velocityY);
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (delegate == null) {
                return false;
            }
            return delegate.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (delegate == null) {
                return false;
            }
            return delegate.onTapUp();
        }
    };

    public void addKeyboardNotification() {
        if (softKeyboardHelper == null) {
            softKeyboardHelper = new SoftKeyboardStateHelper(getActivity().findViewById(R.id.activity_root_view));
            softKeyboardHelper.addSoftKeyboardStateListener(onSoftKeyboardStateChanged);
        }
    }

    private TextView.OnEditorActionListener onETMessageAction = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
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
        Log.i("PROGESS", String.valueOf(progress));
        if (progress >= 0) {
            mSendingProgress.setProgress(progress);
        } else if (softKeyboardHelper.isSoftKeyboardOpened()) {
            Toast.makeText(getActivity(), R.string.send_vessage_failure, Toast.LENGTH_SHORT).show();
        }
        mSendingProgress.setVisibility(progress >= 0 && progress <= 100 ? View.VISIBLE : View.INVISIBLE);
    }

    private void sendVessage() {
        String textMessage = mMessageEditText.getEditableText().toString();
        String selectedChatImageId = getPlayManager().getSelectedChatImageId(); //chatImagesGralleryAdapter.getSelecetedImageId();
        if (StringHelper.isNullOrEmpty(textMessage)) {
            Toast.makeText(getActivity(), R.string.no_text_message, Toast.LENGTH_SHORT).show();
        } else if (!StringHelper.isNullOrEmpty(getPlayManager().getConversation().chatterId)) {
            getActivity().startSendingProgress();
            Vessage vessage = new Vessage();
            boolean isGroup = getPlayManager().getConversation().type == Conversation.TYPE_GROUP_CHAT;
            vessage.typeId = Vessage.TYPE_FACE_TEXT;
            vessage.fileId = selectedChatImageId;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("textMessage", textMessage);
                vessage.body = jsonObject.toString(0);
                String receiver = getPlayManager().getConversation().chatterId;
                SendVessageQueue.getInstance().pushSendVessageTask(receiver, isGroup, vessage, SendVessageTaskSteps.SEND_NORMAL_VESSAGE_STEPS, null);
                mMessageEditText.setText(null);
            } catch (JSONException e) {
                Toast.makeText(getActivity(), R.string.sendDataError, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), R.string.noChatterId, Toast.LENGTH_SHORT).show();
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
            if (!hasFocus) {
                hideImageChatInputView();
            }
        }
    };

    private View.OnClickListener onClickImageChatInputViews = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (v.getId() == R.id.root_view) {
                hideImageChatInputView();
                return;
            }
            AnimationHelper.startAnimation(getActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    switch (v.getId()) {
                        case R.id.btn_send:
                            onClickSend();
                            break;
                    }
                }
            });

        }
    };

    public void onDestory() {

    }
}
