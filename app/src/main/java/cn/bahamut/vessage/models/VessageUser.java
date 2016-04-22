package cn.bahamut.vessage.models;

import org.apache.commons.codec1.digest.DigestUtils;
import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.BahamutObject;
import cn.bahamut.common.StringHelper;
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

    public static boolean isTheSameUser(VessageUser userA,VessageUser userB){
        if(userA != null && userB != null){
            if (!StringHelper.isStringNullOrEmpty(userA.userId) && !StringHelper.isStringNullOrEmpty(userB.userId) && userA.userId.equals(userB.userId)){
                return true;
            }
            if (!StringHelper.isStringNullOrEmpty(userA.mobile) && !StringHelper.isStringNullOrEmpty(userB.mobile)){
                if (userA.mobile.equals(userB.mobile) || DigestUtils.md5Hex(userA.mobile).equals(userB.mobile) || DigestUtils.md5Hex(userB.mobile).equals(userA.mobile)){
                    return true;
                }
            }
        }
        return false;
    }
}
