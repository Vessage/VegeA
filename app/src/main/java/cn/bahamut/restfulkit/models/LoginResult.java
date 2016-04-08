package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class LoginResult{
    public String LoginSuccessed;
    public String AccountID;
    public String AccountName;
    public String AccessToken;
    public String AppServerIP;
    public String AppServerPort;
    public String AppServiceUrl;
    public String BindMobile;
    public String BindEmail;

    public boolean isLoginSuccessed(){
        return Boolean.parseBoolean(LoginSuccessed);
    }

    public void setFieldValuesByJson(JSONObject jsonObject) throws JSONException {
        LoginSuccessed = jsonObject.getString("LoginSuccessed");
        AccountID = jsonObject.getString("AccountID");
        AccountName = jsonObject.getString("AccountName");
        AccessToken = jsonObject.getString("AccessToken");
        AppServerIP = jsonObject.getString("AppServerIP");
        AppServerPort = jsonObject.getString("AppServerPort");
        AppServiceUrl = jsonObject.getString("AppServiceUrl");
        BindMobile = jsonObject.getString("BindMobile");
        BindEmail = jsonObject.getString("BindEmail");

    }
}
