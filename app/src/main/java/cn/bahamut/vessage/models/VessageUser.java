package cn.bahamut.vessage.models;

import java.util.Date;

import cn.bahamut.common.BahamutObject;
import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/1.
 */
public class VessageUser extends BahamutObject {
    public String userId;
    public String nickName;
    public String motto;

    public String accountId;
    public String mainChatImage;
    public String avatar;
    public String mobile;

    public Date lastUpdatedTime;
}
