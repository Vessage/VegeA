package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class GetUserInfoRequest extends BahamutRequestBase{
    public void setUserId(String userId){
        setApi("/VessageUsers/UserId/" + userId);
    }

    @Override
    public boolean canPostRequest(int inQueueRequestCount) {
        return inQueueRequestCount < 10;
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.GET;
    }
}
