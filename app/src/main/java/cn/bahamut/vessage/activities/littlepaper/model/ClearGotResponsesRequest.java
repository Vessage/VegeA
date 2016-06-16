package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/6/13.
 */
public class ClearGotResponsesRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/LittlePaperMessages/ClearGotResponses";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.DELETE;
    }
}
