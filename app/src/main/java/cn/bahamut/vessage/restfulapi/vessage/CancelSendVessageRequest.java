package cn.bahamut.vessage.restfulapi.vessage;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class CancelSendVessageRequest extends BahamutRequestBase{
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    public String getApi() {
        return "/Vessages/CancelSendVessage";
    }

    public void setVessageId(String vessageId) {
        putParameter("vessageId", vessageId);
    }

    public void setVessageBoxId(String vessageBoxId) {
        putParameter("vessageBoxId", vessageBoxId);
    }
}
