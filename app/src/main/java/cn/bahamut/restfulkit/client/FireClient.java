package cn.bahamut.restfulkit.client;

import cn.bahamut.restfulkit.client.base.BahamutClientBase;
import cn.bahamut.restfulkit.models.BahamutClientInfo;
import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/5.
 */
public class FireClient extends BahamutClientBase<FireClient.FireClientInfo> {

    static public class FireClientInfo extends BahamutClientInfo{
        public String appKey;
        public String userId;
        public String appToken;
        public String fileAPIServer;
    }

    @Override
    protected void prepareRequest(BahamutRequestBase request, FireClientInfo clientInfo) {
        request.putHeader("appkey",clientInfo.appKey);
        request.setApiServerUrl(clientInfo.fileAPIServer);
        request.putHeader("userId", clientInfo.userId);
        request.putHeader("token",clientInfo.appToken);
    }

}
