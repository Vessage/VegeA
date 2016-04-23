package cn.bahamut.vessage.services;

import org.apache.commons.codec1.digest.DigestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.IDUtil;
import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Ignore;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    public static final String NOTIFY_CONVERSATION_LIST_UPDATED = "NOTIFY_CONVERSATION_LIST_UPDATED";
    public static final String NOTIFY_CONVERSATION_UPDATED = "NOTIFY_CONVERSATION_UPDATED";
    public Conversation openConversation(String conversationId){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("conversationId",conversationId).findFirst();
        return conversation;
    }

    public Conversation openConversationVessageInfo(String chatterId,String mobileHash, String nickName){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("chatterMobileHash",mobileHash).findFirst();
        if(conversation == null) {
            Realm.getDefaultInstance().beginTransaction();
            conversation = Realm.getDefaultInstance().createObject(Conversation.class);
            conversation.chatterId = chatterId;
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.chatterMobileHash = mobileHash;
            conversation.noteName = nickName;
            conversation.sLastMessageTime = new Date();
            Realm.getDefaultInstance().commitTransaction();
        }
        return conversation;
    }

    public Conversation openConversationByMobile(String mobile){
        return openConversationByMobile(mobile,null);
    }

    public Conversation openConversationByMobile(String mobile,String nickName){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("chatterMobile",mobile).findFirst();
        if(conversation == null) {
            Realm.getDefaultInstance().beginTransaction();
            conversation = Realm.getDefaultInstance().createObject(Conversation.class);
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.chatterMobile = mobile;
            conversation.chatterMobileHash = DigestUtils.md5Hex(mobile);
            conversation.noteName = nickName == null ? mobile : nickName;
            conversation.sLastMessageTime = new Date();
            Realm.getDefaultInstance().commitTransaction();
        }
        return conversation;
    }

    public Conversation openConversationByUser(VessageUser user){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("chatterId",user.userId).findFirst();
        if(conversation == null){
            Realm.getDefaultInstance().beginTransaction();
            conversation = Realm.getDefaultInstance().createObject(Conversation.class);
            conversation.sLastMessageTime = new Date();
            conversation.noteName = user.nickName;
            conversation.chatterId = user.userId;
            conversation.conversationId = IDUtil.generateUniqueId();
            Realm.getDefaultInstance().commitTransaction();
        }
        return conversation;
    }

    public List<Conversation> searchConversations(String keyword){
        RealmResults<Conversation> results = Realm.getDefaultInstance().where(Conversation.class)
                .contains("noteName",keyword, Case.INSENSITIVE).or()
                .equalTo("chatterMobile",keyword)
                .findAllSorted("sLastMessageTime", Sort.DESCENDING);
        return results;
    }

    public List<Conversation> getAllConversations(){
        RealmResults<Conversation> results = Realm.getDefaultInstance().where(Conversation.class).findAllSorted("sLastMessageTime", Sort.DESCENDING);
        return results;
    }

    public void setConversationNoteName(String conversationId,String noteName){
        Conversation conversation = openConversation(conversationId);
        if(conversation != null){
            Realm.getDefaultInstance().beginTransaction();
            conversation.noteName = noteName;
            Realm.getDefaultInstance().commitTransaction();
            ObserverState state = new ObserverState();
            state.setNotifyType(NOTIFY_CONVERSATION_UPDATED);
            state.setInfo(conversation);
            postNotification(state);
        }
    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(ConversationService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }
}
