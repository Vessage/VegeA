package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class DeleteSNSPostCommentRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/Comments";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.DELETE;
    }

    public void setPostId(String postId) {
        putParameter("postId", postId);
    }

    public void setCmtId(String cmtId) {
        putParameter("cmtId", cmtId);
    }

    public void setIsCmtOwner(boolean isCmtOwner) {
        putParameter("cmtOwner", String.valueOf(isCmtOwner));
    }
}
