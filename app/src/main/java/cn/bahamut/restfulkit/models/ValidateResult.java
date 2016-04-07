package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class ValidateResult {
    //validate success part
    public String UserId;
    public String AppToken;
    public String APIServer;
    public String FileAPIServer;
    public String ChicagoServer;

    //new user part
    public String RegistAPIServer;

    public boolean isNotRegistAccount(){
        return RegistAPIServer != null;
    }

    public boolean isValidateResultDataComplete() {
        if (RegistAPIServer != null) {
            return true;
        } else {
            return (UserId != null &&
                    AppToken != null &&
                    FileAPIServer != null &&
                    APIServer != null &&
                    ChicagoServer != null
            );
        }
    }

    public void setFieldValuesByJson(JSONObject jsonObject){
        try {
            UserId = jsonObject.getString("UserId");
            AppToken = jsonObject.getString("AppToken");
            APIServer = jsonObject.getString("APIServer");
            FileAPIServer = jsonObject.getString("FileAPIServer");
            ChicagoServer = jsonObject.getString("ChicagoServer");
            RegistAPIServer = jsonObject.getString("RegistAPIServer");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}