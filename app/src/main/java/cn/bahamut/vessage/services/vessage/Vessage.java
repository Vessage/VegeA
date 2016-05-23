package cn.bahamut.vessage.services.vessage;

import org.json.JSONException;

import cn.bahamut.common.JsonHelper;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/4/1.
 */
public class Vessage extends RealmObject{
    @PrimaryKey
    public String vessageId;
    public String fileId;
    public String sender;
    public boolean isRead = false;
    public String sendTime;
    public String extraInfo;

    public VessageExtraInfoModel getExtraInfoModel() {
        try {
            return JsonHelper.parseObject(extraInfo,VessageExtraInfoModel.class);
        } catch (JSONException e) {
            return null;
        }
    }



    static public class VessageExtraInfoModel{
        private String accountId;
        private String nickName;
        private String mobileHash;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getMobileHash() {
            return mobileHash;
        }

        public void setMobileHash(String mobileHash) {
            this.mobileHash = mobileHash;
        }
    }
}
