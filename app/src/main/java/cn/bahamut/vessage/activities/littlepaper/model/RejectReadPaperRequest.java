package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/6/13.
 */
public class RejectReadPaperRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/LittlePaperMessages/RejectReadPaper";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setReader(String reader){
        putParameter("reader",reader);
    }

    public void setPaperId(String paperId){
        putParameter("paperId",paperId);
    }
}
