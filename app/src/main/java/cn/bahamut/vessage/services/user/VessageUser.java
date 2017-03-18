package cn.bahamut.vessage.services.user;

import org.apache.commons.codec1.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.StringHelper;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/1.
 */
public class VessageUser extends RealmObject{

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_SUBSCRIPTION = 1;

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

    public long acTs = 0;

    public int t = TYPE_NORMAL;

    @Ignore
    public double[] location;

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
        user.sex = this.sex;
        user.acTs = this.acTs;
        user.location = this.location;
        user.t = this.t;
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

    public String getUserId() {
        return userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getNickName() {
        return nickName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getMotto() {
        return motto;
    }

    public String getMainChatImage() {
        return mainChatImage;
    }

    public String getAvatar() {
        return avatar;
    }

    public double[] getLocation() {
        return location;
    }

    public int getSex() {
        return sex;
    }

    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public long getAcTs() {
        return acTs;
    }

    public void setRealmUnSupportProperties(JSONObject jsonObject) {

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("location");
            location = new double[]{jsonArray.getDouble(0), jsonArray.getDouble(1)};
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
