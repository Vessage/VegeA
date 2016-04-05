package cn.bahamut.restfulkit.client;

import cn.bahamut.common.BahamutObject;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/2.
 */
public interface BahamutClient {
    <T extends BahamutObject> boolean executeRequest(BahamutRequestBase request,OnRequestCompleted<T> callback);
    boolean executeRequestString(BahamutRequestBase request,OnRequestCompleted<String> callback);
    <T extends BahamutObject> boolean executeRequestArray(BahamutRequestBase request,OnRequestCompleted<T[]> callback);
}
