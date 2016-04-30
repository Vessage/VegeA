package cn.bahamut.vessage.main;

/**
 * Created by alexchow on 16/4/24.
 */
public class LocalizedStringHelper {
    public static int getLocalizedStringResId(String localizedString){
        return AppMain.getInstance().getResources().getIdentifier(localizedString,"string",AppMain.getInstance().getPackageName());
    }

    public static String getLocalizedString(String localizedString){
        return getLocalizedString(getLocalizedStringResId(localizedString));
    }

    public static String getLocalizedString(int resId){
        return AppMain.getInstance().getResources().getString(resId);
    }
}