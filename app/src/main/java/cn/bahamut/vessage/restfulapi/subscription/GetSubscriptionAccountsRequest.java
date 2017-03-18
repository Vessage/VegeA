package cn.bahamut.vessage.restfulapi.subscription;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 2017/3/18.
 */

public class GetSubscriptionAccountsRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/Subscription";
    }
}
