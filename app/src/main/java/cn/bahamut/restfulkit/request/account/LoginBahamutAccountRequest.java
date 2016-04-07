package cn.bahamut.restfulkit.request.account;

import org.apache.commons.codec.digest.DigestUtils;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/7.
 */
public class LoginBahamutAccountRequest extends BahamutRequestBase {

    private String loginApi;

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    public String getApiUrl() {
        return loginApi;
    }

    public void setAccountString(String accountString){
        putParameter("username",accountString);
    }

    public void setPassword(String password){
        DigestUtils.sha256Hex(password);
        putParameter("password",password);
    }

    public void setLoginApi(String loginApi) {
        this.loginApi = loginApi;
    }
}
