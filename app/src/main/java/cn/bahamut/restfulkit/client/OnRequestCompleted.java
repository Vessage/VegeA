package cn.bahamut.restfulkit.client;

/**
 * Created by alexchow on 16/4/5.
 */
public interface OnRequestCompleted<T>{
    void callback(Boolean isOk,int statusCode, T result);
}
