package cn.bahamut.vessage.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.codec1.digest.DigestUtils;

import cn.bahamut.vessage.R;

public class DevPanelActivity extends AppCompatActivity {
    private static final String GOD_CODE = "0992369b28f2d4903851f17382cc884a97b6ecaf939fc02063dd113a21ee334e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_panel);
        findViewById(R.id.btn_close).setOnClickListener(onClickClose);
        findViewById(R.id.btn_server_67).setOnClickListener(onClickSelectServerButton);
        findViewById(R.id.btn_server_remote).setOnClickListener(onClickSelectServerButton);
    }

    private View.OnClickListener onClickSelectServerButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_server_67:
                    loadAppConfig(R.raw.bahamut_config_dev);
                    UserSetting.setAppConfig(UserSetting.APP_CONFIG_DEV);
                    break;
                case R.id.btn_server_remote:
                    loadAppConfig(R.raw.bahamut_config);
                    UserSetting.setAppConfig(UserSetting.APP_CONFIG_DEFAULT);
                    break;
            }
            if(v instanceof Button){
                Toast.makeText(DevPanelActivity.this,((Button) v).getText().toString(),Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void loadAppConfig(int configResId){
        AppMain.getInstance().loadConfigures(configResId);
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
