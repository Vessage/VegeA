package cn.bahamut.vessage.models;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/1.
 */
public class Conversation extends RealmObject {

    @PrimaryKey
    public String conversationId;
    public String chatterId;
    public String chatterMobile;
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
}