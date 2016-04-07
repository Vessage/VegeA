package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class MessageResult {
    public String msg;

    public void setFieldValuesByJson(JSONObject jsonObject){
        try {
            msg = jsonObject.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
