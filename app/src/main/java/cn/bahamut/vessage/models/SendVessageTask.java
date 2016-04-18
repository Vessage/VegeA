package cn.bahamut.vessage.models;

import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/18.
 */
public class SendVessageTask extends RealmObject {
    public String vessageId;
    public String vessageBoxId;
    public String videoPath;
}
