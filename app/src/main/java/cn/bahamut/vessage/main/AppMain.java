package cn.bahamut.vessage.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.account.SignInActivity;
import cn.bahamut.vessage.account.SignUpActivity;
import cn.bahamut.vessage.conversation.ConversationListActivity;
import cn.bahamut.vessage.services.AccountService;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;
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
            configureServices();
            return true;
        }
        return true;
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
        ServicesProvider.registService(new UserService());
        ServicesProvider.registService(new ConversationService());
        ServicesProvider.registService(new VessageService());
        ServicesProvider.initServices();
        ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_USER_WILL_LOGOIN, onUserWillLogin);
    }

    private Observer onUserWillLogin = new Observer() {
        @Override
        public void update(ObserverState state) {
            configureBahamutKit();
            configureRealm(UserSetting.getUserId());
        }
    };

    private void configureBahamutKit() {

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
