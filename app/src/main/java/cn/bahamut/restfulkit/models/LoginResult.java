package cn.bahamut.restfulkit.models;

/**
 * Created by alexchow on 16/4/5.
 */
public class LoginResult{
    private String loginSuccessed;
    private String accountID;
    private String accountName;
    private String accessToken;
    private String appServerIP;
    private String appServerPort;
    private String appServiceUrl;
    private String bindMobile;
    private String bindEmail;

    public boolean isLoginSuccessed(){
        return Boolean.parseBoolean(loginSuccessed);
    }

    public void setLoginSuccessed(String loginSuccessed) {
        this.loginSuccessed = loginSuccessed;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAppServerIP() {
        return appServerIP;
    }

    public void setAppServerIP(String appServerIP) {
        this.appServerIP = appServerIP;
    }

    public String getAppServerPort() {
        return appServerPort;
    }

    public void setAppServerPort(String appServerPort) {
        this.appServerPort = appServerPort;
    }

    public String getAppServiceUrl() {
        return appServiceUrl;
    }

    public void setAppServiceUrl(String appServiceUrl) {
        this.appServiceUrl = appServiceUrl;
    }

    public String getBindMobile() {
        return bindMobile;
    }

    public void setBindMobile(String bindMobile) {
        this.bindMobile = bindMobile;
    }

    public String getBindEmail() {
        return bindEmail;
    }

    public void setBindEmail(String bindEmail) {
        this.bindEmail = bindEmail;
    }

    public String getLoginSuccessed() {
        return loginSuccessed;
    }
}
