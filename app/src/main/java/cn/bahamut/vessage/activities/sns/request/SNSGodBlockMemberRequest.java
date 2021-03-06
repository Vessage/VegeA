package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSGodBlockMemberRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/GodBlockMember";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setMemberId(String memberId){
        putParameter("mbId",memberId);
    }
}
