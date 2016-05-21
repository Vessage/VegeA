package cn.bahamut.vessage.activities.littlepaper.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.DateHelper;
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
    public String postmenString;
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
        if(postmenString == null){
            return false;
        }
        return postmenString.contains(myUserId);
    }

    public boolean isMyOpened(String myUserId){
        return StringHelper.notStringNullOrWhiteSpace(receiver) && myUserId.equals(receiver);
    }

    public Date getUpdatedTime(){
        return DateHelper.stringToAccurateDate(updatedTime);
    }

    public void reSetPostMenFromJsonObject(JSONObject object) {

        try {
            StringBuilder postmenStringBuilder = new StringBuilder();
            JSONArray postmenArray = object.getJSONArray("postmen");
            for (int pi = 0; pi < postmenArray.length(); pi++) {
                postmenStringBuilder.append(postmenArray.getString(pi));
                postmenStringBuilder.append(';');
            }
            this.postmenString = postmenStringBuilder.toString();
        } catch (JSONException e) {

        }

    }
}