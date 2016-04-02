package cn.bahamut.vessage.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/1.
 */
public class VessageUser extends RealmObject {
    public String userId;
    public String nickName;
    public String motto;

    public String accountId;
    public String mainChatImage;
    public String avatar;
    public String mobile;

    public Date lastUpdatedTime;
}
