package cn.bahamut.vessage.models;

import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.BahamutObject;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/1.
 */
public class VessageUser extends RealmObject{
    @PrimaryKey
    public String userId;
    public String nickName;
    public String motto;

    public String accountId;
    public String mainChatImage;
    public String avatar;
    public String mobile;

    public Date lastUpdatedTime;

}
