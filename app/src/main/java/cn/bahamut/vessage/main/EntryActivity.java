package cn.bahamut.vessage.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;

import com.umeng.analytics.MobclickAgent;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.usersettings.ChangeChatBackgroundActivity;
import cn.bahamut.vessage.usersettings.ValidateMobileActivity;

public class EntryActivity extends Activity {

    private static final int REGIST_MOBILE_REQUEST_CODE = 1;
    private static final int UPLOAD_CHAT_BCG_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_entry);
        start();
    }

    private void start(){
        if (AppMain.getInstance().startConfigure()){
            if(UserSetting.isUserLogined()){
                ValidateResult storedValidateResult = UserSetting.getUserValidateResult();
                if(storedValidateResult == null){
                    UserSetting.setUserLogout();
                    AppMain.startSignActivity(this);
                }else{
                    AppMain.getInstance().useValidateResult(storedValidateResult);
                    ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
                    ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_INIT_SERVICE_FAILED, onInitServiceFailed);
                    ServicesProvider.instance.userLogin(UserSetting.getUserId());
                }
            }else {
                AppMain.startSignActivity(this);
            }
        }else{
            MobclickAgent.onKillProcess(this);
            System.exit(0);
        }
    }

    private Observer onInitServiceFailed = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_INIT_SERVICE_FAILED, onServiceReady);
            AppMain.startSignActivity(EntryActivity.this);
        }
    };

    private Observer onServiceReady = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
            UserService userService = ServicesProvider.getService(UserService.class);
            if(!userService.isMyMobileValidated()){
                ValidateMobileActivity.startRegistMobileActivity(EntryActivity.this,REGIST_MOBILE_REQUEST_CODE);
            }else if(!userService.isMyProfileHaveChatBackground()){
                askUploadChatBcg();
            }else {
                AppMain.startMainActivity(EntryActivity.this);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UserService userService = ServicesProvider.getService(UserService.class);
        if(REGIST_MOBILE_REQUEST_CODE == requestCode){
            if(resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS){
                if(userService.isMyProfileHaveChatBackground()){
                    AppMain.startMainActivity(EntryActivity.this);
                }else {
                    askUploadChatBcg();
                }
            }else {
                askToValidateMobile();
            }
        }else if(UPLOAD_CHAT_BCG_REQUEST_CODE == requestCode){
            if(resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS){
                if(userService.isMyMobileValidated()) {
                    AppMain.startMainActivity(EntryActivity.this);
                }else {
                    askToValidateMobile();
                }
            }else {
                askUploadChatBcg();
            }
        }
    }

    private void askUploadChatBcg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
        builder.setTitle(R.string.need_upload_chat_bcg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChangeChatBackgroundActivity.startChangeChatBackgroundActivity(EntryActivity.this,UPLOAD_CHAT_BCG_REQUEST_CODE);
            }
        });

        builder.setNegativeButton(R.string.exchange_account, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.setUserLogout();
                ServicesProvider.userLogout();
                AppMain.startSignActivity(EntryActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void askToValidateMobile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
        builder.setTitle(R.string.need_validate_mobile);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ValidateMobileActivity.startRegistMobileActivity(EntryActivity.this,REGIST_MOBILE_REQUEST_CODE);
            }
        });

        builder.setNegativeButton(R.string.exchange_account, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.setUserLogout();
                ServicesProvider.userLogout();
                AppMain.startSignActivity(EntryActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

}
