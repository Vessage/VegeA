package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class ReportObjectionableSNSPostRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/ObjectionablePosts";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    public void setPostId(String postId){
        putParameter("postId",postId);
    }
}
