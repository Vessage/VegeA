package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/6/13.
 */
public class GetReadPaperResponsesRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/LittlePaperMessages/ReadPaperResponses";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.GET;
    }
}
