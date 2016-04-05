package cn.bahamut.vessage.restfulapi.vessage;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class NotifyGotNewVessagesRequest extends BahamutRequestBase{
    @Override
    public String getApi() {
        return "/Vessages/Got";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }
}
