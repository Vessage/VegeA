package cn.bahamut.vessage.services.conversation;

import java.util.Date;

import cn.bahamut.vessage.services.vessage.Vessage;
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
    public long lstTs = 0;
    public boolean isPinned = false;
    public int type = TYPE_SINGLE_CHAT;

    public String activityId;

    public boolean isInConversation(Vessage vessage) {
        return vessage.sender.equals(chatterId);
    }

    public Conversation copyToObject() {
        Conversation conversation = new Conversation();
        conversation.chatterId = this.chatterId;
        conversation.conversationId = this.conversationId;
        conversation.activityId = this.activityId;
        conversation.lstTs = this.lstTs;
        conversation.type = this.type;
        conversation.isPinned = this.isPinned;
        return conversation;
    }

    public static final long MAX_LEFT_TIME_MS = 14 * 24 * 3600 * 1000;
    public static final long MAX_LEFT_TIME_MIN = 14 * 24 * 60;

    public float getTimeUpProgress() {
        long leftMins = getTimeUpMinutesLeft();
        if (leftMins > 1) {
            return 1.0f * leftMins / MAX_LEFT_TIME_MIN;
        } else {
            return 0;
        }
    }

    public long getTimeUpMinutesLeft() {

        Date expireDate = new Date(lstTs + MAX_LEFT_TIME_MS);
        long left = expireDate.getTime() - new Date().getTime();
        return left / (1000 * 60);
    }
}