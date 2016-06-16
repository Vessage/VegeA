package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/6/13.
 */
public class AskSenderReadPaperRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    protected String getApi() {
        return "/LittlePaperMessages/AskReadPaper";
    }

    public void setPaperId(String paperId){
        putParameter("paperId",paperId);
    }
}
