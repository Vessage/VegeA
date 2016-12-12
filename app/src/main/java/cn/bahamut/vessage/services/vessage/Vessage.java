package cn.bahamut.vessage.services.vessage;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.JsonHelper;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/1.
 */
public class Vessage extends RealmObject{
    public static final int TYPE_NO_VESSAGE = -2;
    public static final int TYPE_UNKNOW = -1;
    public static final int TYPE_CHAT_VIDEO = 0;
    public static final int TYPE_FACE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_LITTLE_VIDEO = 3;

    public static final int MARK_VG_RANDOM_VESSAGE = 1;
    public static final int MARK_MY_SENDING_VESSAGE = 2;

    @PrimaryKey
    public String vessageId;
    public String fileId;
    public String sender; //groupid if is group vessage
    public boolean isRead = false;
    public long ts = 0;
    public String extraInfo;
    public boolean isGroup = false;
    public String body;
    public int typeId;

    public String gSender; //vessage sender of group if is group vessage

    @Ignore
    public int mark = 0;

    public VessageExtraInfoModel getExtraInfoModel() {
        return new Gson().fromJson(extraInfo,VessageExtraInfoModel.class);
    }

    public JSONObject getExtraInfo(){
        try {
            return new JSONObject(extraInfo);
        } catch (JSONException e) {
            return null;
        }
    }

    public void setValuesByOther(Vessage vessage){
        this.typeId = vessage.typeId;
        this.fileId = vessage.fileId;
        this.extraInfo = vessage.extraInfo;
        this.isGroup = vessage.isGroup;
        this.body = vessage.body;
        this.isRead = vessage.isRead;
        this.sender = vessage.sender;
        this.ts = vessage.ts;
        this.gSender = vessage.gSender;
        this.mark = vessage.mark;
    }

    public Vessage copyToObject() {
        Vessage vsg = new Vessage();
        vsg.vessageId = this.vessageId;
        vsg.typeId = this.typeId;
        vsg.fileId =this.fileId;
        vsg.extraInfo = this.extraInfo;
        vsg.isGroup = this.isGroup;
        vsg.body = this.body;
        vsg.isRead = this.isRead;
        vsg.sender = this.sender;
        vsg.ts = this.ts;
        vsg.mark = this.mark;
        vsg.gSender = this.gSender;
        return vsg;
    }

    public String getVessageRealSenderId() {
        if (isGroup){
            return gSender;
        }
        return sender;
    }

    public JSONObject getBodyJsonObject() {
        try {
            JSONObject body = new JSONObject(this.body);
            return body;
        } catch (JSONException e) {
            return null;
        }
    }

    public boolean isMySendingVessage() {
        return mark == MARK_MY_SENDING_VESSAGE;
    }

    public boolean isNormalVessage() {
        return mark == 0;
    }

    static public class VessageExtraInfoModel{
        public String accountId;
        public String nickName;
    }
}
