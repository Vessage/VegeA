package cn.bahamut.vessage.restfulapi.groupchat;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/7/19.
 */
public class CreateGroupChatRequest extends BahamutRequestBase{
    @Override
    protected String getApi() {
        return "/GroupChats/CreateGroupChat";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setGroupName(String groupName){
        putParameter("groupName",groupName);
    }

    public void setGroupUsers(String[] groupUsers){
        putParameter("groupUsers",StringHelper.stringsJoinSeparator(groupUsers,","));
    }
}
