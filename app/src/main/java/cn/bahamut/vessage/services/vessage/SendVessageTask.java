package cn.bahamut.vessage.services.vessage;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/18.
 */
public class SendVessageTask extends RealmObject {
    @PrimaryKey
    public String vessageId;
    public String vessageBoxId;
    public String videoPath;
    public String fileId;

    //public String toMobile; //migration to receiverId
    public String receiverId;
    public boolean isGroup = false;

    public SendVessageTask copyToObject(){
        SendVessageTask taskInfo = new SendVessageTask();
        taskInfo.fileId = this.fileId;
        taskInfo.receiverId = this.receiverId;
        taskInfo.vessageBoxId = this.vessageBoxId;
        taskInfo.vessageId = this.vessageId;
        taskInfo.videoPath = this.videoPath;
        taskInfo.isGroup = this.isGroup;
        return taskInfo;
    }
}