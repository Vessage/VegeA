package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2017/2/9.
 */

public class ChangeMottoRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    public String getApi() {
        return "/VessageUsers/Motto";
    }

    public void setMotto(String motto) {
        putParameter("motto", motto);
    }
}
