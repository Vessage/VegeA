package cn.bahamut.restfulkit.client.base;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/2.
 */
public interface BahamutClient {
    boolean executeRequest(BahamutRequestBase request,OnRequestCompleted<JSONObject> callback);
    boolean executeRequestString(BahamutRequestBase request,OnRequestCompleted<String> callback);
    boolean executeRequestArray(BahamutRequestBase request,OnRequestCompleted<JSONArray> callback);
}
