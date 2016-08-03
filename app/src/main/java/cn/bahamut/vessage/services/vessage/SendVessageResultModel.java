package cn.bahamut.vessage.services.vessage;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/8.
 */
public class SendVessageResultModel extends RealmObject {

    @PrimaryKey
    public String vessageId;
    public String vessageBoxId;

    public SendVessageResultModel copyToObject(){
        SendVessageResultModel model = new SendVessageResultModel();
        model.vessageBoxId = vessageBoxId;
        model.vessageId = vessageId;
        return model;
    }
}
