package cn.bahamut.common;

import android.util.Base64;

/**
 * Created by alexchow on 16/4/8.
 */
public class StringHelper {

    static public boolean notNullOrEmpty(String string){
        return !isNullOrEmpty(string);
    }

    static public boolean isNullOrEmpty(String string){
        return string == null || string.length() == 0;
    }


    static public boolean notStringNullOrWhiteSpace(String string){
        return !isStringNullOrWhiteSpace(string);
    }

    static public boolean isStringNullOrWhiteSpace(String string){
        return isNullOrEmpty(string) || string.replace("\t","").trim().length() == 0;
    }

    static public boolean isMobileNumber(String string){
        return string.matches("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
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

    // 将 s 进行 BASE64 编码
    public static String getBASE64(String str) {
        if (str == null) return null;
        return new String(Base64.encode(str.getBytes(), Base64.DEFAULT));
    }

    // 将 BASE64 编码的字符串 s 进行解码
    public static String getFromBASE64(String str) {
        if (str == null) return null;
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }

    public static String stringsJoinSeparator(String[] strings, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : strings) {
            stringBuilder.append(str);
            stringBuilder.append(separator);
        }
        return stringBuilder.toString();
    }
}
