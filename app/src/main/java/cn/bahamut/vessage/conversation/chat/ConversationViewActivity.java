package cn.bahamut.vessage.conversation.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import cn.bahamut.vessage.usersettings.UpdateChatImageActivity;

public class ConversationViewActivity extends AppCompatActivity {
    private Conversation conversation;

    private ChatGroup chatGroup;


    private int outterVessageCount = 0;

    PlayVessageManager playManager;

    ConversationViewManagerBase currentManager;

    private GestureDetector gestureDetector;

    boolean isGroupChat() {
        return conversation.type == Conversation.TYPE_GROUP_CHAT;
    }

    ChatGroup getChatGroup() {
        return chatGroup;
    }

    Conversation getConversation() {
        return conversation;
    }

    protected String getConversationTitle() {
        String outterPrefix = outterVessageCount > 0 ? String.format("(%d)", outterVessageCount) : "";
        String titileSubfix;
        switch (conversation.type) {
            case Conversation.TYPE_GROUP_CHAT:
                titileSubfix = chatGroup.groupName;
                break;
            case Conversation.TYPE_SINGLE_CHAT:
                titileSubfix = ServicesProvider.getService(UserService.class).getUserNoteOrNickName(conversation.chatterId);
                break;
            case Conversation.TYPE_MULTI_CHAT:
                titileSubfix = LocalizedStringHelper.getLocalizedString(R.string.mutil_conversation);
                break;
            default:
                return LocalizedStringHelper.getLocalizedString(R.string.nameless_conversation);
        }
        return String.format("%s%s", outterPrefix, titileSubfix);
    }

    private void setConversation(Conversation conversation) {
        this.conversation = conversation.copyToObject();
    }

    private void incOutterVessageCount(int inc) {
        outterVessageCount += inc;
        setActivityTitle(getConversationTitle());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_conversation_view);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(AssetsDefaultConstants.randomConversationBackground()));
        ((ImageView) findViewById(R.id.conversation_bcg)).setImageBitmap(bitmap);
        String conversationId = getIntent().getStringExtra("conversationId");
        if (conversationId == null) {
            finish();
            Toast.makeText(this, R.string.no_conversation, Toast.LENGTH_SHORT).show();
        } else {
            Conversation storeConversation = ServicesProvider.getService(ConversationService.class).openConversation(conversationId);
            if (storeConversation == null) {
                finish();
                Toast.makeText(this, R.string.no_conversation, Toast.LENGTH_SHORT).show();
            } else {
                setConversation(storeConversation);
                if (isGroupChat()) {
                    prepareChatGroup();
                } else {
                    ChatGroup tmpGroup = new ChatGroup();
                    tmpGroup.groupId = conversation.chatterId;
                    tmpGroup.setChatter(new String[]{conversation.chatterId, UserSetting.getUserId()});
                    setChatGroup(tmpGroup);
                }
                playManager = new PlayVessageManager();
                playManager.initManager(this);
                initNotifications();
                initGestures();
                playManager.onSwitchToManager();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentManager.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        currentManager.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        currentManager.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroupChat()) {
            menu.add(Menu.NONE, Menu.FIRST, 0, R.string.group_profile).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add(Menu.NONE, Menu.FIRST, 0, R.string.note_conversation).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean isQuitedChatGroup() {
        if (isGroupChat()) {
            String myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
            for (String chatter : chatGroup.getChatters()) {
                if (chatter.equals(myUserId)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                if (isGroupChat()) {
                    if (isQuitedChatGroup()) {
                        Toast.makeText(ConversationViewActivity.this, R.string.u_not_in_group, Toast.LENGTH_SHORT).show();
                    } else {
                        if (item.getOrder() == 0) {
                            showGroupProfile();
                        }
                    }
                } else {
                    VessageUser chatter = ServicesProvider.getService(UserService.class).getUserById(conversation.chatterId);
                    AppMain.showUserProfileAlert(ConversationViewActivity.this, chatter, new AppMain.UserProfileAlertNoteUserHandler() {
                        @Override
                        public void handle() {
                            showNoteConversationDialog();
                        }
                    });
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        playManager.onConfigurationChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!currentManager.onActivityResult(requestCode, resultCode, data)) {
            if (requestCode == ActivityRequestCode.REQUEST_CHANGE_NOTE_CODE && resultCode == EditPropertyActivity.RESULT_CODE_SAVED_PROPERTY) {
                String newNoteName = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
                if (StringHelper.notNullOrEmpty(conversation.chatterId)) {
                    ServicesProvider.getService(UserService.class).setUserNoteName(conversation.chatterId, newNoteName);
                }
                setActivityTitle(newNoteName);
            }
        }
    }

    private void showNoteConversationDialog() {
        EditPropertyActivity.showEditPropertyActivity(this, ActivityRequestCode.REQUEST_CHANGE_NOTE_CODE, R.string.note_conversation, getConversationTitle());
    }

    private void initNotifications() {
        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onNewVessagePushed);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SENDED_VESSAGE, onSendVessage);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SENDING_PROGRESS, onSendVessage);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SEND_VESSAGE_FAILURE, onSendVessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playManager != null){
            playManager.onDestroy();
        }
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onNewVessagePushed);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SENDED_VESSAGE, onSendVessage);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SEND_VESSAGE_FAILURE, onSendVessage);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SENDING_PROGRESS, onSendVessage);
        ServicesProvider.getService(ChatGroupService.class).deleteObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED, onChatGroupUpdated);
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
    }

    private Observer onNewVessagePushed = new Observer() {
        @Override
        public void update(ObserverState state) {
            SendVessageQueueTask task = (SendVessageQueueTask) state.getInfo();
            playManager.pushSendingVessage(task.vessage);
        }
    };

    private Observer onSendVessage = new Observer() {
        @Override
        public void update(ObserverState state) {
            SendVessageQueue.SendingTaskInfo info = (SendVessageQueue.SendingTaskInfo) state.getInfo();
            if (info.task.receiverId.equals(conversation.chatterId)) {
                if (info.state < 0) {
                    setSendingProgressSendFaiure();
                } else if (info.state == SendVessageQueue.SendingTaskInfo.STATE_SENDED) {
                    setSendingProgressSended();
                } else if (info.state == SendVessageQueue.SendingTaskInfo.STATE_SENDING) {
                    setSendingProgress((float) info.progress);
                }
            }
        }
    };

    private Observer onChatGroupUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            ChatGroup fetchedGroup = (ChatGroup) state.getInfo();
            setChatGroup(fetchedGroup);
        }
    };

    private void prepareChatGroup() {
        ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        ChatGroupService chatGroupService = ServicesProvider.getService(ChatGroupService.class);
        chatGroupService.addObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED, onChatGroupUpdated);
        ChatGroup storedGroup = chatGroupService.getCachedChatGroup(conversation.chatterId);
        if (storedGroup != null) {
            setChatGroup(storedGroup);
        } else {
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
                if (chatGroup == null) {
                    Toast.makeText(ConversationViewActivity.this, R.string.no_chat_group_found, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void setChatGroup(ChatGroup chatGroup) {
        this.chatGroup = chatGroup.copyToObject();
        if (playManager != null) {
            playManager.onChatGroupUpdated();
        }
    }

    private Observer onVessageUserUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser) state.getInfo();
            for (String chatter : chatGroup.getChatters()) {
                if (chatter.equals(user.userId)) {
                    playManager.onGroupedChatterUpdated(user);
                }
            }
        }
    };

    private Observer onNewVessagesReceived = new Observer() {
        @Override
        public void update(ObserverState state) {
            int outter = 0;
            List<Vessage> vsgs = (List<Vessage>) state.getInfo();
            List<Vessage> receivedVsgs = new ArrayList<>();
            for (Vessage vsg : vsgs) {
                if (vsg.sender.equals(conversation.chatterId)) {
                    receivedVsgs.add(vsg);
                } else {
                    outter++;
                }
            }
            incOutterVessageCount(outter);
            playManager.onVessagesReceived(receivedVsgs);
        }
    };

    public void setActivityTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void askUploadChatBcg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationViewActivity.this);
        builder.setTitle(R.string.need_upload_chat_bcg_title);
        builder.setMessage(R.string.need_upload_chat_bcg_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ConversationViewActivity.this, UpdateChatImageActivity.class);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showGroupProfile() {
        ChatGroupProfileActivity.showChatGroupProfileActivity(this, chatGroup);
    }

    public void startSendingProgress() {
        playManager.sending(1);
    }

    private void setSendingProgress(float progress) {
        int p = (int) (100 * progress);
        playManager.sending(p);
    }

    private void setSendingProgressSendFaiure() {
        playManager.sending(-1);
    }

    private void setSendingProgressSended() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playManager.sending(101);
            }
        }, 2000);
        playManager.sending(100);
    }

    private void initGestures() {
        gestureDetector = new GestureDetector(this, onGestureListener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (super.dispatchTouchEvent(event)) {
            return true;
        } else if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
            return true;
        }
        return false;
    }

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (currentManager instanceof VessageGestureHandler) {
                return ((VessageGestureHandler) currentManager).onTapUp();
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (currentManager instanceof VessageGestureHandler) {
                float minMove = 180;        //最小滑动距离
                float minVelocity = 0;     //最小滑动速度
                float beginX = e1.getX();
                float endX = e2.getX();
                float beginY = e1.getY();
                float endY = e2.getY();

                if (beginX - endX > minMove && Math.abs(velocityX) > minVelocity) {  //左滑
                    ((VessageGestureHandler) currentManager).onFling(VessageGestureHandler.FlingDerection.LEFT, velocityX, velocityY);
                    Log.i("SWIPE", velocityX + "左滑");
                } else if (endX - beginX > minMove && Math.abs(velocityX) > minVelocity) {  //右滑
                    ((VessageGestureHandler) currentManager).onFling(VessageGestureHandler.FlingDerection.RIGHT, velocityX, velocityY);
                    Log.i("SWIPE", velocityX + "右滑");
                } else if (beginY - endY > minMove && Math.abs(velocityY) > minVelocity) {  //上滑
                    ((VessageGestureHandler) currentManager).onFling(VessageGestureHandler.FlingDerection.UP, velocityX, velocityY);
                    Log.i("SWIPE", velocityY + "上滑");
                } else if (endY - beginY > minMove && Math.abs(velocityY) > minVelocity) {  //下滑
                    ((VessageGestureHandler) currentManager).onFling(VessageGestureHandler.FlingDerection.DOWN, velocityX, velocityY);
                    Log.i("SWIPE", velocityY + "下滑");
                }
            }

            return false;
        }
    };

    public static void openConversationView(Context context, Conversation conversation) {
        MobclickAgent.onEvent(context, "Vege_OpenConversation");
        Intent intent = new Intent();
        intent.putExtra("conversationId", conversation.conversationId);
        intent.setClass(context, ConversationViewActivity.class);
        context.startActivity(intent);
    }

    public static void openConversationView(Context context, String conversationId, int flags) {
        MobclickAgent.onEvent(context, "Vege_OpenConversation");
        Intent intent = new Intent();
        intent.putExtra("conversationId", conversationId);
        intent.setFlags(flags);
        intent.setClass(context, ConversationViewActivity.class);
        context.startActivity(intent);
    }

    public static void openConversation(Context context, String userId) {
        if (userId.equals(UserSetting.getUserId())) {
            Toast.makeText(context, R.string.cant_chat_with_self, Toast.LENGTH_SHORT).show();
            return;
        }
        MobclickAgent.onEvent(context, "Vege_OpenConversation");
        Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(userId);
        openConversationView(context, conversation);
    }
}
