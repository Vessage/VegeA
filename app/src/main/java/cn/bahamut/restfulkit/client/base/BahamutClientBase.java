package cn.bahamut.restfulkit.client.base;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.bahamut.restfulkit.models.BahamutClientInfo;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cz.msebera.android.httpclient.Header;

/**
 * Created by alexchow on 16/4/5.
 */
public abstract class BahamutClientBase<CI extends  BahamutClientInfo> implements BahamutClient,BahamutClientLifeProcess {
    protected CI info;

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
    public boolean executeRequest(BahamutRequestBase request, final OnRequestCompleted<JSONObject> callback) {

        if(!canSendRequest(request)){
            return false;
        }

        AsyncHttpResponseHandler handler = new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.callback(true,statusCode,response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.callback(false,statusCode,null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                callback.callback(false,statusCode,errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                callback.callback(false,statusCode,null);
            }
        };
        sendRequest(request,handler);
        return true;
    }

    @Override
    public boolean executeRequestArray(BahamutRequestBase request, final OnRequestCompleted<JSONArray> callback) {
        if(!canSendRequest(request)){
            return false;
        }
        AsyncHttpResponseHandler handler = new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                callback.callback(true,statusCode,response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.callback(false,statusCode,null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                callback.callback(false,statusCode,null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                callback.callback(false,statusCode,"errorResponse");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                callback.callback(false,statusCode,"errorResponse");
            }
        };
        sendRequest(request, handler);
        return true;
    }

    private void sendRequest(BahamutRequestBase request, AsyncHttpResponseHandler handler){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        prepareRequest(request,this.info);

        for (Map.Entry<String, String> param : request.getParameters().entrySet()) {
            params.add(param.getKey(), param.getValue());
        }

        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            client.addHeader(header.getKey(), header.getValue());
        }

        String apiUrl = request.getApiUrl();
        switch (request.getMethod()){
            case GET:client.get(apiUrl, params,handler);break;
            case PUT:client.put(apiUrl, params,handler); break;
            case POST:client.post(apiUrl, params,handler);break;
            case DELETE:client.delete(apiUrl, params,handler);break;
        }
    }

    protected abstract void prepareRequest(BahamutRequestBase request,CI clientInfo);

    private boolean canSendRequest(BahamutRequestBase request) {
        if(!started){
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

    public void setClientInfo(CI clientInfo) {
        this.info = clientInfo;
    }
}
