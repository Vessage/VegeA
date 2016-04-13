package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class LoginResult{
    private String LoginSuccessed;
    private String AccountID;
    private String AccountName;
    private String AccessToken;
    private String AppServerIP;
    private String AppServerPort;
    private String AppServiceUrl;
    private String BindMobile;
    private String BindEmail;

    public boolean isLoginSuccessed(){
        return Boolean.parseBoolean(LoginSuccessed);
    }

    public void setLoginSuccessed(String loginSuccessed) {
        this.LoginSuccessed = loginSuccessed;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        this.AccountID = accountID;
    }

    public String getAccountName() {
        return AccountName;
    }

    public void setAccountName(String accountName) {
        this.AccountName = accountName;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String accessToken) {
        this.AccessToken = accessToken;
    }

    public String getAppServerIP() {
        return AppServerIP;
    }

    public void setAppServerIP(String appServerIP) {
        this.AppServerIP = appServerIP;
    }

    public String getAppServerPort() {
        return AppServerPort;
    }

    public void setAppServerPort(String appServerPort) {
        this.AppServerPort = appServerPort;
    }

    public String getAppServiceUrl() {
        return AppServiceUrl;
    }

    public void setAppServiceUrl(String appServiceUrl) {
        this.AppServiceUrl = appServiceUrl;
    }

    public String getBindMobile() {
        return BindMobile;
    }

    public void setBindMobile(String bindMobile) {
        this.BindMobile = bindMobile;
    }

    public String getBindEmail() {
        return BindEmail;
    }

    public void setBindEmail(String bindEmail) {
        this.BindEmail = bindEmail;
    }

    public String getLoginSuccessed() {
        return LoginSuccessed;
    }
}
