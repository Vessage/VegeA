package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/5/19.
 */
public class OpenAcceptlessPaperRequest extends BahamutRequestBase{
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    public void setPaperId(String paperId){
        this.setApi(String.format("/LittlePaperMessages/OpenPaperId/%s",paperId));
    }
}
