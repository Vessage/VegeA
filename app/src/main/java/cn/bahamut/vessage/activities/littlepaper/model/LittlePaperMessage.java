package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.common.StringHelper;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/5/19.
 */
public class LittlePaperMessage extends RealmObject{
    @PrimaryKey
    public String paperId;
    public String sender;
    public String receiver;
    public String receiverInfo;
    public String message;
    public String[] postmen;
    public String updatedTime;
    public boolean isOpened = false;

    public boolean isUpdated = false;

    public boolean isMySended(String myUserId){
        return StringHelper.notStringNullOrWhiteSpace(sender) && myUserId.equals(sender);
    }

    public boolean isMyReceived(String myUserId) {
        return !isMySended(myUserId);
    }

    public boolean isReceivedNotDeal(String myUserId) {
        return isMyReceived(myUserId) && !isMyOpened(myUserId) && !isMyPosted(myUserId);
    }

    public boolean isMyPosted(String myUserId){
        for (String s : postmen) {
            if(s.equals(myUserId)){
                return true;
            }
        }
        return false;
    }

    public boolean isMyOpened(String myUserId){
        return StringHelper.notStringNullOrWhiteSpace(receiver) && myUserId.equals(receiver);
    }
}
