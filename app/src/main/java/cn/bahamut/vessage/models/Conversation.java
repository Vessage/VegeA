package cn.bahamut.vessage.models;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bahamut.common.BahamutObject;
import cn.bahamut.common.DateHelper;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by alexchow on 16/4/1.
 */
public class Conversation extends BahamutObject {
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