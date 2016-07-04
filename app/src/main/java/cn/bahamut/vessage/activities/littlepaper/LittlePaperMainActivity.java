package cn.bahamut.vessage.activities.littlepaper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;

public class LittlePaperMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_little_paper_main);

        ImageView backgroundImageView = (ImageView)findViewById(R.id.bcg_img_view);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_bcg));
        backgroundImageView.setImageBitmap(bitmap);

        findViewById(R.id.new_little_paper).setOnClickListener(onClickNewLittlePaper);
        findViewById(R.id.little_paper_box).setOnClickListener(onClickLittlePaperBox);
        findViewById(R.id.msg_box).setOnClickListener(onClickMessageBox);

        findViewById(R.id.paper_box_badge).bringToFront();
        findViewById(R.id.msg_box_badge).bringToFront();

        findViewById(R.id.invite_friends_btn).setOnClickListener(onClickInviteFirends);

        LittlePaperManager.initManager();
        LittlePaperManager.getInstance().getPaperMessages(new LittlePaperManager.OnPaperMessageUpdated() {
            @Override
            public void onPaperMessageUpdated(int updated) {
                LittlePaperManager.getInstance().refreshPaperMessage(new LittlePaperManager.OnPaperMessageUpdated() {
                    @Override
                    public void onPaperMessageUpdated(int updated) {
                        LittlePaperManager.getInstance().reloadCachedData();
                        refreshBadge();
                    }
                });
            }
        });
        LittlePaperManager.getInstance().getReadResponses(new LittlePaperManager.LittlePaperManagerOperateCallback() {
            @Override
            public void onCallback(boolean isOk, String errorMessage) {
                refreshBadge();
            }
        });
        ServicesProvider.getService(ExtraActivitiesService.class).clearActivityBadge(LittlePaperManager.LITTLE_PAPER_ACTIVITY_ID);

        MobclickAgent.onEvent(this,"LittlePaper_Launch");
    }

    private void setBadge(int viewId,int badge){
        if(badge == 0){
            setBadge(viewId,null);
        }else {
            setBadge(viewId,String.valueOf(badge));
        }
    }

    private void setBadge(int viewId,final String badge){
        final TextView badgeView = (TextView) findViewById(viewId);
        badgeView.post(new Runnable() {
            @Override
            public void run() {
                if(StringHelper.isNullOrEmpty(badge)){
                    badgeView.setVisibility(View.INVISIBLE);
                }else {

                    badgeView.setVisibility(View.VISIBLE);
                    badgeView.setText(badge);
                }
            }
        });

    }

    private void refreshBadge() {
        setBadge(R.id.msg_box_badge,LittlePaperManager.getInstance().getResponsesBadge());
        setBadge(R.id.paper_box_badge,LittlePaperManager.getInstance().getTotalBadgeCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LittlePaperManager.releaseManager();
    }

    private View.OnClickListener onClickInviteFirends = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppMain.getInstance().showTellVegeToFriendsAlert(LocalizedStringHelper.getLocalizedString(R.string.little_paper_tell_friend),R.string.little_paper_tell_friend_alert_title);
        }
    };

    private View.OnClickListener onClickMessageBox = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LittlePaperMainActivity.this,LittlePaperResponsesActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickLittlePaperBox = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LittlePaperMainActivity.this,LittlePaperBoxActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickNewLittlePaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LittlePaperMainActivity.this,LittlePaperWriteActivity.class);
            startActivity(intent);
        }
    };
}
