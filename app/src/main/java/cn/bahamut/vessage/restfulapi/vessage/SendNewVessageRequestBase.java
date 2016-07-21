package cn.bahamut.vessage.restfulapi.vessage;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class SendNewVessageRequestBase extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setExtraInfo(String extraInfo){
        if (!StringHelper.isStringNullOrWhiteSpace(extraInfo)){
            putParameter("extraInfo",extraInfo);
        }
    }
}
