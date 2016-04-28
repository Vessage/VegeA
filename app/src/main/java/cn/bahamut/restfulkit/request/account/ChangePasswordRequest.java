package cn.bahamut.restfulkit.request.account;

import org.apache.commons.codec1.digest.DigestUtils;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/7.
 */
public class ChangePasswordRequest extends BahamutRequestBase{

    private String tokenApi;
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    public String getApiUrl() {
        return tokenApi;
    }

    public void setTokenApi(String tokenApi) {
        this.tokenApi = tokenApi;
    }

    public void setAppkey(String appkey){
        putParameter("appkey",appkey);
    }

    public void setAppToken(String appToken){
        putParameter("appToken",appToken);
    }

    public void setAccountId(String accountId){
        putParameter("accountId",accountId);
    }

    public void setUserId(String userId){
        putParameter("userId",userId);
    }

    public void setOriginPassword(String originPassword){
        String password = DigestUtils.sha256Hex(originPassword);
        putParameter("originPassword",password);
    }

    public void setNewPassword(String newPassword){
        String password = DigestUtils.sha256Hex(newPassword);
        putParameter("newPassword", password);
    }
}
