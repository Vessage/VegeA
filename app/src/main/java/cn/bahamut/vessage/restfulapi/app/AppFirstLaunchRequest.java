package cn.bahamut.vessage.restfulapi.app;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/10/3.
 */
public class AppFirstLaunchRequest extends BahamutRequestBase {

    @Override
    protected String getApi() {
        return "/App/FirstLaunch";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setBuildVersion(int buildVersion) {
        putParameter("buildVersion", buildVersion + "");
    }

    public void setOldBuildVersion(int oldBuildVersion) {
        putParameter("oldBuildVersion", oldBuildVersion + "");
    }

    public void setPlatform(String platform) {
        putParameter("platform", platform);
    }

}
