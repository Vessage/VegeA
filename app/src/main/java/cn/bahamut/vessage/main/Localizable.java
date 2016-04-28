package cn.bahamut.vessage.main;

import java.util.HashMap;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/4/24.
 */
public class Localizable {
    private static HashMap<String,Integer> localizable;
    public static int getLocalizableResId(String localizedString){
        if(localizable.containsKey(localizedString)){
            return localizable.get(localizedString);
        }
        return 0;
    }

    static{
        localizable = new HashMap<>();
        localizable.put("CHANGE_PASSWORD_SUCCESS", R.string.CHANGE_PASSWORD_SUCCESS);
        localizable.put("CHANGE_PASSWORD_ERROR", R.string.CHANGE_PASSWORD_ERROR);
        localizable.put("TOKEN_UNAUTHORIZED", R.string.TOKEN_UNAUTHORIZED);
        localizable.put("VALIDATE_DATA_ERROR", R.string.VALIDATE_DATA_ERROR);
        localizable.put("NETWORK_ERROR", R.string.NETWORK_ERROR);
        localizable.put("NOT_LOGIN", R.string.NOT_LOGIN);
        localizable.put("LOGOUTED", R.string.LOGOUTED);
        localizable.put("OTHER_DEVICE_HAD_LOGIN", R.string.OTHER_DEVICE_HAD_LOGIN);
        localizable.put("VALIDATE_ACCTOKEN_FAILED", R.string.VALIDATE_ACCTOKEN_FAILED);
        localizable.put("NO_MORE_MESSAGE", R.string.NO_MORE_MESSAGE);
        localizable.put("SEND_WHITE_SPACE_ERROR", R.string.SEND_WHITE_SPACE_ERROR);
        localizable.put("CONNECTING", R.string.CONNECTING);
        localizable.put("CONNECTED", R.string.CONNECTED);
        localizable.put("CONNECT_ERROR", R.string.CONNECT_ERROR);
        localizable.put("CHICAGO_VALIDATE_FAILED", R.string.CHICAGO_VALIDATE_FAILED);
        localizable.put("CONNECT_ERROR_TAP_RETRY", R.string.CONNECT_ERROR_TAP_RETRY);
        localizable.put("REGIST_FAILED", R.string.REGIST_FAILED);
        localizable.put("REGIST_SUC", R.string.REGIST_SUC);
        localizable.put("DATA_ERROR", R.string.DATA_ERROR);
        localizable.put("USER_NAME_EXISTS", R.string.USER_NAME_EXISTS);
        localizable.put("ALLOC_TOKEN_FAILED", R.string.ALLOC_TOKEN_FAILED);
        localizable.put("NO_APP_INSTANCE", R.string.NO_APP_INSTANCE);
        localizable.put("VALIDATE_INFO_INVALID", R.string.VALIDATE_INFO_INVALID);
        localizable.put("VALIDATE_FAILED", R.string.VALIDATE_FAILED);
    }

}
