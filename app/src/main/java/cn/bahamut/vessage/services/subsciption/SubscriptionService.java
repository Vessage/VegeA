package cn.bahamut.vessage.services.subsciption;

import org.json.JSONArray;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.restfulapi.subscription.GetSubscriptionAccountsRequest;

/**
 * Created by alexchow on 2017/3/18.
 */

public class SubscriptionService implements OnServiceUserLogin, OnServiceUserLogout {
    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(SubscriptionService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(SubscriptionService.class);
    }

    public void getOnlineSubscriptionAccounts(final GetSubscriptionAccountsCallback callback) {
        GetSubscriptionAccountsRequest req = new GetSubscriptionAccountsRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                SubscriptionAccount[] arr = null;
                if (isOk) {
                    arr = JsonHelper.parseArray(result, SubscriptionAccount.class);
                }
                if (callback != null) {
                    callback.onGetSubscriptionAccounts(arr);
                }
            }
        });
    }

    /**
     * Created by alexchow on 2017/3/18.
     */

    public interface GetSubscriptionAccountsCallback {
        void onGetSubscriptionAccounts(SubscriptionAccount[] accounts);
    }
}
