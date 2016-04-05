package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class SendMobileVSMSRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    public String getApi() {
        return "/VessageUsers/SendMobileVSMS";
    }

    public void setMobile(String mobile){
        putParameter("mobile",mobile);
    }
}
