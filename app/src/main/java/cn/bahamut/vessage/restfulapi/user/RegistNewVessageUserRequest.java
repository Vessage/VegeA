package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class RegistNewVessageUserRequest extends BahamutRequestBase{
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    public String getApi() {
        return "/NewUsers";
    }

    public void setRegion(String region) {
        putParameter("region", region);
    }

    public void setNickName(String nickName) {
        putParameter("nickName", nickName);
    }

    public void setMotto(String motto){
        putParameter("motto", motto);
    }

    public void setAppkey(String appkey) {
        putParameter("appkey", appkey);
    }

    public void setAccountId(String accountId){
        putParameter("accountId", accountId);
    }

    public void setAccessToken(String accessToken){
        putParameter("accessToken", accessToken);
    }
}
