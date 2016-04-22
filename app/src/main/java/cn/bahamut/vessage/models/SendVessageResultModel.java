package cn.bahamut.vessage.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/8.
 */
public class SendVessageResultModel {

    private String vessageId;
    private String vessageBoxId;

    public String getVessageId() {
        return vessageId;
    }

    public void setVessageId(String vessageId) {
        this.vessageId = vessageId;
    }

    public String getVessageBoxId() {
        return vessageBoxId;
    }

    public void setVessageBoxId(String vessageBoxId) {
        this.vessageBoxId = vessageBoxId;
    }
}
