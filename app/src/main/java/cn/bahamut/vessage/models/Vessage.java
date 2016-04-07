package cn.bahamut.vessage.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/1.
 */
public class Vessage extends RealmObject{

    @PrimaryKey
    public String vessageId;
    public String fileId;
    public String sender;
    public boolean isRead = false;
    public String sendTime;
    public String extraInfo;
}
