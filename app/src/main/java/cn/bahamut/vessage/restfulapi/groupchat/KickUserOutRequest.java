package cn.bahamut.vessage.restfulapi.groupchat;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/7/19.
 */
public class KickUserOutRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/GroupChats/KickUserOut";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.DELETE;
    }

    public void setGroupId(String groupId){
        putParameter("groupId",groupId);
    }

    public void setUserId(String userId){
        putParameter("userId",userId);
    }
}
