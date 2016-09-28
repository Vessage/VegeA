package cn.bahamut.vessage.restfulapi.user;

import android.util.Log;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/29.
 */
public class RegistUserDeviceRequest extends BahamutRequestBase {

    public static final String DEVICE_TYPE_ANDROID = "Android";

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    @Override
    protected String getApi() {
        return "/VessageUsers/UserDevice";
    }

    public void setDeviceToken(String deviceToken){
        putParameter("deviceToken",deviceToken);
    }

    public void setDeviceType(String deviceType){
        putParameter("deviceType",deviceType);
    }

    @Override
    public boolean canPostRequest(int inQueueRequestCount) {
        Log.i("RegistUserDeviceRequest",inQueueRequestCount + "");
        return inQueueRequestCount == 0;
    }
}
