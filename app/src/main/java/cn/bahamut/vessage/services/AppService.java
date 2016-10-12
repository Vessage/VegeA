package cn.bahamut.vessage.services;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.restfulapi.app.AppFirstLaunchRequest;
import cz.msebera.android.httpclient.Header;

/**
 * Created by alexchow on 2016/10/3.
 */

public class AppService implements OnServiceInit,OnServiceUserLogin,OnServiceUserLogout{


    @Override
    public void onServiceInit(Context applicationContext) {

    }

    public void trySendFirstLaunchToServer() {
        int buildVersion = AndroidHelper.getVersionCode(AppMain.getInstance());
        int cachedBuildVersion = UserSetting.getCachedBuildVersion();
        if (cachedBuildVersion < buildVersion) {
            AppFirstLaunchRequest req = new AppFirstLaunchRequest();
            req.setBuildVersion(buildVersion);
            req.setOldBuildVersion(cachedBuildVersion);
            req.setPlatform("android");
            BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
                @Override
                public void callback(Boolean isOk, int statusCode, JSONObject result) {
                    if (statusCode == 200){
                        int buildVersion = AndroidHelper.getVersionCode(AppMain.getInstance());
                        UserSetting.setCachedBuildVersion(buildVersion);
                    }
                }
            });
        }
    }


    public void checkAppLatestVersion(Context context){
        checkAppLatestVersion(context,false);
    }

    public void checkAppLatestVersion(final Context context, final boolean userCheckUpdate){
        final long nowDays = new Date().getTime() / 86400000;
        if(!userCheckUpdate){
            long days = UserSetting.getUserSettingPreferences().getLong("CHECK_APP_LATEST_VERSION_TIME",0);
            if (nowDays - days < 7){
                return;
            }
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(context,"http://bahamut.cn/vege_android_version.json",new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int newestCode = response.getInt("versionCode");
                    UserSetting.getUserSettingPreferences().edit().putLong("CHECK_APP_LATEST_VERSION_TIME",nowDays).commit();
                    if(AndroidHelper.getVersionCode(context) < newestCode){
                        String description = response.getString("description");
                        final String url = response.getString("url");
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle(R.string.new_app_version_found)
                                .setMessage(description)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction("android.intent.action.VIEW");
                                        Uri uri = Uri.parse(url);
                                        intent.setData(uri);
                                        context.startActivity(intent);
                                    }
                                });

                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }else if(userCheckUpdate){
                        Toast.makeText(context,R.string.app_is_new_version,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(AppService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(AppService.class);
    }
}
