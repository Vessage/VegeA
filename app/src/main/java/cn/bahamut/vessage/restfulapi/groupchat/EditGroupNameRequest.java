package cn.bahamut.vessage.restfulapi.groupchat;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/7/19.
 */
public class EditGroupNameRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/GroupChats/EditGroupName";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    public void setGroupId(String groupId){
        putParameter("groupId",groupId);
    }

    public void setInviteCode(String inviteCode){
        putParameter("inviteCode",inviteCode);
    }

    public void setNewGroupName(String newGroupName){
        putParameter("newGroupName",newGroupName);
    }
}
