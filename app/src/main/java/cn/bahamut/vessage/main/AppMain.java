package cn.bahamut.vessage.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import java.io.InputStream;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
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
import cn.bahamut.vessage.conversation.ConversationViewActivity;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.SendVessageTask;
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
    static private Activity currentActivity;
    private boolean firstLaunch = false;

    public static AppMain getInstance() {
        return instance;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        synchronized (instance){
            AppMain.currentActivity = currentActivity;
        }
    }

    public static Activity getCurrentActivity(){
        synchronized (instance){
            return currentActivity;
        }
    }

    @Override
    public void onCreate() {
        instance = this;
        configureUPush();
        super.onCreate();
    }

    private void congifureSMSSDK() {
        SMSSDK.initSDK(this, VessageConfig.getBahamutConfig().getSmsSDKAppkey(), VessageConfig.getBahamutConfig().getSmsSDKSecretKey());
    }

    public boolean startConfigure(){
        if(!firstLaunch){
            firstLaunch = true;
            switch (UserSetting.getAppConfig()){
                case UserSetting.APP_CONFIG_DEFAULT:loadConfigures(R.raw.bahamut_config);break;
                case UserSetting.APP_CONFIG_DEV:loadConfigures(R.raw.bahamut_config_dev);break;
            }
            registerActivityLifecycleCallbacks(onActivityLifecycle);
            configureServices();
            congifureSMSSDK();
        }
        return true;
    }

    private ActivityLifecycleCallbacks onActivityLifecycle = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            PushAgent.getInstance(getApplicationContext()).onAppStart();
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            setCurrentActivity(activity);
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

    private void configureUPush() {
        PushAgent mPushAgent = PushAgent.getInstance(getApplicationContext());
        mPushAgent.setNotificationClickHandler(notificationHandler);
        mPushAgent.setMessageHandler(new CustomUmengMessageHandler());
    }

    private UmengNotificationClickHandler notificationHandler = new UmengNotificationClickHandler(){
        @Override
        public void dealWithCustomAction(Context context, UMessage msg) {
            if(msg.custom.equals("OtherDeviceLogin")){
                onOtherDeviceLogin();
            }else if(msg.builder_id == CustomUmengMessageHandler.BUILDER_ID_NEW_VESSAGE){
                ConversationService conversationService = ServicesProvider.getService(ConversationService.class);
                if(conversationService != null && StringHelper.isStringNullOrEmpty(msg.text) == false) {
                    Conversation conversation = conversationService.getConversationByChatterId(msg.text);
                    if (conversation != null && getCurrentActivity() != null) {
                        ConversationViewActivity.openConversationView(getCurrentActivity(), conversation);
                        return;
                    }
                }
                launchApp(AppMain.this,msg);
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
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_WILL_LOGOUT, onUserWillLogout);
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_LOGOIN,onUserLogined);
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_LOGOUT,onUserLogout);
    }

    private Observer onUserLogined = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGE_SENDED,onVessageSended);
        }
    };

    private Observer onVessageSended = new Observer() {
        @Override
        public void update(ObserverState state) {
            MobclickAgent.onEvent(AppMain.this,"TotalPostVessages");
            SendVessageTask task = (SendVessageTask) state.getInfo();
            final String toMobile = task.toMobile;
            if(StringHelper.isStringNullOrEmpty(toMobile)){
                ProgressHUDHelper.showHud(AppMain.currentActivity,getResources().getString(R.string.vessage_sended),R.mipmap.check_mark,true);
            }else {
                ProgressHUDHelper.showHud(AppMain.currentActivity, getResources().getString(R.string.vessage_sended), R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                    @Override
                    public void onHudDismiss() {
                        sendNotifyFriendSMS(toMobile);
                    }
                });
            }
        }
    };

    private void sendNotifyFriendSMS(final String number) {

        if(UserSetting.isNotifySMSSendedToMobile(number)){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity)
                .setTitle(R.string.ask_send_notify_sms)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserSetting.setNotifySMSSendedToMobile(number);
                        Uri uri = Uri.parse("smsto:" + number);
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                        String sms_body = getResources().getString(R.string.notify_friend_sms_body);
                        String nickName = ServicesProvider.getService(UserService.class).getMyProfile().nickName;
                        String url = VessageConfig.getBahamutConfig().getBahamutAppOuterExecutorUrlPrefix() + StringHelper.getBASE64(nickName);
                        sendIntent.putExtra("sms_body", String.format("%s\n%s",sms_body,url));
                        currentActivity.startActivity(sendIntent);
                    }
                });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MobclickAgent.onEvent(AppMain.this,"CancelSendNotifySMS");
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private Observer onUserLogout = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGE_SENDED,onVessageSended);
        }
    };

    private Observer onUserWillLogin = new Observer() {
        @Override
        public void update(ObserverState state) {
            configureRealm(UserSetting.getUserId());
        }
    };

    private Observer onUserWillLogout = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.getService(UserService.class).removeUserDevice();
        }
    };

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
