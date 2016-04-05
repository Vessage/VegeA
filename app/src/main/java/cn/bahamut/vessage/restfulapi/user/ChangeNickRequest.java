package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class ChangeNickRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    public String getApi() {
        return "/VessageUsers/Nick";
    }

    public void setNick(String newNick) {
        putParameter("nick", newNick);
    }
}
