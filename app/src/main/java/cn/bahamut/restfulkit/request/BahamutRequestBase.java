package cn.bahamut.restfulkit.request;

import java.util.HashMap;

/**
 * Created by alexchow on 16/4/2.
 */
public class BahamutRequestBase {

    private String apiServerUrl;
    private String api;
    private RequestMethod method = RequestMethod.GET;
    private HashMap<String,String> headers;
    private HashMap<String,String> parameters;
    public boolean canPostRequest(int inQueueRequestCount){
        return true;
    }

    public boolean canClientResetUri(){
        return true;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public void putParameter(String key,String value){
        if(parameters == null){
            parameters = new HashMap<>();
        }
        parameters.put(key,value);
    }

    public void popParameter(String key){
        if(parameters != null){
            parameters.remove(key);
        }
    }

    public HashMap<String,String> getParameters(){
        if(parameters == null){
            return new HashMap<>();
        }
        return parameters;
    };

    public void putHeader(String key,String value){
        if(headers == null){
            headers = new HashMap<>();
        }
        headers.put(key,value);
    }

    public void popHeader(String key){
        if(headers != null){
            headers.remove(key);
        }
    }

    public HashMap<String,String> getHeaders(){
        if(headers == null){
            return new HashMap<>();
        }
        return headers;
    }

    public String getApi() {
        return api;
    }

    public String getApiServerUrl() {
        return apiServerUrl;
    }

    public String getApiUrl(){
        return apiServerUrl + api;
    }

    protected void setApi(String api) {
        this.api = api;
    }
}
