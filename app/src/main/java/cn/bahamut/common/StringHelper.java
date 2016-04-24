package cn.bahamut.common;

/**
 * Created by alexchow on 16/4/8.
 */
public class StringHelper {
    static public boolean isStringNullOrEmpty(String string){
        return string == null || string.length() == 0;
    }

    static public boolean isStringNullOrWhiteSpace(String string){
        return isStringNullOrEmpty(string) || string.replace("\t","").trim().length() == 0;
    }

    static public boolean isMobileNumber(String string){
        return string.matches("^((13\\d{9}$)|(15[0,1,2,3,5,6,7,8,9]\\d{8}$)|(18[0,2,5,6,7,8,9]\\d{8}$)|(147\\d{8})$)");
    }

    static public boolean isEmail(String string){
        return string.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
    }

    static public boolean isPassword(String string){
        return string.matches("^[\\@A-Za-z0-9\\!\\#\\$\\%\\^\\&\\*\\.\\~]{6,22}$");
    }

    static public boolean isUsername(String string){
        return string.matches("([a-z]|[A-Z]|[0-9]|[\\u4e00-\\u9fa5]){2,23}$");
    }
}
