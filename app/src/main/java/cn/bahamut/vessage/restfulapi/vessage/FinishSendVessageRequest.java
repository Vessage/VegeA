package cn.bahamut.vessage.restfulapi.vessage;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/5.
 */
public class FinishSendVessageRequest extends CancelSendVessageRequest {
    @Override
    public String getApi() {
        return "/Vessages/FinishSendVessage";
    }

    public void setFileId(String fileId) {
        putParameter("fileId", fileId);
    }

}
