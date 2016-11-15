package cn.bahamut.vessage.activities.sns.request;

/**
 * Created by alexchow on 2016/11/13.
 */

public class GetSNSPostCommentRequest extends GetSNSValuesRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/PostComments";
    }

    public void setPostId(String postId){
        putParameter("postId",postId);
    }
}
