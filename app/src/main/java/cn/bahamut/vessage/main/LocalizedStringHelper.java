package cn.bahamut.vessage.main;

/**
 * Created by alexchow on 16/4/24.
 */
public class LocalizedStringHelper {
    public static int getLocalizedStringResId(String localizedString){
        return AppMain.getInstance().getResId(localizedString, "string");
    }

    public static String getLocalizedString(String localizedString){
        int resId = getLocalizedStringResId(localizedString);
        if(resId == 0){
            return localizedString;
        }
        return getLocalizedString(resId);
    }

    public static String getLocalizedString(int resId){
        return AppMain.getInstance().getResources().getString(resId);
    }
}