package cn.bahamut.vessage.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.IDUtil;
import cn.bahamut.observer.Observable;
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
    public Conversation openConversation(String conversationId){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("conversationId",conversationId).findFirst();
        return conversation;
    }

    public Conversation openConversationByMobile(String mobile){
        Conversation conversation = Realm.getDefaultInstance().where(Conversation.class).equalTo("mobile",mobile).findFirst();
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

    public List<Conversation> getAllConversations(){
        RealmResults<Conversation> results = Realm.getDefaultInstance().where(Conversation.class).findAll();
        return results;
    }

    public void setConversationNoteName(String conversationId,String noteName){
        //TODO:
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
