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
    public static final int APP_CONFIG_DEFAULT = 1;
    public static final int APP_CONFIG_DEV = 2;

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

    public static int getAppConfig(){
        return getUserSettingPreferences().getInt("app_config",APP_CONFIG_DEFAULT);
    }

    public static void setAppConfig(int config){
        getUserSettingPreferences().edit().putInt("app_config", config).commit();
    }

    public static String getDeviceToken(){
        return getUserSettingPreferences().getString("device_token",null);
    }

    public static void setDeviceToken(String deviceToken){
        getUserSettingPreferences().edit().putString("device_token",deviceToken).commit();
    }

    public static boolean isNotifySMSSendedToMobile(String mobile){
        return getUserSettingPreferences().getBoolean("NOTIFY_SMS_SENDED_" + mobile,false);
    }

    public static void setNotifySMSSendedToMobile(String mobile){
        getUserSettingPreferences().edit().putBoolean("NOTIFY_SMS_SENDED_" + mobile,true).commit();
    }
}
