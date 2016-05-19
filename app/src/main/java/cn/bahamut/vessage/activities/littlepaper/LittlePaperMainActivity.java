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

import cn.bahamut.vessage.R;

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
