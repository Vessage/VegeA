package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSNewCommentRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/PostComments";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setComment(String comment){
        putParameter("comment",comment);
    }

    public void setPostId(String postId){
        putParameter("postId",postId);
    }

    public void setSenderNick(String nick){
        putParameter("senderNick",nick);
    }

    public void setAtUserId(String atUserId){
        putParameter("atUser",atUserId);
    }

    public void setAtUserNick(String atUserNick){
        putParameter("atNick",atUserNick);
    }
}
