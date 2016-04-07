package cn.bahamut.vessage.main;

/**
 * Created by alexchow on 16/4/1.
 */
public class UserSetting {
    static public boolean isUserLogined(){
        return true;
    }

    static private String lastUserLoginedAccount;
    static private String userId;

    static public String getLastUserLoginedAccount(){
        return "";
    }

    public static String getUserId() {
        return "sdfs";
    }

    public static void setLastUserLoginedAccount(String lastUserLoginedAccount) {
        UserSetting.lastUserLoginedAccount = lastUserLoginedAccount;
    }

    public static void setUserId(String userId) {
        UserSetting.userId = userId;
    }
}
