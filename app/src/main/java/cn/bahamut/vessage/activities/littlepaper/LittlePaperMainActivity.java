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

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;

public class LittlePaperMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_little_paper_main);

        ImageView backgroundImageView = (ImageView)findViewById(R.id.backgroundImageView);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_bcg));
        backgroundImageView.setImageBitmap(bitmap);

        findViewById(R.id.new_little_paper).setOnClickListener(onClickNewLittlePaper);
        findViewById(R.id.little_paper_box).setOnClickListener(onClickLittlePaperBox);

        findViewById(R.id.badgeTextView).bringToFront();

        LittlePaperManager.initManager();
        LittlePaperManager.getInstance().getPaperMessages(new LittlePaperManager.OnPaperMessageUpdated() {
            @Override
            public void onPaperMessageUpdated() {
                LittlePaperManager.getInstance().refreshPaperMessage(new LittlePaperManager.OnPaperMessageUpdated() {
                    @Override
                    public void onPaperMessageUpdated() {
                        LittlePaperManager.getInstance().reloadCachedData();
                        refreshBadge();
                    }
                });
            }
        });
        ServicesProvider.getService(ExtraActivitiesService.class).clearActivityBadge(LittlePaperManager.LITTLE_PAPER_ACTIVITY_ID);
    }

    private void setBadge(int badge){
        if(badge == 0){
            setBadge(null);
        }else {
            setBadge(String.valueOf(badge));
        }
    }

    private void setBadge(String badge){
        if(StringHelper.isStringNullOrEmpty(badge)){
            findViewById(R.id.badgeTextView).setVisibility(View.INVISIBLE);
        }else {
            TextView badgeView = (TextView) findViewById(R.id.badgeTextView);
            badgeView.setVisibility(View.VISIBLE);
            badgeView.setText(badge);
        }
    }

    private void refreshBadge() {
        setBadge(LittlePaperManager.getInstance().getTotalBadgeCount());
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

    View.OnClickListener onClickLittlePaperBox = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LittlePaperMainActivity.this,LittlePaperBoxActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickNewLittlePaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LittlePaperMainActivity.this,WriteLittlePaperActivity.class);
            startActivity(intent);
        }
    };
}
