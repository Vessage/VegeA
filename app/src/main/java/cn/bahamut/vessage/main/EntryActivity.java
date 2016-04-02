package cn.bahamut.vessage.main;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
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

    private static final int UI_ANIMATION_DELAY = 700;

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
        if (AppMain.instance.start()){
            ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
            if(UserSetting.isUserLogined()){
                ServicesProvider.instance.userLogin(UserSetting.getUserId());
            }else {
                startSignActivity();
            }
        }else{
            System.exit(0);
        }
    }

    private Observer onServiceReady = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY,onServiceReady);
            startMainActivity();
        }
    };

    private void startMainActivity(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showMainActivity();
            }
        }, UI_ANIMATION_DELAY);
    }

    private void showMainActivity() {
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
        finish();
    }

    private void startSignActivity(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showSignActivity();
            }
        }, UI_ANIMATION_DELAY);
    }

    private void showSignActivity(){
        Intent intent = null;
        if(UserSetting.getLastUserLoginedAccount() == null){
            intent = new Intent(this, SignUpActivity.class);
        }else{
            intent = new Intent(this, SignInActivity.class);
        }
        startActivity(intent);
        finish();
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
