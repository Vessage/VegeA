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

    private View.OnClickListener onClickSignUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            signUp();
        }
    };

    private void signUp(){
        if(!checkLoginFieldsIsValid()){
            return;
        }
        AccountService aService = ServicesProvider.getService(AccountService.class);
        mSignUpButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        aService.signUp(mUsernameEditText.getText().toString(), mPasswordEditText.getText().toString(), new AccountService.SignCompletedCallback() {

            @Override
            public void onSignCompleted(ValidateResult result) {
                mSignUpButton.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServicesReady);
                ServicesProvider.userLogin(result.getUserId());
            }

            @Override
            public void onSignError(String errorMessage) {
                mSignUpButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
            Toast.makeText(this,R.string.username_test_hint,Toast.LENGTH_LONG).show();
            return false;
        }else if(!StringHelper.isPassword(mPasswordEditText.getText().toString())){
            Toast.makeText(this,R.string.password_test_hint,Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private Observer onServicesReady = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServicesReady);
            AppMain.startMainActivity(SignUpActivity.this);
            finish();
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
