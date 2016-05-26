package cn.bahamut.vessage.services.conversation;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.vessage.Vessage;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by alexchow on 16/4/1.
 */
public class Conversation extends RealmObject {
    public String conversationId;
    public String chatterId;
    public String chatterMobile;
    public String chatterMobileHash;
    public String noteName;
    public Date sLastMessageTime;

    @Ignore
    private String lastMessageTime;

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
        this.sLastMessageTime = DateHelper.stringToAccurateDate(lastMessageTime);
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public boolean isInConversation(Vessage vessage) {
        if(vessage.sender.equals(chatterId)){
            return true;
        }
        if(!StringHelper.isStringNullOrEmpty(chatterMobileHash) && chatterMobileHash.equals(vessage.getExtraInfoModel().getMobileHash())){
            if(StringHelper.isStringNullOrEmpty(chatterId)){
                Realm realm = ServicesProvider.getService(ConversationService.class).getRealm();
                realm.beginTransaction();
                chatterId = vessage.sender;
                realm.commitTransaction();
            }
            return true;
        }
        return false;
    }
}