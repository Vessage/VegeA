package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/5/19.
 */
public class NewPaperMessageRequest extends BahamutRequestBase{
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    protected String getApi() {
        return "/LittlePaperMessages";
    }

    public void setReceiverInfo(String receiverInfo){
        putParameter("receiverInfo",receiverInfo);
    }

    public void setMessage(String message){
        putParameter("message",message);
    }

    public void setNextReceiver(String receiver){
        putParameter("nextReceiver",receiver);
    }

    public void setOpenNeedAccept(boolean openNeedAccept){
        putParameter("openNeedAccept",String.valueOf(openNeedAccept));
    }
}
