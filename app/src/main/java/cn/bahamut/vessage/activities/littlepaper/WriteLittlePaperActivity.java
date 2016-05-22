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

import java.util.List;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.UsersListActivity;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;

public class WriteLittlePaperActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_write_little_paper);

        ImageView backgroundImageView = (ImageView)findViewById(R.id.backgroundImageView);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_bcg));
        backgroundImageView.setImageBitmap(bitmap);

        findViewById(R.id.send_little_paper).setOnClickListener(onClickSendLittlePaper);
    }

    private View.OnClickListener onClickSendLittlePaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText receiverInfo = (EditText) findViewById(R.id.receiverInfoEditText);
            EditText content = (EditText) findViewById(R.id.contentEditText);
            String receiverInfoText = receiverInfo.getText().toString();
            String contentText = content.getText().toString();
            if(StringHelper.isStringNullOrWhiteSpace(receiverInfoText)){
                Toast.makeText(WriteLittlePaperActivity.this,R.string.receiver_info_is_null,Toast.LENGTH_SHORT).show();
                receiverInfo.requestFocus();
            }else if (StringHelper.isStringNullOrWhiteSpace(contentText)) {
                Toast.makeText(WriteLittlePaperActivity.this,R.string.paper_content_is_null,Toast.LENGTH_SHORT).show();
                content.requestFocus();
            }else {
                UsersListActivity.showSelectUserActivity(WriteLittlePaperActivity.this,false);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == UsersListActivity.USERS_LIST_ACTIVITY_MODE_SELECTION && requestCode == resultCode){
            EditText receiverInfo = (EditText) findViewById(R.id.receiverInfoEditText);
            EditText content = (EditText) findViewById(R.id.contentEditText);
            String receiverInfoText = receiverInfo.getText().toString();
            String contentText = content.getText().toString();
            List<String> userIds = data.getStringArrayListExtra(UsersListActivity.SELECTED_USER_IDS_ARRAY_KEY);
            final KProgressHUD hud = KProgressHUD.create(WriteLittlePaperActivity.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false)
                    .show();
            LittlePaperManager.getInstance().newPaperMessage(contentText, receiverInfoText, userIds.get(0), new LittlePaperManager.OnNewPaperMessagePost() {
                @Override
                public void onNewPaperMessagePost(boolean suc) {
                    hud.dismiss();
                    if(suc){
                        ProgressHUDHelper.showHud(WriteLittlePaperActivity.this, R.string.send_little_paper_suc, R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                            @Override
                            public void onHudDismiss() {
                                finish();
                            }
                        });
                    }else {
                        ProgressHUDHelper.showHud(WriteLittlePaperActivity.this, R.string.send_little_paper_failure, R.mipmap.cross_mark,true);
                    }
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
