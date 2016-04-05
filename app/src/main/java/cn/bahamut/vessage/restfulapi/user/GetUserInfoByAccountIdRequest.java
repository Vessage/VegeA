package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class GetUserInfoByAccountIdRequest extends BahamutRequestBase{
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.GET;
    }

    public void setAccountId(String accountId){
        setApi("/VessageUsers/AccountId/"+ accountId);
    }
}
