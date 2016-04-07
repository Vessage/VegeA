package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class RegistResult {
    public Boolean suc = false;

    //regist info
    public String accountId;
    public String accountName;

    public void setFieldValuesByJson(JSONObject jsonObject){
        try {
            suc = jsonObject.getBoolean("suc");
            accountId = jsonObject.getString("accountId");
            accountName = jsonObject.getString("accountName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
