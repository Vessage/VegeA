package cn.bahamut.vessage.conversation.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;

import org.apache.commons.codec1.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.UsersListActivity;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
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

        public boolean isGroupChat(){
            return getConversationViewActivity().isGroupChat();
        }

        public ChatGroup getChatGroup(){
            return conversationViewActivity.chatGroup;
        }

        protected String getConversationTitle() {
            return conversationViewActivity.getConversationTitle();
        }

        public Conversation getConversation(){
            return conversationViewActivity.conversation;
        }
        public void onChatterUpdated(){}
        public void onChatGroupUpdated() {}
        public void onVessagesReceived(Collection<Vessage> vessages){}
        public void onDestroy(){}
        public void onPause(){}
        public void onResume(){}
        public void onSwitchToManager(){}
    }

    private boolean isGroupChat() {
        return conversation.isGroup;
    }

    protected String getConversationTitle() {
        if(conversation.isGroup && chatGroup != null){
            return chatGroup.groupName;
        }else if(!conversation.isGroup && chatter  != null) {
            return ServicesProvider.getService(UserService.class).getUserNoteName(chatter.userId);
        }
        return LocalizedStringHelper.getLocalizedString(R.string.nameless_conversation);
    }
    private static final int REQUEST_CHANGE_NOTE_CODE = 1;
    private Conversation conversation;
    private VessageUser chatter;
    private ChatGroup chatGroup;
    private String sendVessageExtraInfo;

    public String getSendVessageExtraInfo() {
        return sendVessageExtraInfo;
    }

    private void generateVessageExtraInfo(){
        UserService userService = ServicesProvider.getService(UserService.class);
        String nick = userService.getMyProfile().nickName;
        String mobile = userService.getMyProfile().mobile;
        String mobileHash = "";
        if(!StringHelper.isStringNullOrWhiteSpace(mobile)){
            mobileHash = DigestUtils.md5Hex(mobile);
        }
        sendVessageExtraInfo = String.format("{\"accountId\":\"%s\",\"nickName\":\"%s\",\"mobileHash\":\"%s\"}", userService.getMyProfile().accountId,nick,mobileHash);
    }

    public void tryShowRecordViews() {

        if(isGroupChat() && isQuitedChatGroup()){
            Toast.makeText(ConversationViewActivity.this,R.string.u_not_in_group,Toast.LENGTH_SHORT).show();
            return;
        }

        if(ServicesProvider.getService(UserService.class).isMyProfileHaveChatBackground()){
            findViewById(R.id.play_vsg_container).setVisibility(View.INVISIBLE);
            findViewById(R.id.record_vsg_container).setVisibility(View.VISIBLE);
            getSupportActionBar().setShowHideAnimationEnabled(false);
            getSupportActionBar().hide();
            fullScreen(true);
            recordManager.onSwitchToManager();
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
        playManager.onSwitchToManager();
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
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(AssetsDefaultConstants.randomConversationBackground()));
        ((ImageView)findViewById(R.id.conversation_bcg)).setImageBitmap(bitmap);
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
                if (conversation.isGroup){
                    prepareChatGroup();
                }else {
                    prepareChatter();
                }
                playManager = new ConversationViewPlayManager();
                recordManager = new ConversationViewRecordManager();
                playManager.initManager(this);
                recordManager.initManager(this);
                initNotifications();
                showPlayViews();

            }
        }
        generateVessageExtraInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playManager.onPause();
        recordManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playManager.onResume();
        recordManager.onResume();
    }

    private void setConversation(Conversation conversation) {
        this.conversation = conversation.copyToObject();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroupChat()){
            menu.add(Menu.NONE,Menu.FIRST,0,R.string.group_profile).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE,Menu.FIRST,1,R.string.exit_chat_group).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }else {
            menu.add(Menu.NONE,Menu.FIRST,0,R.string.note_conversation).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isQuitedChatGroup(){
        if(isGroupChat()){
            String myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
            for (String chatter : chatGroup.getChatters()) {
                if(chatter.equals(myUserId)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Menu.FIRST:
                if(isGroupChat()){
                    if(isQuitedChatGroup()){
                        Toast.makeText(ConversationViewActivity.this,R.string.u_not_in_group,Toast.LENGTH_SHORT).show();
                    }else {
                        if(item.getOrder() == 0){
                            showGroupProfile();
                        }else if(item.getOrder() == 1){
                            askExitGroup();
                        }
                    }
                }else {
                    showUserProfileAlert(ConversationViewActivity.this,chatter,true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void askExitGroup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationViewActivity.this)
                .setTitle(R.string.ask_exit_chat_group_title)
                .setMessage(getConversationTitle());
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitGroup();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void exitGroup() {
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ConversationViewActivity.this);
        ServicesProvider.getService(ChatGroupService.class).quitChatGroup(chatGroup.groupId, new ChatGroupService.OnQuitChatGroupHandler() {
            @Override
            public void onQuited(ChatGroup quitedChatGroup) {
                hud.dismiss();
                setChatGroup(quitedChatGroup);
            }

            @Override
            public void onQuitError() {
                hud.dismiss();
                Toast.makeText(ConversationViewActivity.this,R.string.quit_chat_group_error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoteConversationDialog() {
        EditPropertyActivity.showEditPropertyActivity(this,REQUEST_CHANGE_NOTE_CODE,R.string.note_conversation,getConversationTitle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHANGE_NOTE_CODE && resultCode == EditPropertyActivity.RESULT_CODE_SAVED_PROPERTY){
            String newNoteName = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
            if(StringHelper.notNullOrEmpty(chatter.userId)){
                ServicesProvider.getService(UserService.class).setUserNoteName(chatter.userId,newNoteName);
            }
            setActivityTitle(newNoteName);
        }

    }

    private void initNotifications() {
        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playManager.onDestroy();
        recordManager.onDestroy();
        ServicesProvider.getService(ChatGroupService.class).deleteObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED,onChatGroupUpdated);
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
    }

    private Observer onChatGroupUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            ChatGroup fetchedGroup = (ChatGroup) state.getInfo();
            setChatGroup(fetchedGroup);
        }
    };

    private void prepareChatGroup() {
        ChatGroupService chatGroupService = ServicesProvider.getService(ChatGroupService.class);
        chatGroupService.addObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED, onChatGroupUpdated);
        ChatGroup storedGroup = chatGroupService.getCachedChatGroup(conversation.chatterId);
        if(storedGroup != null){
            setChatGroup(storedGroup);
        }else {
            ChatGroup tmpGroup = new ChatGroup();
            tmpGroup.groupId = conversation.chatterId;
            setChatGroup(tmpGroup);
        }

        chatGroupService.fetchChatGroup(conversation.chatterId, new ChatGroupService.OnFetchChatGroupHandler() {
            @Override
            public void onFetchedChatGroup(ChatGroup chatGroup) {

            }

            @Override
            public void onFetchChatGroupError() {
                if(chatGroup == null){
                    Toast.makeText(ConversationViewActivity.this,R.string.no_chat_group_found,Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void setChatGroup(ChatGroup chatGroup) {
        this.chatGroup = chatGroup.copyToObject();
        if(playManager!=null){
            playManager.onChatGroupUpdated();
        }
        if(recordManager != null){
            recordManager.onChatGroupUpdated();
        }
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
            }
        }
    };

    private void setChatter(VessageUser user) {
        this.chatter = user.copyToObject();
        if(playManager!=null){
            playManager.onChatterUpdated();
        }
        if(recordManager != null){
            recordManager.onChatterUpdated();
        }
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

    private void showGroupProfile() {
        List<String> userIds = new ArrayList<String>();
        for (String userId : chatGroup.getChatters()) {
            userIds.add(userId);
        }
        new UsersListActivity
                .ShowUserListActivityBuilder(this)
                .setRemoveMyProfile(false)
                .setTitle(getConversationTitle())
                .setUserIdList(userIds)
                .setOnClickUserItemHandler(new UsersListActivity.OnClickUserItem() {
                    @Override
                    public void onClickUserItem(UsersListActivity sender,VessageUser user) {
                        showUserProfileAlert(sender,user,false);
                    }
                })
                .showActivity();
    }

    private void showUserProfileAlert(Context context,VessageUser user,boolean canNoteUser){
        String msg;
        if(StringHelper.isNullOrEmpty(user.accountId)){
            msg = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
        }else {
            msg = LocalizedStringHelper.getLocalizedString(R.string.account) + ":" + user.accountId;
        }
        String title = ServicesProvider.getService(UserService.class).getUserNoteName(user.userId);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg);
        if (canNoteUser){
            builder.setPositiveButton(R.string.note_conversation, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showNoteConversationDialog();
                        }
                    });
        }
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

    public static void openConversation(Context context, String userId){
        MobclickAgent.onEvent(context,"Vege_OpenConversation");
        Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(userId);
        openConversationView(context,conversation);
    }
}
