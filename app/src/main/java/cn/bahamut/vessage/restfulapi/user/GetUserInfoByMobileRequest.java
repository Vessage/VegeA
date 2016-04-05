package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class GetUserInfoByMobileRequest extends BahamutRequestBase{
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.GET;
    }

    @Override
    public String getApi() {
        return "/VessageUsers/Mobile";
    }

    public void setMobile(String mobile){
        putParameter("mobile",mobile);
    }
}
