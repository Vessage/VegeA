package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSLikePostRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/LikePost";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setPostId(String postId){
        putParameter("postId",postId);
    }

    public void setNick(String nick){
        putParameter("nick",nick);
    }
}
