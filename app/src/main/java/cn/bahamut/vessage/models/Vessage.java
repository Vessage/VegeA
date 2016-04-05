package cn.bahamut.vessage.models;

import cn.bahamut.common.BahamutObject;
import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/1.
 */
public class Vessage extends BahamutObject{
    public String vessageId;
    public String fileId;
    public String sender;
    public boolean isRead = false;
    public String sendTime;
    public String extraInfo;
}
