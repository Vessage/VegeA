package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/6/22.
 */
public class GetNearUsersRequest extends BahamutRequestBase{
    @Override
    protected String getApi() {
        return "/VessageUsers/Near";
    }

    public void setLocation(String location){
        putParameter("location",location);
    }
}
