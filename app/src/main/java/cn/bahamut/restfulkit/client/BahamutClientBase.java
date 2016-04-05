package cn.bahamut.restfulkit.client;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.bahamut.common.BahamutObject;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.models.LoginResult;
import cn.bahamut.vessage.models.ValidationResult;
import cz.msebera.android.httpclient.Header;
import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class BahamutClientBase implements BahamutClient,ClientLifeCircle,SetClient {
    private LoginResult loginInfo;
    private ValidationResult validationInfo;
    private boolean started;
    private HashMap<Class,Integer> inQueueCount = new HashMap<>();
    @Override
    public void startClient() {
        started = true;
    }

    @Override
    public void closeClient() {
        started = false;
    }

    @Override
    public<T extends BahamutObject> boolean executeRequest(BahamutRequestBase request, final OnRequestCompleted<T> callback) {

        if(!canSendRequest(request)){
            return false;
        }

        AsyncHttpResponseHandler handler = new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                T result = T.getObjectOfJson(response);
                callback.callback(true,statusCode,result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.callback(false,statusCode,null);
            }
        };
        sendRequest(request,handler);
        return true;
    }

    @Override
    public <T extends BahamutObject> boolean executeRequestArray(BahamutRequestBase request, final OnRequestCompleted<T[]> callback) {
        if(!canSendRequest(request)){
            return false;
        }
        AsyncHttpResponseHandler handler = new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                BahamutObject[] arr = new BahamutObject[response.length()];
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        arr[i] = T.getObjectOfJson(jsonObject);
                    } catch (JSONException e) {
                        callback.callback(false,999,null);
                        return;
                    }
                }
                callback.callback(true,statusCode,(T[])arr);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.callback(false,statusCode,null);
            }
        };

        sendRequest(request,handler);
        return true;
    }

    @Override
    public boolean executeRequestString(BahamutRequestBase request, final OnRequestCompleted<String> callback) {
        if(!canSendRequest(request)){
            return false;
        }
        AsyncHttpResponseHandler handler = new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                callback.callback(true,statusCode,responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.callback(false,statusCode,null);
            }
        };
        sendRequest(request,handler);
        return true;
    }

    private void sendRequest(BahamutRequestBase request, AsyncHttpResponseHandler handler){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        for (Map.Entry<String, String> param : request.getParameters().entrySet()) {
            params.add(param.getKey(),param.getValue());
        }

        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            client.addHeader(header.getKey(),header.getValue());
        }

        switch (request.getMethod()){
            case GET:client.get(request.getApiServerUrl(), params,handler);break;
            case PUT:client.put(request.getApiServerUrl(), params,handler); break;
            case POST:client.post(request.getApiServerUrl(), params,handler);break;
            case DELETE:client.delete(request.getApiServerUrl(), params,handler);break;
        }
    }

    private boolean canSendRequest(BahamutRequestBase request) {
        if(started == false){
            return false;
        }else{
            synchronized (this){
                Integer c = inQueueCount.get(request.getClass());
                if(c == null){
                    c = 0;
                }
                if(!request.canPostRequest(c)){
                    return false;
                }else{
                    inQueueCount.put(request.getClass(),c + 1);
                }

            }
        }
        return true;
    }

    @Override
    public void setClient(LoginResult loginResult) {
        this.loginInfo = loginResult;
    }

    @Override
    public void setClient(ValidationResult validationResult) {
        this.validationInfo = validationResult;
    }
}
