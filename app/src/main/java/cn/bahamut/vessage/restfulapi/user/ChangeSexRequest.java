package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2017/2/9.
 */

public class ChangeSexRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    public String getApi() {
        return "/VessageUsers/SexValue";
    }

    public void setSex(int sex) {
        putParameter("value", String.valueOf(sex));
    }
}
