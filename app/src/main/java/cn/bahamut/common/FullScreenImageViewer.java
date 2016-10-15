package cn.bahamut.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import cn.bahamut.vessage.R;

public class FullScreenImageViewer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_viewer);
        ActivityHelper.fullScreen(this,true);

        byte[] data = getIntent().getByteArrayExtra("data");
        if (data != null && data.length > 0){
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setOnClickListener(onClickImageView);
            Bitmap img = BitmapFactory.decodeByteArray(data,0,data.length);
            imageView.setImageBitmap(img);
        }else {
            finish();
        }
    }

    private View.OnClickListener onClickImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
