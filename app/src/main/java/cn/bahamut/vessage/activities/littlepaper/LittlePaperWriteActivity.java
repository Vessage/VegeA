package cn.bahamut.vessage.activities.littlepaper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.UsersListActivity;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class LittlePaperWriteActivity extends Activity {

    private static final int SELECT_USER_REQUEST_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_little_paper_write);

        ImageView backgroundImageView = (ImageView)findViewById(R.id.bcg_img_view);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_bcg));
        backgroundImageView.setImageBitmap(bitmap);

        findViewById(R.id.send_little_paper).setOnClickListener(onClickSendLittlePaper);
    }

    private View.OnClickListener onClickSendLittlePaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText receiverInfo = (EditText) findViewById(R.id.receiver_Info_et);
            EditText content = (EditText) findViewById(R.id.content_et);
            String receiverInfoText = receiverInfo.getText().toString();
            String contentText = content.getText().toString();
            if(StringHelper.isStringNullOrWhiteSpace(receiverInfoText)){
                Toast.makeText(LittlePaperWriteActivity.this,R.string.little_paper_receiver_info_is_null,Toast.LENGTH_SHORT).show();
                receiverInfo.requestFocus();
            }else if (StringHelper.isStringNullOrWhiteSpace(contentText)) {
                Toast.makeText(LittlePaperWriteActivity.this,R.string.little_paper_content_is_null,Toast.LENGTH_SHORT).show();
                content.requestFocus();
            }else {
                String title =  LocalizedStringHelper.getLocalizedString(R.string.little_paper_select_receiver);
                new UsersListActivity.ShowSelectUserActivityBuilder(LittlePaperWriteActivity.this)
                        .setConversationUserIdList()
                        .setCanSelectMobile(true)
                        .setCanSelectNearUser(true)
                        .setTitle(title)
                        .showActivity(SELECT_USER_REQUEST_ID);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_USER_REQUEST_ID && requestCode == resultCode){
            List<String> userIds = data.getStringArrayListExtra(UsersListActivity.SELECTED_USER_IDS_ARRAY_KEY);
            sendPaperToUser(userIds.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendPaperToUser(final String userId) {
        EditText receiverInfo = (EditText) findViewById(R.id.receiver_Info_et);
        EditText content = (EditText) findViewById(R.id.content_et);
        String receiverInfoText = receiverInfo.getText().toString();
        String contentText = content.getText().toString();

        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperWriteActivity.this);
        LittlePaperManager.getInstance().newPaperMessage(contentText, receiverInfoText, userId, new LittlePaperManager.OnNewPaperMessagePost() {
            @Override
            public void onNewPaperMessagePost(boolean suc) {
                hud.dismiss();
                if(suc){
                    MobclickAgent.onEvent(LittlePaperWriteActivity.this,"LittlePaper_PostNew");
                    ProgressHUDHelper.showHud(LittlePaperWriteActivity.this, R.string.little_paper_send_suc, R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                        @Override
                        public void onHudDismiss() {
                            VessageUser user = ServicesProvider.getService(UserService.class).getUserById(userId);
                            if(user != null && StringHelper.isStringNullOrWhiteSpace(user.accountId)){
                                String msg = LocalizedStringHelper.getLocalizedString(R.string.little_paper_tell_friend_get_paper);
                                AppMain.getInstance().showTellVegeToFriendsAlert(msg,R.string.tell_friends_alert_msg_no_regist);
                            }else {
                                finish();
                            }

                        }
                    });
                }else {
                    ProgressHUDHelper.showHud(LittlePaperWriteActivity.this, R.string.little_paper_send_failure, R.mipmap.cross_mark,true);
                }
            }
        });
    }
}
