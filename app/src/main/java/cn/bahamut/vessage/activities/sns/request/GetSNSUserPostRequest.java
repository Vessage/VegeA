package cn.bahamut.vessage.activities.sns.request;

/**
 * Created by alexchow on 2017/1/4.
 */

public class GetSNSUserPostRequest extends GetSNSValuesRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/UserPosts";
    }

    public void setUserId(String userId) {
        putParameter("userId", userId);
    }
}
