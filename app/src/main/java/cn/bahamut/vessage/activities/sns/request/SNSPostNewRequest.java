package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSPostNewRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/NewPost";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setImage(String imageId){
        putParameter("image",imageId);
    }

    public void setNick(String nick){
        putParameter("nick",nick);
    }

    public void setBody(String body) {
        putParameter("body", body);
    }

    public void setState(int state) {
        putParameter("state", String.valueOf(state));
    }
}
