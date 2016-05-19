package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/5/19.
 */
public class PostPaperMessageRequest extends BahamutRequestBase{

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    protected String getApi() {
        return "/LittlePaperMessages/PostMessage";
    }

    public void setIsAnonymous(boolean isAnonymous) {
        putParameter("isAnonymousPost",String.valueOf(isAnonymous));
    }

    public void setPaperId(String paperId){
        putParameter("paperId",paperId);
    }

    public void setNextReceiver(String receiver){
        putParameter("nextReceiver",receiver);
    }
}
