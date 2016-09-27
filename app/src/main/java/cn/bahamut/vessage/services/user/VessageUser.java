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

    public int sex;

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

    public VessageUser copyToObject(){
        VessageUser user = new VessageUser();
        user.userId = this.userId;
        user.lastUpdatedTime = this.lastUpdatedTime;
        user.accountId = this.accountId;
        user.avatar = this.avatar;
        user.mainChatImage = this.mainChatImage;
        user.mobile = this.mobile;
        user.motto = this.motto;
        user.nickName = this.nickName;
        return user;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public void setAccountId(String accountId){
        this.accountId = accountId;
    }

    public void setNickName(String nickName){
        this.nickName = nickName;
    }

    public void setMotto(String motto){
        this.motto = motto;
    }

    public void setMainChatImage(String mainChatImage){
        this.mainChatImage = mainChatImage;
    }

    public void setAvatar(String avatar){
        this.avatar = avatar;
    }

    public void setMobile(String mobile){
        this.mobile = mobile;
    }
}
