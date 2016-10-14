package cn.bahamut.vessage.conversation.view;

import android.content.DialogInterface;
import android.content.Intent;
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
import cn.bahamut.vessage.conversation.vessagehandler.ImageVessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.NoVessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.UnknowVessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.VessageGestureHandler;
import cn.bahamut.vessage.conversation.vessagehandler.VessageHandler;
import cn.bahamut.vessage.conversation.vessagehandler.VideoVessageHandler;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/6/1.
 */
public class ConversationViewPlayManager extends ConversationViewActivity.ConversationViewProxyManager implements VessageGestureHandler{

    private List<Vessage> readedVessages = new LinkedList<>();
    private List<Vessage> notReadVessages = new LinkedList<>();
    private int currentIndex = 0;
    private Vessage presentingVessage;

    private ViewGroup vessageContentContainer;
    private TextView badgeTextView;
    private Button mMiddleButton;
    private Button mLeftButton;
    private HashMap<Integer,VessageHandler> vessageHandlers;

    private Button mImageChatButton;

    private VessageHandler currentHandler = null;

    private SendMoreTypeVessageManager sendMoreTypeVessageManager;
    private SendImageChatMessageManager sendImageChatManager;

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        badgeTextView = (TextView)findViewById(R.id.badge_tv);
        vessageContentContainer = (ViewGroup)findViewById(R.id.vsg_content_container);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initHandlers();
        hideView(badgeTextView);
        initBottomButtons();
        sendImageChatManager = new SendImageChatMessageManager(activity);
        sendMoreTypeVessageManager = new SendMoreTypeVessageManager(activity);
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
        vessageHandlers.put(Vessage.TYPE_CHAT_VIDEO,new VideoVessageHandler(this, vessageContentContainer));
        vessageHandlers.put(Vessage.TYPE_IMAGE,new ImageVessageHandler(this,vessageContentContainer));
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
    public boolean onFling(int direction, float x, float y) {
        if(sendImageChatManager.isTyping()){
            return false;
        }
        if (currentHandler != null && currentHandler instanceof VessageGestureHandler) {
            try{
                return ((VessageGestureHandler)currentHandler).onFling(direction, x, y);
            }catch (Exception e){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(sendImageChatManager.isTyping()){
            return false;
        }
        if (currentHandler != null && currentHandler instanceof VessageGestureHandler) {
            try {
                return ((VessageGestureHandler) currentHandler).onScroll(e1, e2, distanceX, distanceY);
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return sendMoreTypeVessageManager.onActivityResult(requestCode, resultCode,data);
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
        mMiddleButton = (Button)findViewById(R.id.new_vessage_btn);
        mLeftButton = (Button)findViewById(R.id.play_left_btn);
        mImageChatButton = (Button)findViewById(R.id.btn_image_chat);

        mMiddleButton.setOnClickListener(onClickMiddleButton);

        mMiddleButton.setOnLongClickListener(onLongClickMiddleButton);

        mLeftButton.setOnClickListener(onClickLeftButton);

        mImageChatButton.setOnClickListener(onClickImageChatButton);
    }

    public void readVessage() {
        if(!presentingVessage.isRead){
            ServicesProvider.getService(VessageService.class).readVessage(presentingVessage);
            updateBadge();
            MobclickAgent.onEvent(getConversationViewActivity(),"Vege_ReadVessage");
        }
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

    public void setSendingVessage(Vessage vessage){
        if(vessage != null){
            currentHandler = vessage.isValidVessage() ? this.getVessageHandler(vessage.typeId) : this.getVessageHandler(Vessage.TYPE_UNKNOW);
            currentHandler.onPresentingVessageSeted(this.presentingVessage,vessage);
            this.presentingVessage = vessage;
            Handler action = new Handler();
            action.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setPresentingVessage();
                }
            },10000);
        }
    }

    private void loadNextVessage(){
        if (notReadVessages.size() == 0){
            Toast.makeText(getConversationViewActivity(),R.string.no_not_read_vessages,Toast.LENGTH_SHORT).show();
        }else if(notReadVessages.size() > 1 && notReadVessages.size() > currentIndex + 1){
            currentIndex += 1;
            setPresentingVessage();
        }else {
            Toast.makeText(getConversationViewActivity(),R.string.no_more_vessages,Toast.LENGTH_SHORT).show();
        }
    }

    private void removeReadedVessages(){
        for (Vessage vessage : readedVessages) {
            String fileId = vessage.fileId;
            ServicesProvider.getService(VessageService.class).removeVessage(vessage);
            File oldVideoFile = null;
            if (vessage.typeId == Vessage.TYPE_CHAT_VIDEO){
                oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId,".mp4");
            }else if (vessage.typeId == Vessage.TYPE_IMAGE){
                oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId,".jpg");
            }
            if(oldVideoFile != null){
                try{
                    oldVideoFile.delete();
                    Log.d("ConversationView","Delete Passed Vessage File");
                }catch (Exception ex){
                    oldVideoFile.deleteOnExit();
                    Log.d("ConversationView","Delete Passed Vessage Video File On Exit");
                }
            }
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

    @Override
    public void onBackKeyPressed() {
        super.onBackKeyPressed();
        sendMoreTypeVessageManager.hideVessageTypesHub();
        sendImageChatManager.hideImageChatInputView();
    }

    @Override
    public void sending(int progress) {
        if(sendImageChatManager != null){
            sendImageChatManager.sending(progress);
        }
    }

    private View.OnClickListener onClickImageChatButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    sendImageChatManager.addKeyboardNotification();
                    sendImageChatManager.showImageChatInputView();
                    Log.i("AAA",String.valueOf(vessageContentContainer.getVisibility() == View.VISIBLE));
                }
            });

        }
    };

    private View.OnClickListener onClickLeftButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendMoreTypeVessageManager.showVessageTypesHub();
        }
    };

    public void tryShowPreviousVessage(){
        if (notReadVessages.size() > 0 && currentIndex > 0){
            currentIndex -= 1;
            setPresentingVessage();
        }else {
            Toast.makeText(getConversationViewActivity(),R.string.no_previous_vessages,Toast.LENGTH_SHORT).show();
        }
    }

    public void tryShowNextVessage(){
        if(presentingVessage == null || notReadVessages.size() <= 1){
            Toast.makeText(getConversationViewActivity(),R.string.no_more_vessages,Toast.LENGTH_SHORT).show();
            return;
        }
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
                        readVessage();
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

    private void setPresentingVessage() {
        if(notReadVessages.size() > 0){
            Vessage oldVsg = this.presentingVessage;
            this.presentingVessage = notReadVessages.get(currentIndex);
            currentHandler = this.presentingVessage.isValidVessage() ? this.getVessageHandler(this.presentingVessage.typeId) : this.getVessageHandler(Vessage.TYPE_UNKNOW);
            currentHandler.onPresentingVessageSeted(oldVsg,this.presentingVessage);
        }else {
            currentHandler = this.getNoVessageHandler();
            currentHandler.onPresentingVessageSeted(null,null);
        }
        updateBadge();
    }

    private View.OnLongClickListener onLongClickMiddleButton = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.new_vessage_btn){
                sendMoreTypeVessageManager.showVessageTypesHub();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener onClickMiddleButton = new View.OnClickListener() {
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
    public void onResume() {
        super.onResume();
        setPresentingVessage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeReadedVessages();
        sendMoreTypeVessageManager.onDestory();
        sendImageChatManager.onDestory();
    }
}
