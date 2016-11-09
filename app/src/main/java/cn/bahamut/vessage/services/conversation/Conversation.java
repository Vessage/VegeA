package cn.bahamut.vessage.services.conversation;

import java.util.Date;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.vessage.Vessage;
import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/1.
 */
public class Conversation extends RealmObject {
    public static final int TYPE_SINGLE_CHAT = 1;
    public static final int TYPE_GROUP_CHAT = 2;
    public static final int TYPE_MULTI_CHAT = 3;
    public String conversationId;
    public String chatterId;
    public String chatterMobile;
    public String chatterMobileHash;
    //public Date sLastMessageTime;
    public long lstTs = 0;
    //public boolean isGroup = false;
    public boolean isPinned = false;
    public int type = TYPE_SINGLE_CHAT;

    public boolean isInConversation(Vessage vessage) {
        if (vessage.sender.equals(chatterId)) {
            return true;
        }
        if (!StringHelper.isNullOrEmpty(chatterMobileHash) && chatterMobileHash.equals(vessage.getExtraInfoModel().getMobileHash())) {
            if (StringHelper.isNullOrEmpty(chatterId)) {
                Realm realm = ServicesProvider.getService(ConversationService.class).getRealm();
                realm.beginTransaction();
                chatterId = vessage.sender;
                realm.commitTransaction();
            }
            return true;
        }
        return false;
    }

    public Conversation copyToObject() {
        Conversation conversation = new Conversation();
        conversation.chatterId = this.chatterId;
        conversation.chatterMobile = this.chatterMobile;
        conversation.chatterMobileHash = this.chatterMobileHash;
        conversation.conversationId = this.conversationId;
        conversation.lstTs = this.lstTs;
        conversation.type = this.type;
        return conversation;
    }

    static final long maxLeftTimeMs = 14 * 24 * 3600 * 1000;
    static final long maxLeftTimeMin = 14 * 24 * 60;

    public float getTimeUpProgress() {
        long leftMins = getTimeUpMinutesLeft();
        if (leftMins > 1) {
            return 1.0f * leftMins / maxLeftTimeMin;
        } else {
            return 0;
        }
    }

    private long getTimeUpMinutesLeft() {

        Date expireDate = new Date(lstTs + maxLeftTimeMs);
        long left = expireDate.getTime() - new Date().getTime();
        return left / (1000 * 60);
    }
}