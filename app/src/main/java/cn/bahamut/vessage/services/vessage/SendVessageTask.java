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

    public String toMobile;
}
