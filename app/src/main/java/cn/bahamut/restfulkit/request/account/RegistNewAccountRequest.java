package cn.bahamut.restfulkit.request.account;

import org.apache.commons.codec.digest.DigestUtils;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/7.
 */
public class RegistNewAccountRequest extends BahamutRequestBase {
    private String registApi;

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    public String getApiUrl() {
        return registApi;
    }

    public void setAccountName(String accountName){
        putParameter("username",accountName);
    }

    public void setPhone(String phone){
        putParameter("phone_number",phone);
    }

    public void setEmail(String email){
        putParameter("email",email);
    }

    public void setPassword(String password){
        DigestUtils.sha256Hex(password);
        putParameter("password",password);
    }

    public void setRegistApi(String registApi) {
        this.registApi = registApi;
    }
}
