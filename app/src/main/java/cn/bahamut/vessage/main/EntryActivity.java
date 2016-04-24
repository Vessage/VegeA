package cn.bahamut.vessage.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.ValidateMobileActivity;
import cn.bahamut.vessage.services.UserService;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class EntryActivity extends AppCompatActivity {

    private static final int REGIST_MOBILE_REQUEST_CODE = 1;
    private View mContentView;
    private View mControlsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        initControls();
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
            if(ServicesProvider.getService(UserService.class).isMyMobileValidated()){
                AppMain.startMainActivity(EntryActivity.this);
            }else {
                ValidateMobileActivity.startRegistMobileActivity(EntryActivity.this,REGIST_MOBILE_REQUEST_CODE);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REGIST_MOBILE_REQUEST_CODE == requestCode){
            if(resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS){
                AppMain.startMainActivity(EntryActivity.this);
            }else {
                askToValidateMobile();
            }
        }
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

    private void initControls(){

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

}
