package cn.bahamut.vessage.services.conversation;

import org.apache.commons.codec1.digest.DigestUtils;

import java.util.Date;
import java.util.List;

import cn.bahamut.common.IDUtil;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.user.VessageUser;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    public static final String NOTIFY_CONVERSATION_LIST_UPDATED = "NOTIFY_CONVERSATION_LIST_UPDATED";
    public static final String NOTIFY_CONVERSATION_UPDATED = "NOTIFY_CONVERSATION_UPDATED";
    private Realm realm;

    public Conversation openConversation(String conversationId){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("conversationId",conversationId).findFirst();
        return conversation;
    }

    public Conversation openConversationVessageInfo(String chatterId,String mobileHash, String nickName){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("chatterMobileHash",mobileHash).findFirst();
        if(conversation == null) {
            getRealm().beginTransaction();
            conversation = getRealm().createObject(Conversation.class);
            conversation.chatterId = chatterId;
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.chatterMobileHash = mobileHash;
            conversation.noteName = nickName;
            conversation.sLastMessageTime = new Date();
            getRealm().commitTransaction();
        }
        return conversation;
    }
    /*
    public Conversation openConversationByMobile(String mobile){
        return openConversationByMobile(mobile,null);
    }

    public Conversation openConversationByMobile(String mobile,String nickName){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("chatterMobile",mobile).findFirst();
        if(conversation == null) {
            getRealm().beginTransaction();
            conversation = getRealm().createObject(Conversation.class);
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.chatterMobile = mobile;
            conversation.chatterMobileHash = DigestUtils.md5Hex(mobile);
            conversation.noteName = nickName == null ? mobile : nickName;
            conversation.sLastMessageTime = new Date();
            getRealm().commitTransaction();
        }
        return conversation;
    }*/

    public Conversation openConversationByUserInfo(String userId,String nickName){
        Conversation conversation = getRealm().where(Conversation.class).equalTo("chatterId",userId).findFirst();
        if(conversation == null){
            getRealm().beginTransaction();
            conversation = getRealm().createObject(Conversation.class);
            conversation.sLastMessageTime = new Date();
            conversation.noteName = nickName;
            conversation.chatterId = userId;
            conversation.conversationId = IDUtil.generateUniqueId();
            getRealm().commitTransaction();
        }
        return conversation;
    }

    public Conversation getConversationByChatterId(String chatterId){
        if(StringHelper.isStringNullOrEmpty(chatterId)){
            return null;
        }
        return getRealm().where(Conversation.class).equalTo("chatterId",chatterId).findFirst();
    }

    public List<Conversation> searchConversations(String keyword){
        RealmResults<Conversation> results = getRealm().where(Conversation.class)
                .contains("noteName",keyword, Case.INSENSITIVE).or()
                .equalTo("chatterMobile",keyword)
                .findAllSorted("sLastMessageTime", Sort.DESCENDING);
        return results;
    }

    public List<Conversation> getAllConversations(){
        RealmResults<Conversation> results = getRealm().where(Conversation.class).findAllSorted("sLastMessageTime", Sort.DESCENDING);
        return results;
    }

    public void setConversationNoteName(String conversationId,String noteName){
        Conversation conversation = openConversation(conversationId);
        if(conversation != null){
            getRealm().beginTransaction();
            conversation.noteName = noteName;
            getRealm().commitTransaction();
            ObserverState state = new ObserverState();
            state.setNotifyType(NOTIFY_CONVERSATION_UPDATED);
            state.setInfo(conversation);
            postNotification(state);
        }
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
}
