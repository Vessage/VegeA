package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import cn.bahamut.vessage.usersettings.ChangeChatBackgroundActivity;

public class ConversationViewActivity extends AppCompatActivity {

    public static class ConversationViewProxyManager{

        private ConversationViewActivity conversationViewActivity;

        public void  initManager(ConversationViewActivity activity){
            this.conversationViewActivity = activity;
        }

        public ConversationViewActivity getConversationViewActivity() {
            return conversationViewActivity;
        }
        protected void hideView(View v){
            v.setVisibility(View.INVISIBLE);
        }

        protected void showView(View v){
            v.setVisibility(View.VISIBLE);
        }
        public View findViewById(int resId){
            return conversationViewActivity.findViewById(resId);
        }

        public VessageUser getChatter(){
            return conversationViewActivity.chatter;
        }

        public Conversation getConversation(){
            return conversationViewActivity.conversation;
        }
        public void onChatterUpdated(){}
        public void onVessagesReceived(Collection<Vessage> vessages){}
        public void onDestroy(){}

    }

    private static final int REQUEST_CHANGE_NOTE_CODE = 1;
    private Conversation conversation;
    private VessageUser chatter;

    public void tryShowRecordViews() {
        if(ServicesProvider.getService(UserService.class).isMyProfileHaveChatBackground()){
            findViewById(R.id.play_vsg_container).setVisibility(View.INVISIBLE);
            findViewById(R.id.record_vsg_container).setVisibility(View.VISIBLE);
            getSupportActionBar().setShowHideAnimationEnabled(false);
            getSupportActionBar().hide();
            fullScreen(true);
            recordManager.startRecord();
            recordManager.chatterImageFadeIn();
        }else {
            askUploadChatBcg();
        }
    }

    public void hidePreview(){
        View previewView = findViewById(R.id.preview_view);
        previewView.setAlpha(0);
    }

    public void showPreview(){
        View previewView = findViewById(R.id.preview_view);
        previewView.setAlpha(1);
    }

    public void showPlayViews(){
        getSupportActionBar().show();
        fullScreen(false);
        findViewById(R.id.play_vsg_container).setVisibility(View.VISIBLE);
        findViewById(R.id.record_vsg_container).setVisibility(View.INVISIBLE);
        showPreview();
    }

    private void fullScreen(boolean enable) {
        WindowManager.LayoutParams p = this.getWindow().getAttributes();
        if (enable) {

            p.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;//|=：或等于，取其一

        } else {
            p.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);//&=：与等于，取其二同时满足，     ~ ： 取反

        }
        getWindow().setAttributes(p);
    }

    ConversationViewPlayManager playManager;
    ConversationViewRecordManager recordManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_view);

        String conversationId = getIntent().getStringExtra("conversationId");
        if(conversationId == null){
            finish();
            Toast.makeText(this,R.string.no_conversation,Toast.LENGTH_SHORT).show();
        }else{
            Conversation storeConversation = ServicesProvider.getService(ConversationService.class).openConversation(conversationId);
            if(storeConversation == null){
                finish();
                Toast.makeText(this, R.string.no_conversation, Toast.LENGTH_SHORT).show();
            }else{
                setConversation(storeConversation);
                setActivityTitle(conversation.noteName);
                initNotifications();
                prepareChatter();
                playManager = new ConversationViewPlayManager();
                playManager.initManager(this);
                recordManager = new ConversationViewRecordManager();
                recordManager.initManager(this);
                showPlayViews();
            }
        }
    }

    private void setConversation(Conversation conversation) {
        this.conversation = new Conversation();
        this.conversation.chatterId = conversation.chatterId;
        this.conversation.chatterMobile = conversation.chatterMobile;
        this.conversation.chatterMobileHash = conversation.chatterMobileHash;
        this.conversation.conversationId = conversation.conversationId;
        this.conversation.noteName = conversation.noteName;
        this.conversation.sLastMessageTime = conversation.sLastMessageTime;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,Menu.FIRST,0,R.string.note_conversation).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Menu.FIRST:
                showUserProfileAlert();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showNoteConversationDialog() {
        EditPropertyActivity.showEditPropertyActivity(this,REQUEST_CHANGE_NOTE_CODE,R.string.note_conversation,conversation.noteName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHANGE_NOTE_CODE && resultCode == EditPropertyActivity.RESULT_CODE_SAVED_PROPERTY){
            String newNoteName = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
            ServicesProvider.getService(ConversationService.class).setConversationNoteName(conversation.conversationId,newNoteName);
            if(StringHelper.notNullOrEmpty(chatter.userId)){
                ServicesProvider.getService(UserService.class).setUserNoteName(chatter.userId,newNoteName);
            }
            setActivityTitle(newNoteName);
        }

    }

    private void initNotifications() {

        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
        ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playManager.onDestroy();
        recordManager.onDestroy();
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
    }


    private void prepareChatter() {
        UserService userService = ServicesProvider.getService(UserService.class);
        userService.addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        VessageUser storedUser = null;
        if(!StringHelper.isNullOrEmpty(conversation.chatterId)){
            storedUser = userService.getUserById(conversation.chatterId);
            if(storedUser == null) {
                storedUser = new VessageUser();
                storedUser.userId = conversation.chatterId;
                storedUser.mobile = conversation.chatterMobile;
                userService.fetchUserByUserId(conversation.chatterId);
            }
        }else {
            storedUser = new VessageUser();
            storedUser.mobile = conversation.chatterMobile;
            userService.fetchUserByMobile(storedUser.mobile);
        }
        setChatter(storedUser);
    }

    private Observer onVessageUserUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser)state.getInfo();
            if(VessageUser.isTheSameUser(user,chatter)){
                setChatter(user);
                if(playManager!=null){
                    playManager.onChatterUpdated();
                }
                if(recordManager != null){
                    recordManager.onChatterUpdated();
                }
            }
        }
    };

    private void setChatter(VessageUser user) {
        this.chatter = new VessageUser();
        this.chatter.userId = user.userId;
        this.chatter.lastUpdatedTime = user.lastUpdatedTime;
        this.chatter.accountId = user.accountId;
        this.chatter.avatar = user.avatar;
        this.chatter.mainChatImage = user.mainChatImage;
        this.chatter.mobile = user.mobile;
        this.chatter.motto = user.motto;
        this.chatter.nickName = user.nickName;
    }

    private Observer onNewVessagesReceived = new Observer() {
        @Override
        public void update(ObserverState state) {
            List<Vessage> vsgs = (List<Vessage>)state.getInfo();
            List<Vessage> receivedVsgs = new ArrayList<>();
            for (Vessage vsg : vsgs) {
                if(vsg.sender.equals(conversation.chatterId)){
                    receivedVsgs.add(vsg);
                }
            }
            playManager.onVessagesReceived(receivedVsgs);
            recordManager.onVessagesReceived(receivedVsgs);
        }
    };

    public void setActivityTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    private void askUploadChatBcg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationViewActivity.this);
        builder.setTitle(R.string.need_upload_chat_bcg_title);
        builder.setMessage(R.string.need_upload_chat_bcg_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ConversationViewActivity.this, ChangeChatBackgroundActivity.class);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }


    private void showUserProfileAlert(){
        String msg;
        if(StringHelper.isNullOrEmpty(chatter.accountId)){
            msg = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
        }else {
            msg = LocalizedStringHelper.getLocalizedString(R.string.account) + ":" + chatter.accountId;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationViewActivity.this)
                .setTitle(conversation.noteName)
                .setMessage(msg)
                .setPositiveButton(R.string.note_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showNoteConversationDialog();
                    }
                });
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    public static void openConversationView(Context context, Conversation conversation){
        MobclickAgent.onEvent(context,"Vege_OpenConversation");
        Intent intent = new Intent();
        intent.putExtra("conversationId",conversation.conversationId);
        intent.setClass(context, ConversationViewActivity.class);
        context.startActivity(intent);
    }

    public static void openConversationView(Context context, String conversationId, int flags){
        MobclickAgent.onEvent(context,"Vege_OpenConversation");
        Intent intent = new Intent();
        intent.putExtra("conversationId",conversationId);
        intent.setFlags(flags);
        intent.setClass(context, ConversationViewActivity.class);
        context.startActivity(intent);
    }

    public static void openConversation(Context context, String userId,String noteName){
        MobclickAgent.onEvent(context,"Vege_OpenConversation");
        Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(userId,noteName);
        openConversationView(context,conversation);
    }
}
