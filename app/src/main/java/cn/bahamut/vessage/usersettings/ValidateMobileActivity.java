package cn.bahamut.vessage.usersettings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import java.util.Timer;
import java.util.TimerTask;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.main.VessageConfig;
import cn.bahamut.vessage.services.user.UserService;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ValidateMobileActivity extends AppCompatActivity {

    private static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";
    public static final int RESULT_CODE_VALIDATE_SUCCESS = 1;
    private static final int MAX_WAIT_SMS_SECONDS = 30;

    private EditText mMobileEditText;
    private TextView mCountryCodeTextView;
    private Button mGetSmsButton;
    private View mGetMobileViewsContainer;
    private View mValidateMobileContainer;
    private Button mReGetSmsButton;
    private Button mValidateCodeButton;
    private EditText mCodeEditText;

    private Timer regetTimer;
    private TimerTask regetTimerTask;
    private volatile int secondsNeedWaitToRegetSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usersetting_activity_validate_mobile);
        getSupportActionBar().setTitle(R.string.validate_sms_mobile);
        mGetMobileViewsContainer = findViewById(R.id.mobile_container);
        mValidateMobileContainer = findViewById(R.id.validate_sms_code_container);
        mMobileEditText = (EditText)findViewById(R.id.et_mobile);
        mCountryCodeTextView = (TextView) findViewById(R.id.tv_country_code);
        mGetSmsButton = (Button)findViewById(R.id.btn_get_sms);
        mGetSmsButton.setOnClickListener(onClickGetSmsButton);
        mReGetSmsButton = (Button)findViewById(R.id.btn_reget_code);
        mReGetSmsButton.setOnClickListener(onClickReGetSmsButton);
        mValidateCodeButton = (Button)findViewById(R.id.btn_validate_code);
        mValidateCodeButton.setOnClickListener(onClickValidateCodeButton);
        mCodeEditText = (EditText)findViewById(R.id.et_sms_code);
        mValidateMobileContainer.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener onClickReGetSmsButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mGetMobileViewsContainer.setVisibility(View.VISIBLE);
            mValidateMobileContainer.setVisibility(View.INVISIBLE);
            mMobileEditText.setEnabled(true);
            mGetSmsButton.setEnabled(true);
            getSupportActionBar().setTitle(R.string.validate_sms_mobile);
        }
    };

    private View.OnClickListener onClickValidateCodeButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(StringHelper.isNullOrEmpty(mCodeEditText.getText().toString())){
                Toast.makeText(ValidateMobileActivity.this,R.string.input_validate_code,Toast.LENGTH_SHORT).show();
                return;
            }
            if(!(mCodeEditText.getText().toString().matches("^[0-9]{4}$"))){
                Toast.makeText(ValidateMobileActivity.this,R.string.invalid_sms_code,Toast.LENGTH_SHORT).show();
                return;
            }
            final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ValidateMobileActivity.this);
            ServicesProvider.getService(UserService.class).validateMobile(VessageConfig.getBahamutConfig().getSmsSDKAppkey(),mMobileEditText.getText().toString(), mCountryCodeTextView.getText().toString(), mCodeEditText.getText().toString(), new UserService.MobileValidateCallback() {
                @Override
                public void onValidateMobile(boolean validated, boolean isBindedNewAccount, String newAccountUserId) {
                    hud.dismiss();
                    if(isBindedNewAccount){
                        ValidateResult storedInfo = UserSetting.getUserValidateResult();
                        storedInfo.setUserId(newAccountUserId);
                        UserSetting.setUserId(newAccountUserId);
                        UserSetting.setUserValidateResult(storedInfo);
                        ServicesProvider.userLogout();
                        AppMain.startEntryActivity(ValidateMobileActivity.this);
                    }else if(validated){
                        MobclickAgent.onEvent(ValidateMobileActivity.this,"Vege_FinishValidateMobile");
                        finishAndReturnResult();
                    }else {
                        Toast.makeText(ValidateMobileActivity.this,R.string.validate_sms_code_fail,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    private void finishAndReturnResult() {
        setResult(RESULT_CODE_VALIDATE_SUCCESS);
        finishActivity(getIntent().getIntExtra(KEY_REQUEST_CODE,0));
        finish();
    }

    private View.OnClickListener onClickGetSmsButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String mobile = mMobileEditText.getText().toString();
            if(!StringHelper.isMobileNumber(mobile)){
                Toast.makeText(ValidateMobileActivity.this,R.string.invalid_mobile,Toast.LENGTH_LONG).show();
                return;
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ValidateMobileActivity.this);
                builder.setTitle(getResources().getString(R.string.confirm_send_sms_format,mCountryCodeTextView.getText().toString(),mMobileEditText.getText().toString()));
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGetSmsButton.setEnabled(false);
                        sendSms();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        }
    };

    private EventHandler eventHandler = new EventHandler(){
        @Override
        public void afterEvent(int event, int result, Object data) {
            if (result == SMSSDK.RESULT_COMPLETE) {
                //回调完成
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    //提交验证码成功
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    //获取验证码成功
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    //返回支持发送验证码的国家列表
                } else if(event == SMSSDK.RESULT_ERROR){
                    mGetSmsButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mGetSmsButton.setEnabled(true);
                        }
                    });
                }
            }
        }
    };

    private void startReGetTimer() {
        if(regetTimer != null){
            regetTimer.cancel();
        }
        regetTimer = new Timer();
        if(regetTimerTask != null){
            regetTimerTask.cancel();
        }
        regetTimerTask = getRegetTimerTask();

        secondsNeedWaitToRegetSMS = MAX_WAIT_SMS_SECONDS;
        String waitString = String.format(getResources().getString(R.string.wait_sms_tips_format),String.valueOf(secondsNeedWaitToRegetSMS));
        mReGetSmsButton.setText(waitString);
        mReGetSmsButton.setEnabled(false);
        regetTimer.schedule(regetTimerTask,0,1000);
    }

    private TimerTask getRegetTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                secondsNeedWaitToRegetSMS--;
                if(secondsNeedWaitToRegetSMS <= 0){
                    regetTimer.cancel();
                    mReGetSmsButton.post(new Runnable() {
                        @Override
                        public void run() {
                            mReGetSmsButton.setText(R.string.reget_sms);
                            mReGetSmsButton.setEnabled(true);
                        }
                    });
                }else {
                    mReGetSmsButton.post(new Runnable() {
                        @Override
                        public void run() {
                            String waitString = String.format(getResources().getString(R.string.wait_sms_tips_format),String.valueOf(secondsNeedWaitToRegetSMS));
                            mReGetSmsButton.setText(waitString);
                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    private void sendSms() {
        if (!AndroidHelper.isApkDebugable(ValidateMobileActivity.this)){
            SMSSDK.registerEventHandler(eventHandler);
            SMSSDK.getVerificationCode(mCountryCodeTextView.getText().toString(), mMobileEditText.getText().toString());
        }
        mMobileEditText.setEnabled(false);
        mGetMobileViewsContainer.setVisibility(View.INVISIBLE);
        mValidateMobileContainer.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(R.string.input_validate_code);
        mCodeEditText.clearFocus();
        startReGetTimer();
    }

    static public void startRegistMobileActivity(Activity context,int requestCode){
        Intent intent = new Intent(context, ValidateMobileActivity.class);
        intent.putExtra(KEY_REQUEST_CODE,requestCode);
        context.startActivityForResult(intent,requestCode);
    }
}
