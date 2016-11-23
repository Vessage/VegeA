package cn.bahamut.vessage.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import cn.bahamut.common.AESUtil;
import cn.bahamut.common.AndroidHelper;
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
import cn.bahamut.vessage.activities.vtm.VessageTimeMachine;
import cn.bahamut.vessage.conversation.chat.bubblevessage.BubbleVessageHandlerConfig;
import cn.bahamut.vessage.conversation.list.ConversationListActivity;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.handlers.FinishNormalVessageHandler;
import cn.bahamut.vessage.conversation.sendqueue.handlers.PostVessageHandler;
import cn.bahamut.vessage.conversation.sendqueue.handlers.SendAliOSSFileHandler;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.AppService;
import cn.bahamut.vessage.services.LocationService;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.AccountService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.VessageService;
import cn.smssdk.SMSSDK;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by alexchow on 16/4/1.
 */
public class AppMain extends Application{
    private static final int UI_ANIMATION_DELAY = 1000;
    static private AppMain instance;
    static private Activity currentActivity;
    static private String appId = "1029384756";
    private boolean firstLaunch = false;
    private IWXAPI wxapi;

    public static AppMain getInstance() {
        return instance;
    }

    private static Typeface appNameTypeFace;
    public static Typeface getAppnameTypeFace(){
        if(appNameTypeFace == null){
            appNameTypeFace = Typeface.createFromAsset(getInstance().getAssets(), "fonts/app_name.ttf");
        }
        return appNameTypeFace;
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
        super.onCreate();
        instance = this;
        initReam();
        configureUPush();
    }

    private void initReam() {
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfiguration); // Clean slate
        Realm.setDefaultConfiguration(realmConfiguration); // Make this Realm the default
    }

    private void congifureSMSSDK() {
        SMSSDK.initSDK(this, VessageConfig.getBahamutConfig().getSmsSDKAppkey(), VessageConfig.getBahamutConfig().getSmsSDKSecretKey());
    }

    public boolean startConfigure(){
        if(!firstLaunch){
            firstLaunch = true;
            switch (UserSetting.getAppConfig()){
                case UserSetting.APP_CONFIG_DEFAULT:loadConfigures(R.raw.bahamut_config);break;
                case UserSetting.APP_CONFIG_DEV:VessageConfig.loadBahamutConfig(TextHelper.readInputStreamText(this,R.raw.bahamut_config_dev));break;
            }
            registerActivityLifecycleCallbacks(onActivityLifecycle);
            configureServices();
            congifureSMSSDK();
            configureWX();
            configureUMeng();
            BubbleVessageHandlerConfig.loadHandlers();
        }
        return true;
    }

    private void configureUMeng() {
        if(AndroidHelper.isApkDebugable(AppMain.getInstance())){
            MobclickAgent.setDebugMode(true);
        }
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    private ActivityLifecycleCallbacks onActivityLifecycle = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            PushAgent.getInstance(activity).onAppStart();
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            setCurrentActivity(activity);
            if(AndroidHelper.isApkDebugable(AppMain.getInstance())){
                MobclickAgent.onResume(activity);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if(AndroidHelper.isApkDebugable(AppMain.getInstance())){
                MobclickAgent.onPause(activity);
            }
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

    private void configureWX(){
        String wxAppkey = VessageConfig.getBahamutConfig().getWechatAppkey();
        wxapi = WXAPIFactory.createWXAPI(this,wxAppkey,true);
        wxapi.registerApp(wxAppkey);
    }

    public IWXAPI getWechatApi(){
        return wxapi;
    }

    private void configureUPush() {
        final PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setNotificationClickHandler(new VessageUmengNotificationClickHandler());
        mPushAgent.setMessageHandler(new VessageUmengMessageHandler());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("UMessage","Start Regist UMessage Push");
                mPushAgent.register(new IUmengRegisterCallback() {
                    @Override
                    public void onSuccess(final String deviceToken) {
                        Log.w("UMessage","Regist UMessage Push Service Success");
                        UserSetting.setDeviceToken(deviceToken);
                        UserService service = ServicesProvider.getService(UserService.class);
                        if (service != null) {
                            service.registUserDeviceToken();
                        }
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        Log.w("UMessage","Regist UMessage Push Service Failure: " + s + "->"+ s1);
                    }
                });
            }
        }).start();
    }

    private void configureRealm(String userId){
        VessageMigration migration = new VessageMigration();
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(userId + ".realm")
                .schemaVersion(migration.schemaVersion)
                .migration(migration)
                .build();
        Realm.removeDefaultConfiguration();
        Realm.setDefaultConfiguration(config);
    }

    private void configureServices() {
        ServicesProvider.registService(new AccountService());
        ServicesProvider.registService(new FileService());
        ServicesProvider.registService(new UserService());
        ServicesProvider.registService(new ConversationService());
        ServicesProvider.registService(new VessageService());
        ServicesProvider.registService(new ExtraActivitiesService());
        ServicesProvider.registService(new LocationService());
        ServicesProvider.registService(new ChatGroupService());
        ServicesProvider.registService(new AppService());
        ServicesProvider.initServices(getApplicationContext());
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_WILL_LOGOIN, onUserWillLogin);
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_WILL_LOGOUT, onUserWillLogout);
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_LOGOIN,onUserLogined);
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_LOGOUT,onUserLogout);
    }

    private Observer onUserLogined = new Observer() {
        @Override
        public void update(ObserverState state) {

            SendVessageQueue.getInstance().init();
            SendVessageQueue.getInstance().registStepHandler(PostVessageHandler.HANDLER_NAME,new PostVessageHandler());
            SendVessageQueue.getInstance().registStepHandler(SendAliOSSFileHandler.HANDLER_NAME,new SendAliOSSFileHandler());
            SendVessageQueue.getInstance().registStepHandler(FinishNormalVessageHandler.HANDLER_NAME,new FinishNormalVessageHandler());
            VessageTimeMachine.initTimeMachine();
            ServicesProvider.getService(AppService.class).trySendFirstLaunchToServer();
        }
    };

    public static interface UserProfileAlertNoteUserHandler{
        void handle();
    }

    public static void showUserProfileAlert(Context context, VessageUser user, final UserProfileAlertNoteUserHandler noteUserHandler) {
        String msg;
        if (StringHelper.isNullOrEmpty(user.accountId)) {
            msg = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
        } else {
            msg = LocalizedStringHelper.getLocalizedString(R.string.account) + ":" + user.accountId;
        }
        String title = ServicesProvider.getService(UserService.class).getUserNoteOrNickName(user.userId);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg);
        if (noteUserHandler != null) {
            builder.setPositiveButton(R.string.note_conversation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    noteUserHandler.handle();
                }
            });
        }
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    private Observer onUserLogout = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageTimeMachine.releaseTimeMachine();
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
            String token = UserSetting.getDeviceToken();
            SendVessageQueue.getInstance().release();
            ServicesProvider.getService(UserService.class).removeUserDevice(token);

        }
    };

    public void loadConfigures(int configResId) {
        String json = TextHelper.readInputStreamText(getApplicationContext(),configResId);
        if(json != null){
            try {
                json = AESUtil.decrypt(appId,json);
                VessageConfig.loadBahamutConfig(json);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),R.string.read_config_error,Toast.LENGTH_LONG).show();
            }
        }
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
        apiClientInfo.apiServer = validateResult.getApiServer();
        apiClientInfo.appToken = validateResult.getAppToken();
        apiClientInfo.userId = validateResult.getUserId();
        APIClient apiClient = new APIClient();
        apiClient.setClientInfo(apiClientInfo);
        BahamutRFKit.instance.useClient(apiClient);
    }

    public void tryRegistDeviceToken(){
        if(!StringHelper.isStringNullOrWhiteSpace(UserSetting.getDeviceToken())){
            ServicesProvider.getService(UserService.class).registUserDeviceToken();
        }
    }

    static public void startEntryActivity(Activity context){
        Intent intent = new Intent(context,EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        context.finish();
        ServicesProvider.getService(VessageService.class).newVessageFromServer();
    }

    static public void startSignActivity(Activity context){
        startSignActivity(context,-1);
    }

    static public void startSignActivity(final Activity context, int toastMessageResId){
        if(toastMessageResId != -1){
            Toast.makeText(AppMain.currentActivity,toastMessageResId,Toast.LENGTH_LONG).show();
        }
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }else{
            intent = new Intent(context, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        context.startActivity(intent);
        context.finish();
    }

    public void showTellVegeToFriendsAlert(String message) {
        showTellVegeToFriendsAlert(message,R.string.tell_friends_alert_msg);
    }

    public void showTellVegeToFriendsAlert(final String message,int titleResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle(R.string.app_name);
        builder.setMessage(titleResId);
        builder.setPositiveButton(R.string.wechat_session, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendVegeLinkToWXFriends(SendMessageToWX.Req.WXSceneSession,message);
            }
        });

        if(wxapi.getWXAppSupportAPI() >= 0x21020001){
            builder.setNegativeButton(R.string.wechat_timeline, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendVegeLinkToWXFriends(SendMessageToWX.Req.WXSceneTimeline,LocalizedStringHelper.getLocalizedString(R.string.tell_friends_vege_msg));
                }
            });
        }

        builder.setCancelable(true);
        builder.show();
    }

    private void sendVegeLinkToWXFriends(int scene,String message){
        if(getWechatApi() == null){
            Toast.makeText(currentActivity,R.string.wxapi_not_ready,Toast.LENGTH_SHORT).show();
            return;
        }
        WXWebpageObject object = new WXWebpageObject();
        object.webpageUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=cn.bahamut.vessage";
        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.mediaObject = object;
        mediaMessage.title = LocalizedStringHelper.getLocalizedString(R.string.app_name);
        mediaMessage.description = message;
        Bitmap appIcon = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.app_icon));
        float scaleRate = 128.0f / appIcon.getWidth();//缩小的比例
        appIcon = ImageHelper.scaleImage(appIcon,scaleRate);
        mediaMessage.setThumbImage(appIcon);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = mediaMessage;
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.scene = scene;
        if(getWechatApi().sendReq(req)){
            Toast.makeText(currentActivity,R.string.jump_weixin,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(currentActivity,R.string.wxapi_not_ready,Toast.LENGTH_SHORT).show();
        }
    }
}
