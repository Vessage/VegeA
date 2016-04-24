package cn.bahamut.vessage.account;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.Localizable;
import cn.bahamut.vessage.services.AccountService;

public class SignUpActivity extends AppCompatActivity {

    private View mControlsView;
    private View mContentView;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mSignInButton;
    private Button mSignUpButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initControls();

    }

    private void setLogining(){
        mUsernameEditText.setEnabled(false);
        mPasswordEditText.setEnabled(false);
        mSignInButton.setVisibility(View.INVISIBLE);
        mSignUpButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setLoginCompleted(){
        mUsernameEditText.setEnabled(true);
        mPasswordEditText.setEnabled(true);
        mSignUpButton.setVisibility(View.VISIBLE);
        mSignInButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener onClickSignUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            signUp();
        }
    };

    private void signUp(){
        mUsernameEditText.clearFocus();
        mPasswordEditText.clearFocus();
        if(!checkLoginFieldsIsValid()){
            return;
        }
        setLogining();
        AccountService aService = ServicesProvider.getService(AccountService.class);
        aService.signUp(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString(), new AccountService.SignCompletedCallback() {

            @Override
            public void onSignCompleted(ValidateResult result) {
                ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServicesReady);
                ServicesProvider.userLogin(result.getUserId());
                setLoginCompleted();
            }

            @Override
            public void onSignError(String errorMessage) {
                Toast.makeText(SignUpActivity.this, Localizable.getLocalizableResId(errorMessage), Toast.LENGTH_SHORT).show();
                setLoginCompleted();
            }
        });
    }

    private View.OnClickListener onClickSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    };

    private boolean checkLoginFieldsIsValid(){
        if(!StringHelper.isUsername(mUsernameEditText.getText().toString())){
            Toast.makeText(this,R.string.username_test_hint,Toast.LENGTH_SHORT).show();
            return false;
        }else if(!StringHelper.isPassword(mPasswordEditText.getText().toString())){
            Toast.makeText(this,R.string.password_test_hint,Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Observer onServicesReady = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServicesReady);
            AppMain.startEntryActivity(SignUpActivity.this);
        }
    };

    private void initControls(){
        mSignInButton = (Button)findViewById(R.id.btn_has_account);
        mSignUpButton = (Button)findViewById(R.id.btn_sign_up);
        mUsernameEditText = (EditText)findViewById(R.id.et_username);
        mPasswordEditText = (EditText)findViewById(R.id.et_password);
        mSignUpButton.setOnClickListener(onClickSignUp);
        mSignInButton.setOnClickListener(onClickSignIn);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_loading);
        mProgressBar.setVisibility(View.INVISIBLE);
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
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
