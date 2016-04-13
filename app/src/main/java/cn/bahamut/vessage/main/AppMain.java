package cn.bahamut.vessage.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.CharBuffer;

import cn.bahamut.common.JsonHelper;
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
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by alexchow on 16/4/1.
 */
public class AppMain {
    private static final int UI_ANIMATION_DELAY = 700;
    static public final AppMain instance = new AppMain();
    static private Context applicationContext;
    static public boolean firstRun = true;
    public boolean start(Context context){
        applicationContext = context;
        if(firstRun){
            firstRun = false;
            loadConfigures();
            configureServices();
            return true;
        }
        return true;
    }

    private void loadConfigures() {
        InputStream inputStream = getApplicationContext().getResources().openRawResource(R.raw.bahamut_config_dev);
        String json = TextHelper.readInputStreamText(inputStream);
        if(json != null){
            VessageConfig.loadBahamutConfig(json);
        }
    }

    private void configureRealm(String userId){
        Realm.removeDefaultConfiguration();
        RealmConfiguration config = new RealmConfiguration.Builder(applicationContext)
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
        ServicesProvider.initServices(applicationContext);
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_WILL_LOGOIN, onUserWillLogin);
    }

    private Observer onUserWillLogin = new Observer() {
        @Override
        public void update(ObserverState state) {
            configureRealm(UserSetting.getUserId());
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

    static public Context getApplicationContext(){
        return applicationContext;
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
            //TODO: remove test
            //intent = new Intent(context, SignUpActivity.class);
            intent = new Intent(context, SignInActivity.class);
        }else{
            intent = new Intent(context, SignInActivity.class);
        }
        context.startActivity(intent);
        context.finish();
    }
}
