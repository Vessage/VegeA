package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/5/16.
 */
public class GetActiveUsersRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/VessageUsers/Active";
    }
}
