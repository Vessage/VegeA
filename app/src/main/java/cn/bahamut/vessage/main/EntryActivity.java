package cn.bahamut.vessage.main;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.account.SignInActivity;
import cn.bahamut.vessage.account.SignUpActivity;
import cn.bahamut.vessage.conversation.ConversationListActivity;
import cn.bahamut.vessage.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class EntryActivity extends AppCompatActivity {

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
        if (AppMain.instance.start(this.getApplicationContext())){
            if(UserSetting.isUserLogined()){
                ValidateResult storedValidateResult = UserSetting.getUserValidateResult();
                if(storedValidateResult == null){
                    UserSetting.setUserLogout();
                    AppMain.startSignActivity(this);
                }else{
                    AppMain.instance.useValidateResult(storedValidateResult);
                    ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
                    ServicesProvider.instance.userLogin(UserSetting.getUserId());
                }
            }else {
                AppMain.startSignActivity(this);
            }
        }else{
            System.exit(0);
        }
    }

    private Observer onServiceReady = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
            AppMain.startMainActivity(EntryActivity.this);
        }
    };

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
