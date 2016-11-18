package cn.bahamut.vessage.services.groupchat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.StringHelper;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/7/19.
 */
public class ChatGroup extends RealmObject {
    public static final int MAX_USERS_COUNT = 6;
    @PrimaryKey
    public String groupId;
    public String inviteCode;
    public String groupName;

    //Not support primitive properties
    private String chattersString;
    private String hostersString;

    public void setPrimitiveArrayValues(JSONObject originJsonObject){
        try {
            StringBuilder arrStringBuilder = new StringBuilder();
            JSONArray stringArray = originJsonObject.getJSONArray("hosters");
            for (int pi = 0; pi < stringArray.length(); pi++) {
                arrStringBuilder.append(stringArray.getString(pi));
                arrStringBuilder.append(';');
            }
            this.hostersString = arrStringBuilder.toString();
        } catch (JSONException e) {

        }

        try {
            StringBuilder arrStringBuilder = new StringBuilder();
            JSONArray stringArray = originJsonObject.getJSONArray("chatters");
            for (int pi = 0; pi < stringArray.length(); pi++) {
                arrStringBuilder.append(stringArray.getString(pi));
                arrStringBuilder.append(';');
            }
            this.chattersString = arrStringBuilder.toString();
        } catch (JSONException e) {

        }
    }

    public void setHoster(String[] hosters){
        if(hosters == null || hosters.length == 0){
            chattersString = null;
        }else {
            chattersString = StringHelper.stringsJoinSeparator(hosters,";");
        }
        hostersString = StringHelper.stringsJoinSeparator(hosters,";");
    }

    public String[] getHosters(){
        try
        {
            return hostersString.split(";");
        }catch (Exception e){
            return new String[0];
        }
    }

    public void setChatter(String[] chatters){
        if(chatters == null || chatters.length == 0){
            chattersString = null;
        }else {
            chattersString = StringHelper.stringsJoinSeparator(chatters,";");
        }
    }

    public String[] getChatters(){
        try
        {
            return chattersString.split(";");
        }catch (Exception e){
            return new String[0];
        }
    }

    public ChatGroup copyToObject(){
        ChatGroup group = new ChatGroup();
        group.groupId = this.groupId;
        group.inviteCode = this.inviteCode;
        group.groupName = this.groupName;
        group.chattersString = this.chattersString;
        group.hostersString = this.hostersString;
        return group;
    }
}
