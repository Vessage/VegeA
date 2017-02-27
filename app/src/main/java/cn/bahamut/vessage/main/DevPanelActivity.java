package cn.bahamut.vessage.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.commons.codec1.digest.DigestUtils;

import cn.bahamut.common.TextHelper;
import cn.bahamut.vessage.R;

public class DevPanelActivity extends AppCompatActivity {
    private static final String GOD_CODE = "0992369b28f2d4903851f17382cc884a97b6ecaf939fc02063dd113a21ee334e";

    private Switch godSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dev_activity_dev_panel);
        findViewById(R.id.btn_close).setOnClickListener(onClickClose);
        findViewById(R.id.btn_server_dev).setOnClickListener(onClickSelectServerButton);
        findViewById(R.id.btn_server_remote).setOnClickListener(onClickSelectServerButton);
        findViewById(R.id.btn_server_168).setOnClickListener(onClickSelectServerButton);
        findViewById(R.id.btn_server_67).setOnClickListener(onClickSelectServerButton);
        godSwitch = (Switch)findViewById(R.id.god_mode);
        godSwitch.setChecked(UserSetting.godMode);
        godSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSetting.godMode = !UserSetting.godMode;
                godSwitch.setChecked(UserSetting.godMode);
            }
        });
    }

    private View.OnClickListener onClickSelectServerButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_server_remote:
                    AppMain.getInstance().loadConfigures(R.raw.bahamut_config);
                    UserSetting.setAppConfig(UserSetting.APP_CONFIG_DEFAULT);
                    break;
                case R.id.btn_server_dev:
                    loadAppConfig(AppMain.getInstance().getResId("bahamut_config_dev", "raw"));
                    UserSetting.setAppConfig(UserSetting.APP_CONFIG_DEV);
                    break;
                case R.id.btn_server_67:
                    loadAppConfig(AppMain.getInstance().getResId("bahamut_config_67", "raw"));
                    UserSetting.setAppConfig(UserSetting.APP_CONFIG_DEV);
                case R.id.btn_server_168:
                    loadAppConfig(AppMain.getInstance().getResId("bahamut_config_168", "raw"));
                    UserSetting.setAppConfig(UserSetting.APP_CONFIG_DEV);
            }
            if(v instanceof Button){
                Toast.makeText(DevPanelActivity.this,((Button) v).getText().toString(),Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void loadAppConfig(int configResId){
        String config = TextHelper.readInputStreamText(this,configResId);
        VessageConfig.loadBahamutConfig(config);
    }

    private View.OnClickListener onClickClose = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    public static boolean checkAndShowDevPanel(Context context,String username, String password){
        if(checkIsGodCode(username,password)){
            Intent intent = new Intent(context,DevPanelActivity.class);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private static boolean checkIsGodCode(String username, String password) {
        return DigestUtils.sha256Hex(username+password).equals(GOD_CODE);
    }
}
