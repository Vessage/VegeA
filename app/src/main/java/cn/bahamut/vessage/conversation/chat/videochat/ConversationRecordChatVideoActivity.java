package cn.bahamut.vessage.conversation.chat.videochat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import cn.bahamut.common.ActivityHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;

public class ConversationRecordChatVideoActivity extends AppCompatActivity {

    private RecordChatVideoManager recordManager;
    private ChatGroup chatGroup;
    private Conversation conversation;

    public ChatGroup getChatGroup() {
        return chatGroup;
    }

    @Override
    public void onBackPressed() {
        recordManager.onSwitchOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordManager.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_record_chat_video);
        ActivityHelper.fullScreen(this, true);
        getSupportActionBar().setShowHideAnimationEnabled(false);
        getSupportActionBar().hide();
        String chatterId = getIntent().getStringExtra("chatterId");
        conversation = ServicesProvider.getService(ConversationService.class).getConversationByChatterId(chatterId);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(AssetsDefaultConstants.randomConversationBackground()));
        ((ImageView) findViewById(R.id.conversation_bcg)).setImageBitmap(bitmap);
        if (isGroupChat()) {
            prepareChatGroup();
        } else {
            ChatGroup tmpGroup = new ChatGroup();
            tmpGroup.groupId = conversation.chatterId;
            tmpGroup.setChatter(new String[]{conversation.chatterId, UserSetting.getUserId()});
            setChatGroup(tmpGroup);
        }
        recordManager = new RecordChatVideoManager();
        recordManager.initManager(this);
    }

    private boolean isGroupChat() {
        return conversation.type == Conversation.TYPE_GROUP_CHAT;
    }

    private void prepareChatGroup() {
        ChatGroupService chatGroupService = ServicesProvider.getService(ChatGroupService.class);
        chatGroupService.addObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED, onChatGroupUpdated);
        ChatGroup storedGroup = chatGroupService.getCachedChatGroup(conversation.chatterId);
        if (storedGroup != null) {
            setChatGroup(storedGroup);
        } else {
            ChatGroup tmpGroup = new ChatGroup();
            tmpGroup.groupId = conversation.chatterId;
            setChatGroup(tmpGroup);
            chatGroupService.fetchChatGroup(conversation.chatterId, new ChatGroupService.OnFetchChatGroupHandler() {
                @Override
                public void onFetchedChatGroup(ChatGroup chatGroup) {

                }

                @Override
                public void onFetchChatGroupError() {
                    if (chatGroup == null) {
                        Toast.makeText(ConversationRecordChatVideoActivity.this, R.string.no_chat_group_found, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordManager.onDestroy();
        ServicesProvider.getService(ChatGroupService.class).deleteObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED, onChatGroupUpdated);
    }

    private Observer onChatGroupUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            ChatGroup fetchedGroup = (ChatGroup) state.getInfo();
            setChatGroup(fetchedGroup);
        }
    };

    private void setChatGroup(ChatGroup chatGroup) {
        this.chatGroup = chatGroup.copyToObject();
        if (recordManager != null) {
            recordManager.onChatGroupUpdated();
        }
    }
}
