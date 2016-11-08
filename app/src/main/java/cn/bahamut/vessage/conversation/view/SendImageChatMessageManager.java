package cn.bahamut.vessage.conversation.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.SoftKeyboardStateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.ChatImage;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/9/26.
 */

public class SendImageChatMessageManager {

    static class ChatImagesGralleryAdapter extends RecyclerView.Adapter<ChatImagesGralleryAdapter.ViewHolder>{
        private LayoutInflater mInflater;
        private Activity context;
        private int selectedIndex = -1;
        private List<ChatImage> chatImages = new LinkedList<>();

        public String getSelecetedImageId(){
            if (selectedIndex >= 0 && selectedIndex < chatImages.size()){
                return chatImages.get(selectedIndex).imageId;
            }
            return null;
        }

        ChatImagesGralleryAdapter(Activity context){
            this.context = context;
            mInflater = this.context.getLayoutInflater();
            ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED,onMyChatImagesUpdated);
            refreshChatImages();
        }

        public void onDestory(){
            ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED,onMyChatImagesUpdated);
        }

        private Observer onMyChatImagesUpdated = new Observer() {
            @Override
            public void update(ObserverState state) {
                refreshChatImages();
            }
        };

        public void refreshChatImages(){
            ChatImage[] arr = ServicesProvider.getService(UserService.class).getMyChatImages();
            chatImages.clear();
            for (ChatImage chatImage : arr) {
                chatImages.add(chatImage);
            }
            if (selectedIndex == -1 && chatImages.size() > 0){
                selectedIndex = 0;
            }
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.face_image_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.checkMarkView = view.findViewById(R.id.check_mark);
            viewHolder.imageView = (ImageView)view.findViewById(R.id.imageView);
            viewHolder.titleView = (TextView)view.findViewById(R.id.title_view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            ChatImage chatImage = chatImages.get(position);
            holder.checkMarkView.setVisibility(position == selectedIndex ? View.VISIBLE : View.INVISIBLE);
            holder.titleView.setText(chatImage.imageType);
            ImageHelper.setImageByFileId(holder.imageView,chatImage.imageId);
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onItemClick(holder.itemView, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return chatImages.size();
        }

        public void onItemClick(View view, int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public View checkMarkView;
            public ImageView imageView;
            public TextView titleView;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

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

    private ConversationViewPlayManager getPlayManager(){
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
            getPlayManager().hideBubbleVessageView();
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
            vessage.isGroup = getPlayManager().getConversation().isGroup;
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
