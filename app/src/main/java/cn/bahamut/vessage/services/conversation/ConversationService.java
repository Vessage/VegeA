package cn.bahamut.vessage.services.conversation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.IDUtil;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    public static final long MAX_PIN_CONVERSATION_LIMIT = 6;
    public static final String NOTIFY_CONVERSATION_LIST_UPDATED = "NOTIFY_CONVERSATION_LIST_UPDATED";
    private Realm realm;

    public Conversation openConversation(String conversationId){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("conversationId",conversationId).findFirst();
        return conversation;
    }

    public Conversation openConversationVessageInfo(String chatterId,/*String mobileHash,*/ boolean isGroup){
        Conversation conversation = getRealm().where(Conversation.class)
                .equalTo("chatterId",chatterId)
                //.or()
                //.equalTo("chatterMobileHash",mobileHash)
                .findFirst();
        if(conversation == null) {
            getRealm().beginTransaction();
            conversation = getRealm().createObject(Conversation.class);
            conversation.chatterId = chatterId;
            conversation.conversationId = IDUtil.generateUniqueId();
            //conversation.chatterMobileHash = mobileHash;
            conversation.lstTs = DateHelper.getUnixTimeSpan();
            conversation.type = isGroup ? Conversation.TYPE_GROUP_CHAT : Conversation.TYPE_SINGLE_CHAT;
            getRealm().commitTransaction();
        }
        return conversation;
    }

    public Conversation openConversationByGroup(ChatGroup group){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("chatterId",group.groupId).findFirst();
        if(conversation == null){
            getRealm().beginTransaction();
            conversation = getRealm().createObject(Conversation.class);
            conversation.lstTs = DateHelper.getUnixTimeSpan();
            conversation.chatterId = group.groupId;
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.type = Conversation.TYPE_GROUP_CHAT;
            getRealm().commitTransaction();
        }
        return conversation;
    }

    public Conversation openConversationByUserInfo(String userId){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("chatterId",userId).findFirst();
        if(conversation == null){
            getRealm().beginTransaction();
            conversation = getRealm().createObject(Conversation.class);
            conversation.lstTs = DateHelper.getUnixTimeSpan();
            conversation.chatterId = userId;
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.type = Conversation.TYPE_SINGLE_CHAT;
            getRealm().commitTransaction();
        }
        return conversation;
    }

    public Conversation getConversationByChatterId(String chatterId){
        if(StringHelper.isNullOrEmpty(chatterId)){
            return null;
        }
        return getRealm().where(Conversation.class).equalTo("chatterId",chatterId).findFirst();
    }

    public List<Conversation> searchConversations(String keyword){
        RealmResults<Conversation> results = getRealm().where(Conversation.class)
                .equalTo("chatterMobile",keyword)
                .findAllSorted("lstTs", Sort.DESCENDING);
        return results;
    }

    public List<Conversation> getAllConversations(){
        RealmResults<Conversation> results = getRealm().where(Conversation.class).findAllSorted("lstTs", Sort.DESCENDING);
        return results;
    }

    public boolean canPinMoreConversation() {
        return getRealm().where(Conversation.class).equalTo("isPinned", true).count() < MAX_PIN_CONVERSATION_LIMIT;
    }

    @Override
    public void onUserLogin(String userId) {
        realm = Realm.getDefaultInstance();
        ServicesProvider.setServiceReady(ConversationService.class);
    }

    @Override
    public void onUserLogout() {
        realm.close();
        realm = null;
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }

    public Realm getRealm() {
        return realm;
    }

    public Set<String> getChattingNormalUserIds() {
        return getChattingConversationChatterIds(Conversation.TYPE_SINGLE_CHAT);
    }

    public Set<String> getChattingConversationChatterIds(int conversationType) {
        Set<String> chatterIds = new HashSet<>();
        for (Conversation conversation : getAllConversations()) {
            if (conversation.type == conversationType){
                chatterIds.add(conversation.chatterId);
            }
        }
        return chatterIds;
    }
}
