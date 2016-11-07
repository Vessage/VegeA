package cn.bahamut.vessage.restfulapi.user;

import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 2016/11/5.
 */

public class GetUsersProfileRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/VessageUsers/Profiles";
    }

    public void setUserIds(List<String> userIds){
        String idsString = StringHelper.stringsJoinSeparator(userIds.toArray(new String[0]),",");
        if (StringHelper.isStringNullOrWhiteSpace(idsString) == false){
            putParameter("userIds",idsString);
        }
    }
}
