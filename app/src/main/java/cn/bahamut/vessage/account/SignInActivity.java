package cn.bahamut.vessage.account;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.AccountService;

public class SignInActivity extends AppCompatActivity {

    private View mControlsView;
    private View mContentView;
    private EditText mLoginInfoEditText;
    private EditText mPasswordEditText;
    private Button mSignInButton;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private View.OnClickListener onClickSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ServicesProvider.getService(AccountService.class).signIn(mLoginInfoEditText.getText().toString(),mPasswordEditText.getText().toString());
        }
    };

    private void initControls(){
        mSignInButton = (Button)findViewById(R.id.btn_sign_in);
        mSignUpButton = (Button)findViewById(R.id.btn_no_account);
        mLoginInfoEditText = (EditText)findViewById(R.id.et_login_info);
        mPasswordEditText = (EditText)findViewById(R.id.et_password);
        mSignUpButton.setOnClickListener(onClickSignUp);
        mSignInButton.setOnClickListener(onClickSignIn);
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
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}
