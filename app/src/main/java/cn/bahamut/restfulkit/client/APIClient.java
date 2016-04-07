package cn.bahamut.restfulkit.client;

import cn.bahamut.restfulkit.client.base.BahamutClientBase;
import cn.bahamut.restfulkit.models.BahamutClientInfo;
import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/5.
 */
public class APIClient extends BahamutClientBase<APIClient.APIClientInfo> {

    static public class APIClientInfo extends BahamutClientInfo {
        public String userId;
        public String appToken;
        public String apiServer;
    }

    @Override
    protected void prepareRequest(BahamutRequestBase request, APIClientInfo clientInfo) {
        request.setApiServerUrl(clientInfo.apiServer);
        request.putHeader("userId", clientInfo.userId);
        request.putHeader("token",clientInfo.appToken);
    }
}
