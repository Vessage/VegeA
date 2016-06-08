package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/6/8.
 */
public class RegistMobileUserRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    protected String getApi() {
        return "/VessageUsers/NewMobileUser";
    }

    public void setMobile(String mobile){
        putParameter("mobile",mobile);
    }
}
