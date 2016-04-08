package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class ValidateResult {
    //validate success part
    private String UserId;
    private String AppToken;
    private String APIServer;
    private String FileAPIServer;
    private String ChicagoServer;

    //new user part
    private String RegistAPIServer;

    public boolean isNotRegistAccount(){
        return RegistAPIServer != null;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getAppToken() {
        return AppToken;
    }

    public void setAppToken(String appToken) {
        AppToken = appToken;
    }

    public String getAPIServer() {
        return APIServer;
    }

    public void setAPIServer(String APIServer) {
        this.APIServer = APIServer;
    }

    public String getFileAPIServer() {
        return FileAPIServer;
    }

    public void setFileAPIServer(String fileAPIServer) {
        FileAPIServer = fileAPIServer;
    }

    public String getChicagoServer() {
        return ChicagoServer;
    }

    public void setChicagoServer(String chicagoServer) {
        ChicagoServer = chicagoServer;
    }

    public void setRegistAPIServer(String registAPIServer) {
        RegistAPIServer = registAPIServer;
    }
}