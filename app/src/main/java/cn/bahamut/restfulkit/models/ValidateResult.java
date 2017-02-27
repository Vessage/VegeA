package cn.bahamut.restfulkit.models;

import cn.bahamut.common.NotProguard;
import cn.bahamut.common.StringHelper;

/**
 * Created by alexchow on 16/4/5.
 */
@NotProguard
public class ValidateResult {
    //validate success part
    public String userId;
    public String appToken;
    public String apiServer;
    public String fileAPIServer;
    public String chicagoServer;

    //new user part
    public String registAPIServer;

    public boolean checkValidateInfoCorrect() {
        if (StringHelper.isNullOrEmpty(registAPIServer)) {
            return StringHelper.notNullOrEmpty(userId) &&
                    StringHelper.notNullOrEmpty(appToken) &&
                    StringHelper.notNullOrEmpty(apiServer) &&
                    StringHelper.notNullOrEmpty(fileAPIServer) &&
                    StringHelper.notNullOrEmpty(chicagoServer);
        }
        return true;
    }

    public String getRegistAPIServer(){
        return registAPIServer;
    }

    public boolean isNotRegistAccount(){
        return registAPIServer != null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public String getApiServer() {
        return apiServer;
    }

    public void setApiServer(String APIServer) {
        this.apiServer = APIServer;
    }

    public String getFileAPIServer() {
        return fileAPIServer;
    }

    public void setFileAPIServer(String fileAPIServer) {
        this.fileAPIServer = fileAPIServer;
    }

    public String getChicagoServer() {
        return chicagoServer;
    }

    public void setChicagoServer(String chicagoServer) {
        this.chicagoServer = chicagoServer;
    }

    public void setRegistAPIServer(String registAPIServer) {
        this.registAPIServer = registAPIServer;
    }
}