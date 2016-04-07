package cn.bahamut.restfulkit.request.account;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/7.
 */
public class ValidateTokenRequest extends BahamutRequestBase {
    private String tokenApi;

    public void setTokenApi(String tokenApi) {
        this.tokenApi = tokenApi;
    }

    public void setAppkey(String appkey){
        putParameter("appkey",appkey);
    }

    public void setAccessToken(String accessToken){
        putParameter("accessToken",accessToken);
    }

    public void setAccountId(String accountId){
        putParameter("accountId",accountId);
    }

}
