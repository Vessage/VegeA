package cn.bahamut.vessage.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.Date;
import java.util.Random;
import java.util.Set;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.usersettings.ValidateMobileActivity;

public class EntryActivity extends Activity {

    private static final int REGIST_MOBILE_REQUEST_CODE = 1;
    private static final int UPLOAD_CHAT_BCG_REQUEST_CODE = 2;
    private static final int[] mottos = new int[]{R.string.vege_motto_0};
    private static int mottoIndex = new Random(new Date().getTime()).nextInt(mottos.length);
    private static String TAG = "EntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, String.format("App Build Version:%d", UserSetting.getCachedBuildVersion()));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_entry);
        start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView mMottoTextView = (TextView) findViewById(R.id.motto_text_view);
        mottoIndex += 1;
        mottoIndex = mottoIndex % mottos.length;
        mMottoTextView.setText(mottos[mottoIndex]);
    }

    private void start() {
        Log.i(TAG, "VG Start!");
        if (AppMain.getInstance().startConfigure()) {
            Log.i(TAG, "VG Configuration Completed");
            if (UserSetting.isUserLogined()) {
                ValidateResult storedValidateResult = UserSetting.getUserValidateResult();
                if (storedValidateResult == null || !storedValidateResult.checkValidateInfoCorrect()) {
                    UserSetting.setUserLogout();
                    AppMain.startSignActivity(this, R.string.invalidate_user_data);
                } else {
                    AppMain.getInstance().useValidateResult(storedValidateResult);
                    ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
                    ServicesProvider.instance.addObserver(ServicesProvider.NOTIFY_INIT_SERVICE_FAILED, onInitServiceFailed);
                    ServicesProvider.userLogin(UserSetting.getUserId());
                }
            } else {
                AppMain.startSignActivity(this);
            }
        } else {
            Log.i(TAG, "VG Fatal");
            MobclickAgent.onKillProcess(this);
            System.exit(0);
        }
    }

    private Observer onInitServiceFailed = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_INIT_SERVICE_FAILED, onInitServiceFailed);
            Toast.makeText(EntryActivity.this, state.getInfo().toString(), Toast.LENGTH_LONG).show();
            ServicesProvider.userLogout();
            UserSetting.setUserLogout();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppMain.startSignActivity(EntryActivity.this);
                }
            }, 2000);
        }
    };

    private Observer onServiceReady = new Observer() {
        @Override
        public void update(ObserverState state) {
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_ALL_SERVICES_READY, onServiceReady);
            ServicesProvider.instance.deleteObserver(ServicesProvider.NOTIFY_INIT_SERVICE_FAILED, onInitServiceFailed);
            UserService userService = ServicesProvider.getService(UserService.class);
            if (!userService.isMyMobileValidated()) {
                ValidateMobileActivity.startRegistMobileActivity(EntryActivity.this, REGIST_MOBILE_REQUEST_CODE, true);
            } else {
                Set<String> chattingUserIds = ServicesProvider.getService(ConversationService.class).getChattingUserIds();
                int cnt = ServicesProvider.getService(UserService.class).clearTempUsers(chattingUserIds);
                Log.i(TAG, "Removed Temp Users:" + cnt);
                AppMain.startMainActivity(EntryActivity.this);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UserService userService = ServicesProvider.getService(UserService.class);
        if (REGIST_MOBILE_REQUEST_CODE == requestCode) {
            if (resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS) {
                AppMain.startMainActivity(EntryActivity.this);
            } else {
                askToValidateMobile();
            }
        } else if (UPLOAD_CHAT_BCG_REQUEST_CODE == requestCode) {
            if (resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS) {
                if (userService.isMyMobileValidated()) {
                    AppMain.startMainActivity(EntryActivity.this);
                } else {
                    askToValidateMobile();
                }
            }
        }
    }

    /*
    private void askUploadChatBcg() {
        final String key = UserSetting.generateUserSettingKey("SET_CHAT_BCG_LATER");
        if(UserSetting.getUserSettingPreferences().getBoolean(key,false)){
            AppMain.startMainActivity(EntryActivity.this);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
        builder.setTitle(R.string.need_upload_chat_bcg_title);
        builder.setMessage(R.string.need_upload_chat_bcg_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateChatImageActivity.startUpdateChatImageActivity(EntryActivity.this,UPLOAD_CHAT_BCG_REQUEST_CODE);
            }
        });

        builder.setNegativeButton(R.string.jump_set_chat_bcg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.getUserSettingPreferences().edit().putBoolean(key,true).commit();
                AppMain.startMainActivity(EntryActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
*/

    private void askToValidateMobile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntryActivity.this);
        builder.setTitle(R.string.need_validate_mobile);
        builder.setMessage(R.string.validate_mobile_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ValidateMobileActivity.startRegistMobileActivity(EntryActivity.this, REGIST_MOBILE_REQUEST_CODE, true);
            }
        });

        builder.setNegativeButton(R.string.exchange_account, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.setUserLogout();
                ServicesProvider.userLogout();
                AppMain.startSignActivity(EntryActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}