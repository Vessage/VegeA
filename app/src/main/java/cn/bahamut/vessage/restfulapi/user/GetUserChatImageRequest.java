package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 2016/9/26.
 */

public class GetUserChatImageRequest extends BahamutRequestBase {

    @Override
    protected String getApi() {
        return "/VessageUsers/ChatImages";
    }

    public void setUserId(String userId){
        if(!StringHelper.isStringNullOrWhiteSpace(userId)) {
            putParameter("userId", userId);
        }
    }

    @Override
    public boolean canPostRequest(int inQueueRequestCount) {
        return inQueueRequestCount == 0;
    }
}
