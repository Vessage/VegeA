package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSGodLikePostRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/GodLikePost";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setPostId(String postId){
        putParameter("pstId",postId);
    }
}
