package cn.bahamut.vessage.main;

import org.json.JSONException;

import cn.bahamut.common.JsonHelper;

/**
 * Created by alexchow on 16/4/7.
 */
public class VessageConfig {

    static private BahamutConfigObject bahamutConfig;

    static public String getAppkey(){
        return getBahamutConfig().getAppkey();
    }

    static public String getAppName(){
        return getBahamutConfig().getAppName();
    }

    static public String getRegion(){
        return "cn";
    }

    public static BahamutConfigObject getBahamutConfig() {
        return bahamutConfig;
    }

    public static void loadBahamutConfig(String json) {
        try {
            bahamutConfig = JsonHelper.parseObject(json,BahamutConfigObject.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}