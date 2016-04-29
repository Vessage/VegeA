package cn.bahamut.vessage.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import java.io.InputStream;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.TextHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.FireClient;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.SignInActivity;
import cn.bahamut.vessage.account.SignUpActivity;
import cn.bahamut.vessage.conversation.ConversationListActivity;
import cn.bahamut.vessage.services.AccountService;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;
import cn.bahamut.vessage.services.file.FileService;
import cn.smssdk.SMSSDK;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by alexchow on 16/4/1.
 */
public class AppMain extends Application{
    private static final int UI_ANIMATION_DELAY = 700;
    static private AppMain instance;
    private boolean firstLaunch = false;

    public static AppMain getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    private void congifureSMSSDK() {
        SMSSDK.initSDK(this, VessageConfig.getBahamutConfig().getSmsSDKAppkey(), VessageConfig.getBahamutConfig().getSmsSDKSecretKey());
    }

    public boolean startConfigure(){
        if(!firstLaunch){
            firstLaunch = true;
            registerActivityLifecycleCallbacks(onActivityLifecycle);
            switch (UserSetting.getAppConfig()){
                case UserSetting.APP_CONFIG_DEFAULT:loadConfigures(R.raw.bahamut_config);break;
                case UserSetting.APP_CONFIG_DEV:loadConfigures(R.raw.bahamut_config_dev);break;
            }
            configureServices();
            configureUPush();
            congifureSMSSDK();
            configureUMeng();
        }
        return true;
    }

    private ActivityLifecycleCallbacks onActivityLifecycle = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            MobclickAgent.onResume(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            MobclickAgent.onPause(activity);
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    private void configureUMeng() {
        if(!AndroidHelper.isApkDebugable(getApplicationContext())){
            AnalyticsConfig.setAppkey(this,VessageConfig.getBahamutConfig().getUmengAppkey());
            AnalyticsConfig.setChannel(VessageConfig.getBahamutConfig().getUmengChannel());
        }
    }

    private void configureUPush() {
        PushAgent mPushAgent = PushAgent.getInstance(getApplicationContext());
        String umessageAppkey = VessageConfig.getBahamutConfig().getUmengAppkey();
        String umessageAppSecret = VessageConfig.getBahamutConfig().getUmessageSecretKey();
        mPushAgent.setAppkeyAndSecret(umessageAppkey,umessageAppSecret);
        mPushAgent.setNotificationClickHandler(notificationHandler);
    }

    private UmengNotificationClickHandler notificationHandler = new UmengNotificationClickHandler(){
        @Override
        public void dealWithCustomAction(Context context, UMessage msg) {
            if(msg.custom.equals("OtherDeviceLogin")){
                onOtherDeviceLogin();
            }
            if(msg.custom.equals("NewVessageNotify")){
                ServicesProvider.getService(VessageService.class).newVessageFromServer();
            }
        }
    };

    private void onOtherDeviceLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle(R.string.other_device_logon);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.setUserLogout();
                ServicesProvider.userLogout();
                Intent intent = new Intent(getApplicationContext(),EntryActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void loadConfigures(int configResId) {
        InputStream inputStream = getApplicationContext().getResources().openRawResource(configResId);
        String json = TextHelper.readInputStreamText(inputStream);
        if(json != null){
            VessageConfig.loadBahamutConfig(json);
        }
    }

    private void configureRealm(String userId){
        Realm.removeDefaultConfiguration();
        RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext())
                .name(userId + ".realm")
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(config);
    }

    private void configureServices() {
        ServicesProvider.registService(new AccountService());
        ServicesProvider.registService(new FileService());
        ServicesProvider.registService(new UserService());
        ServicesProvider.registService(new ConversationService());
        ServicesProvider.registService(new VessageService());
        ServicesProvider.initServices(getApplicationContext());
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_WILL_LOGOIN, onUserWillLogin);
    }

    private Observer onUserWillLogin = new Observer() {
        @Override
        public void update(ObserverState state) {
            configureRealm(UserSetting.getUserId());
        }
    };

    public void useDeviceToken(String deviceToken){
        UserSetting.setDeviceToken(deviceToken);
    }

    public void useValidateResult(ValidateResult validateResult){
        UserSetting.setUserValidateResult(validateResult);
        UserSetting.setUserId(validateResult.getUserId());
        UserSetting.setUserLogin();
        useAPIClient(validateResult);
        useFireClient(validateResult);
    }

    private void useFireClient(ValidateResult validateResult) {
        FireClient.FireClientInfo fireClientInfo = new FireClient.FireClientInfo();
        fireClientInfo.appKey = VessageConfig.getAppkey();
        fireClientInfo.appToken = validateResult.getAppToken();
        fireClientInfo.fileAPIServer = validateResult.getFileAPIServer();
        fireClientInfo.userId = validateResult.getUserId();
        FireClient fireClient = new FireClient();
        fireClient.setClientInfo(fireClientInfo);
        BahamutRFKit.instance.useClient(fireClient);
    }

    private void useAPIClient(ValidateResult validateResult) {
        APIClient.APIClientInfo apiClientInfo = new APIClient.APIClientInfo();
        apiClientInfo.apiServer = validateResult.getAPIServer();
        apiClientInfo.appToken = validateResult.getAppToken();
        apiClientInfo.userId = validateResult.getUserId();
        APIClient apiClient = new APIClient();
        apiClient.setClientInfo(apiClientInfo);
        BahamutRFKit.instance.useClient(apiClient);
    }

    static public void startEntryActivity(Activity context){
        Intent intent = new Intent(context,EntryActivity.class);
        context.startActivity(intent);
        context.finish();
    }

    static public void startMainActivity(final Activity context){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showMainActivity(context);
            }
        }, UI_ANIMATION_DELAY);
    }

    static private void showMainActivity(Activity context) {
        Intent intent = new Intent(context, ConversationListActivity.class);
        context.startActivity(intent);
        context.finish();
    }

    static public void startSignActivity(final Activity context){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showSignActivity(context);
            }
        }, UI_ANIMATION_DELAY);
    }

    static private void showSignActivity(Activity context){
        Intent intent = null;
        if(UserSetting.getLastUserLoginedAccount() == null){
            intent = new Intent(context, SignUpActivity.class);
        }else{
            intent = new Intent(context, SignInActivity.class);
        }
        context.startActivity(intent);
        context.finish();
    }
}
