package cn.bahamut.vessage.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import cn.bahamut.vessage.main.DevPanelActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.AccountService;

public class SignInActivity extends Activity {

    private EditText mLoginInfoEditText;
    private EditText mPasswordEditText;
    private Button mSignInButton;
    private Button mSignUpButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);
        initControls();

    }

    private View.OnClickListener onClickSignUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startSignUpActivity();
            finish();
        }
    };

    private void startSignUpActivity(){
        Intent intent = new Intent();
        intent.setClass(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLogining(){
        mLoginInfoEditText.setEnabled(false);
        mPasswordEditText.setEnabled(false);
        mSignInButton.setVisibility(View.INVISIBLE);
        mSignUpButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setLoginCompleted(){
        mLoginInfoEditText.setEnabled(true);
        mPasswordEditText.setEnabled(true);
        mSignUpButton.setVisibility(View.VISIBLE);
        mSignInButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener onClickSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLoginInfoEditText.clearFocus();
            mPasswordEditText.clearFocus();
            String username = mLoginInfoEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            if(DevPanelActivity.checkAndShowDevPanel(SignInActivity.this,username,password)){
                return;
            }
            if(!checkLoginFieldsIsValid()){
                return;
            }
            setLogining();
            AccountService aService = ServicesProvider.getService(AccountService.class);
            aService.signIn(username, password, new AccountService.SignCompletedCallback() {

                @Override
                public void onSignCompleted(ValidateResult result) {
                    ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServicesReady);
                    ServicesProvider.userLogin(result.getUserId());
                    setLoginCompleted();
                }

                @Override
                public void onSignError(String errorMessage) {
                    Toast.makeText(SignInActivity.this, LocalizedStringHelper.getLocalizedStringResId(errorMessage), Toast.LENGTH_SHORT).show();
                    setLoginCompleted();
                }
            });
        }
    };

    private boolean checkLoginFieldsIsValid(){
        if(!StringHelper.isUsername(mLoginInfoEditText.getText().toString())){
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
            AppMain.startEntryActivity(SignInActivity.this);
        }
    };

    private void initControls(){
        mSignInButton = (Button)findViewById(R.id.btn_sign_in);
        mSignUpButton = (Button)findViewById(R.id.btn_no_account);
        mLoginInfoEditText = (EditText)findViewById(R.id.et_login_info);
        mPasswordEditText = (EditText)findViewById(R.id.et_password);
        mSignUpButton.setOnClickListener(onClickSignUp);
        mSignInButton.setOnClickListener(onClickSignIn);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_loading);
        mProgressBar.setVisibility(View.INVISIBLE);

        String account = UserSetting.getLastUserLoginedAccount();
        if(!StringHelper.isStringNullOrEmpty(account)){
            mLoginInfoEditText.setText(account);
            mPasswordEditText.requestFocus();
        }
    }
}
