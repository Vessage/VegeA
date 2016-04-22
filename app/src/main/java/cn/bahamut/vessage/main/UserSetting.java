package cn.bahamut.vessage.main;

import android.app.Activity;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.restfulkit.models.ValidateResult;

/**
 * Created by alexchow on 16/4/1.
 */
public class UserSetting {
    static private final String sharedPreferencesName = "UserSetting";
    static public SharedPreferences getUserSettingPreferences(){
        return AppMain.getInstance().getApplicationContext().getSharedPreferences(UserSetting.sharedPreferencesName, Activity.MODE_PRIVATE);
    }

    static public void setUserValidateResult(ValidateResult validateResult){
        String json = JsonHelper.toJSON(validateResult);
        getUserSettingPreferences().edit().putString("validateResult",json).commit();
    }

    static public ValidateResult getUserValidateResult(){
        String json = getUserSettingPreferences().getString("validateResult",null);
        if(json != null){
            try {
                JSONObject jsonObject = new JSONObject(json);
                ValidateResult result = JsonHelper.parseObject(jsonObject,ValidateResult.class);
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static public void setUserLogin(){
        getUserSettingPreferences().edit().putBoolean("userLogined",true).commit();
    }

    static public void setUserLogout(){
        getUserSettingPreferences().edit().putBoolean("userLogined",false).commit();
    }

    static public boolean isUserLogined(){
        return getUserSettingPreferences().getBoolean("userLogined", false);
    }

    static public String getLastUserLoginedAccount(){
        return getUserSettingPreferences().getString("accountId",null);
    }

    public static void setLastUserLoginedAccount(String lastUserLoginedAccount) {
        getUserSettingPreferences().edit().putString("accountId", lastUserLoginedAccount).commit();
    }

    public static String getUserId() {
        return getUserSettingPreferences().getString("userId",null);
    }

    public static void setUserId(String userId) {
        getUserSettingPreferences().edit().putString("userId", userId).commit();
    }
}
