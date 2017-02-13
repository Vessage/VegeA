package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class EditSNSPostStateRequest extends DeleteSNSPostRequest {
    @Override
    protected String getApi() {
        return "/SNS/PostState";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    public void setState(int state) {
        putParameter("state", String.valueOf(state));
    }
}
