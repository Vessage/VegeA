package cn.bahamut.vessage.conversation.view;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.vessagehandler.FaceTextVessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.NoVessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.UnknowVessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.VessageGestureHandler;
import cn.bahamut.vessage.conversation.vessagehandler.VessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.VideoVessageHandler;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/6/1.
 */
public class ConversationViewPlayManager extends ConversationViewActivity.ConversationViewProxyManager implements VessageGestureHandler{
    private List<Vessage> notReadVessages = new LinkedList<>();
    private Vessage presentingVessage;

    private View playVessageContainer;
    private ViewGroup vessageContentContainer;
    private TextView badgeTextView;
    private Button mRecordVideoButton;
    private Button mNextVideoButton;
    private HashMap<Integer,VessageHandler> vessageHandlers;

    private Button mImageChatButton;

    private VessageHandler currentHandler = null;

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        playVessageContainer = findViewById(R.id.play_vsg_container);
        playVessageContainer.setOnClickListener(onClickPlayVessageContainer);
        badgeTextView = (TextView)findViewById(R.id.badge_tv);
        vessageContentContainer = (ViewGroup)findViewById(R.id.vsg_content_container);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initHandlers();
        hideView(badgeTextView);
        initBottomButtons();
        sendImageChatManager = new SendImageChatMessageManager(activity);
        initNotReadVessages();
        if(isGroupChat()){
            onChatGroupUpdated();
        }else {
            onChatterUpdated();
        }
    }



    private void initHandlers() {
        vessageHandlers = new HashMap<>();
        vessageHandlers.put(Vessage.TYPE_FACE_TEXT,new FaceTextVessageHandler(this,vessageContentContainer));
        vessageHandlers.put(Vessage.TYPE_NO_VESSAGE,new NoVessageHandler(this, vessageContentContainer));
        vessageHandlers.put(Vessage.TYPE_UNKNOW,new UnknowVessageHandler(this, vessageContentContainer));
        vessageHandlers.put(Vessage.TYPE_VIDEO,new VideoVessageHandler(this, vessageContentContainer));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onChatterUpdated() {
        super.onChatterUpdated();
        getConversationViewActivity().setActivityTitle(getConversationTitle());
    }

    @Override
    public void onChatGroupUpdated() {
        super.onChatGroupUpdated();
        getConversationViewActivity().setActivityTitle(getConversationTitle());
    }

    @Override
    public void onFling(int direction, float x, float y) {
        if(sendImageChatManager.isTyping()){
            return;
        }
        if (currentHandler != null && currentHandler instanceof VessageGestureHandler) {
            try{
                ((VessageGestureHandler)currentHandler).onFling(direction, x, y);
            }catch (Exception e){
            }
        }
    }

    @Override
    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(sendImageChatManager.isTyping()){
            return;
        }
        if (currentHandler != null && currentHandler instanceof VessageGestureHandler) {
            try {
                ((VessageGestureHandler) currentHandler).onScroll(e1, e2, distanceX, distanceY);
            } catch (Exception e) {
            }
        }
    }

    private void setBadge(int badge){
        if(badge == 0){
            setBadge(null);
        }else {
            setBadge(String.valueOf(badge));
        }
    }

    private void setBadge(String badge){
        if(StringHelper.isNullOrEmpty(badge)){
            hideView(badgeTextView);
        }else {
            showView(badgeTextView);
            badgeTextView.setText(badge);
            AnimationHelper.startAnimation(getConversationViewActivity(),badgeTextView,R.anim.button_scale_anim);
        }
    }

    private void initNotReadVessages() {
        notReadVessages.clear();
        List<Vessage> vsgs = ServicesProvider.getService(VessageService.class).getNotReadVessage(getConversation().chatterId);
        if(vsgs.size() > 0){
            notReadVessages.addAll(vsgs);
        }else {
            Vessage vsg = ServicesProvider.getService(VessageService.class).getCachedNewestVessage(getConversation().chatterId);
            if (vsg != null){
                notReadVessages.add(vsg);
            }
        }
        setPresentingVessage();
    }

    private void initBottomButtons() {
        mRecordVideoButton = (Button)findViewById(R.id.record_btn);
        mNextVideoButton = (Button)findViewById(R.id.next_msg_btn);
        mImageChatButton = (Button)findViewById(R.id.btn_image_chat);

        mRecordVideoButton.setOnClickListener(onClickRecordButton);
        mNextVideoButton.setOnClickListener(onClickNextVessageButton);

        mImageChatButton.setOnClickListener(onClickImageChatButton);
    }

    public void readVessage() {
        if(!presentingVessage.isRead){
            MobclickAgent.onEvent(getConversationViewActivity(),"Vege_ReadVessage");
        }
        ServicesProvider.getService(VessageService.class).readVessage(presentingVessage);
        updateBadge();
    }

    public void setSendingVessage(Vessage vessage){
        if(vessage == null){
            setPresentingVessage();
        }else {
            currentHandler = vessage.isValidVessage() ? this.getVessageHandler(vessage.typeId) : this.getVessageHandler(Vessage.TYPE_UNKNOW);
            currentHandler.onPresentingVessageSeted(this.presentingVessage,vessage);
            Handler action = new Handler();
            action.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setPresentingVessage();
                }
            },10000);
        }
    }

    private void setPresentingVessage() {
        if(notReadVessages.size() > 0){
            Vessage oldVsg = this.presentingVessage;
            this.presentingVessage = notReadVessages.get(0);
            currentHandler = this.presentingVessage.isValidVessage() ? this.getVessageHandler(this.presentingVessage.typeId) : this.getVessageHandler(Vessage.TYPE_UNKNOW);
            currentHandler.onPresentingVessageSeted(oldVsg,this.presentingVessage);
        }else {
            currentHandler = this.getNoVessageHandler();
            currentHandler.onPresentingVessageSeted(null,null);
        }
        updateBadge();
        updateNextButton();
    }

    private VessageHandler getNoVessageHandler() {
        return getVessageHandler(Vessage.TYPE_NO_VESSAGE);
    }

    private VessageHandler getVessageHandler(int typeId) {
        VessageHandler handler = vessageHandlers.get(typeId);
        if (handler == null){
            handler = vessageHandlers.get(Vessage.TYPE_UNKNOW);
        }
        return handler;
    }

    @Override
    public void onVessagesReceived(Collection<Vessage> vessages) {
        super.onVessagesReceived(vessages);
        notReadVessages.addAll(vessages);
        setPresentingVessage();
    }

    private void loadNextVessage(){
        if(notReadVessages.size() > 1){
            Vessage vsg = presentingVessage;
            String fileId = vsg.fileId;
            notReadVessages.remove(0);

            setPresentingVessage();
            ServicesProvider.getService(VessageService.class).removeVessage(vsg);
            File oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId,".mp4");
            if(oldVideoFile != null){
                try{
                    oldVideoFile.delete();
                    Log.d("ConversationView","Delete Passed Vessage Video File");
                }catch (Exception ex){
                    oldVideoFile.deleteOnExit();
                    Log.d("ConversationView","Delete Passed Vessage Video File On Exit");
                }
            }
        }else {
            Toast.makeText(getConversationViewActivity(),R.string.no_more_vessages,Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNextButton() {
        if(notReadVessages.size() > 1){
            showView(mNextVideoButton);
        }else {
            hideView(mNextVideoButton);
        }
    }

    private void updateBadge(){
        if(getChatter() != null && StringHelper.isStringNullOrWhiteSpace(getChatter().userId) == false){
            int badge = ServicesProvider.getService(VessageService.class).getNotReadVessageCount(getChatter().userId);
            setBadge(badge);
        }else {
            setBadge(0);
        }
    }



    private View.OnClickListener onClickPlayVessageContainer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendImageChatManager.hideImageChatInputView();
        }
    };


    @Override
    public void onConfigurationChanged() {
        super.onConfigurationChanged();

    }

    @Override
    public void onBackKeyPressed() {
        super.onBackKeyPressed();
        sendImageChatManager.hideImageChatInputView();
    }

    @Override
    public void sending(int progress) {
        if(sendImageChatManager != null){
            sendImageChatManager.sending(progress);
        }
    }

    private SendImageChatMessageManager sendImageChatManager;
    private View.OnClickListener onClickImageChatButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    sendImageChatManager.addKeyboardNotification();
                    sendImageChatManager.showImageChatInputView();
                }
            });

        }
    };

    private View.OnClickListener onClickNextVessageButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(presentingVessage.isRead){
                        loadNextVessage();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getConversationViewActivity())
                            .setTitle(R.string.ask_jump_vessage)
                            .setMessage(R.string.jump_vessage_will_delete)
                            .setPositiveButton(R.string.jump, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MobclickAgent.onEvent(getConversationViewActivity(),"Vege_JumpVessage");
                                    loadNextVessage();
                                }
                            });

                    builder.setNegativeButton(R.string.cancel_jump, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
            });

        }
    };

    public void tryShowNextVessage(){
        onClickNextVessageButton.onClick(mNextVideoButton);
    }

    private View.OnClickListener onClickRecordButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    getConversationViewActivity().tryShowRecordViews();
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendImageChatManager.onDestory();
    }

    public View getPlayVessageContainer() {
        return playVessageContainer;
    }
}
