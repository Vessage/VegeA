package cn.bahamut.vessage.usersettings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.message.PushAgent;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.client.AccountClient;
import cn.bahamut.restfulkit.models.MessageResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.AccountService;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText mOriginPassword;
    private EditText mNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usersetting_activity_change_password);
        findViewById(R.id.btn_change_password).setOnClickListener(onChangePasswordClick);
        mOriginPassword = (EditText)findViewById(R.id.et_password);
        mNewPassword = (EditText)findViewById(R.id.et_new_password);
    }

    private View.OnClickListener onChangePasswordClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String originPsw = mOriginPassword.getText().toString();
            String newPsw = mNewPassword.getText().toString();
            if(StringHelper.isPassword(originPsw)){
                if(StringHelper.isPassword(newPsw)){
                    if(originPsw.equals(newPsw)){
                        Toast.makeText(ChangePasswordActivity.this,R.string.password_same,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mOriginPassword.clearFocus();
                    mNewPassword.clearFocus();
                    final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ChangePasswordActivity.this);
                    ServicesProvider.getService(AccountService.class).changePassword(originPsw, newPsw, new AccountClient.ChangePasswordCallback() {
                        @Override
                        public void onChangePassword(boolean isDone, MessageResult errorMessage) {
                            hud.dismiss();
                            if(isDone){
                                ProgressHUDHelper.showHud(ChangePasswordActivity.this, R.string.change_password_suc, R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                                    @Override
                                    public void onHudDismiss() {
                                        finish();
                                    }
                                });
                            }else {
                                Toast.makeText(ChangePasswordActivity.this, LocalizedStringHelper.getLocalizedStringResId(errorMessage.getMsg()),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(ChangePasswordActivity.this,R.string.new_password_error_format,Toast.LENGTH_SHORT).show();
                    mNewPassword.requestFocus();
                }
            }else {
                Toast.makeText(ChangePasswordActivity.this,R.string.origin_password_error_format,Toast.LENGTH_SHORT).show();
                mOriginPassword.requestFocus();
            }
        }
    };
}
