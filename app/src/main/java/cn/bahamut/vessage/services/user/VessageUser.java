package cn.bahamut.vessage.services.user;

import org.apache.commons.codec1.digest.DigestUtils;

import java.util.Date;

import cn.bahamut.common.StringHelper;
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
            if (!StringHelper.isNullOrEmpty(userA.userId) && !StringHelper.isNullOrEmpty(userB.userId)){
                return userA.userId.equals(userB.userId);
            }
            if (!StringHelper.isNullOrEmpty(userA.mobile) && !StringHelper.isNullOrEmpty(userB.mobile)){
                if (userA.mobile.equals(userB.mobile) || DigestUtils.md5Hex(userA.mobile).equals(userB.mobile) || DigestUtils.md5Hex(userB.mobile).equals(userA.mobile)){
                    return true;
                }
            }
        }
        return false;
    }
}
