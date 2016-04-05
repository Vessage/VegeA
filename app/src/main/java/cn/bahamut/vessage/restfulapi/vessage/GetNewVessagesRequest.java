package cn.bahamut.vessage.restfulapi.vessage;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/5.
 */
public class GetNewVessagesRequest extends BahamutRequestBase {
    @Override
    public String getApi() {
        return "/Vessages/New";
    }
}
