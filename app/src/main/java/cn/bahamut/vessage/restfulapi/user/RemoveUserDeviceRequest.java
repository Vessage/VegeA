package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/30.
 */
public class RemoveUserDeviceRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.DELETE;
    }

    @Override
    protected String getApi() {
        return "/VessageUsers/UserDevice";
    }
}
