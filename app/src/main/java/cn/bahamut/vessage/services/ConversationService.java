package cn.bahamut.vessage.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bahamut.observer.Observable;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by alexchow on 16/3/30.
 */

public class ConversationService extends Observable{

    public static final String NOTIFY_CONVERSATION_LIST_UPDATED = "NOTIFY_CONVERSATION_LIST_UPDATED";
    public Conversation openConversation(String conversationId){
        return null;
    }

    public Conversation openConversationByMobile(String mobile){
        return null;
    }

    public Conversation openConversationByUser(VessageUser user){
        return null;
    }

    public List<Conversation> getAllConversations(){
        return null;
    }
}
