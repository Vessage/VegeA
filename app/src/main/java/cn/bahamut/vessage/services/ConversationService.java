package cn.bahamut.vessage.services;

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
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.annotations.Ignore;

/**
 * Created by alexchow on 16/3/30.
 */

public class ConversationService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    public static final String NOTIFY_CONVERSATION_LIST_UPDATED = "NOTIFY_CONVERSATION_LIST_UPDATED";
    public static final String NOTIFY_CONVERSATION_UPDATED = "NOTIFY_CONVERSATION_UPDATED";
    public Conversation openConversation(String conversationId){
        return getTestConversation();
        //Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("conversationId",conversationId).findFirst();
        //return conversation;
    }

    public Conversation openConversationByMobile(String mobile){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("mobile",mobile).findFirstAsync();
        if(conversation == null){
            Realm.getDefaultInstance().beginTransaction();
            conversation = Realm.getDefaultInstance().createObject(Conversation.class);
            conversation.conversationId = IDUtil.generateUniqueId();
            conversation.chatterMobile = mobile;
            conversation.noteName = mobile;
            conversation.sLastMessageTime = new Date();
            Realm.getDefaultInstance().commitTransaction();
        }
        return conversation;
    }

    public Conversation openConversationByUser(VessageUser user){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("chatterId",user.userId).findFirstAsync();
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

    public List<Conversation> getAllConversations(){
        RealmResults<Conversation> results = Realm.getDefaultInstance().where(Conversation.class).findAllAsync();
        //return results;
        List<Conversation> list = new ArrayList<>();
        list.add(getTestConversation());
        return list;
    }

    private Conversation getTestConversation(){
        Conversation conversation = new Conversation();
        conversation.conversationId = "abc";
        conversation.noteName = "Y";
        conversation.chatterMobile = "15800038672";
        conversation.sLastMessageTime = new Date();
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().copyToRealmOrUpdate(conversation);
        Realm.getDefaultInstance().commitTransaction();
        return conversation;
    }

    public void setConversationNoteName(String conversationId,String noteName){
        //TODO:
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
