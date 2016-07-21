package cn.bahamut.vessage.restfulapi.groupchat;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/7/19.
 */
public class GetGroupChatRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/GroupChats";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.GET;
    }

    public void setGroupId(String groupId){
        putParameter("groupId",groupId);
    }
}
