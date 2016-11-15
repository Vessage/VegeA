package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 2016/11/13.
 */

public class GetSNSMainBoardDataRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/SNS/SNSMainBoardData";
    }

    public void setPostCount(int postCount){
        putParameter("postCnt",String.valueOf(postCount));
    }

    public void setLocation(String location){
        putParameter("location",location);
    }

    public void setFocusIds(String[] focusIds){
        if (focusIds != null && focusIds.length > 0){
            putParameter("focusIds",StringHelper.stringsJoinSeparator(focusIds,","));
        }
    }
}
