package cn.bahamut.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;

public class FullScreenImageViewer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_viewer);
        ActivityHelper.fullScreen(this, true);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setOnClickListener(onClickImageView);

        int imageResId = getIntent().getIntExtra("imageResId", 0);
        byte[] imageData = getIntent().getByteArrayExtra("imageData");
        String imageFileId = getIntent().getStringExtra("imageFileId");
        Uri imageUri = getIntent().getData();

        if (imageResId != 0) {
            imageView.setImageResource(imageResId);
        } else if (imageData != null && imageData.length > 0) {
            Drawable drawable = ImageConverter.getInstance().bytes2Drawable(imageData);
            imageView.setImageDrawable(drawable);
        } else if (StringHelper.isStringNullOrWhiteSpace(imageFileId) == false) {
            ImageHelper.setImageByFileId(this, imageView, imageFileId);
        } else if (imageUri != null) {
            imageView.setImageURI(imageUri);
        } else {
            finish();
        }
    }

    private View.OnClickListener onClickImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    static public class Builder {
        private Intent intent;
        private Context context;

        public Builder(Context context) {
            this.context = context;
            this.intent = new Intent(context, FullScreenImageViewer.class);
        }

        public FullScreenImageViewer.Builder setImageResId(int imageResId) {
            intent.putExtra("imageResId", imageResId);
            return this;
        }

        public FullScreenImageViewer.Builder setImageData(byte[] imageData) {
            intent.putExtra("imageData", imageData);
            return this;
        }

        public FullScreenImageViewer.Builder setImageFileId(String imageFileId) {
            intent.putExtra("imageFileId", imageFileId);
            return this;
        }

        public FullScreenImageViewer.Builder setImageUri(Uri imageUri) {
            intent.setData(imageUri);
            return this;
        }

        public void show() {
            context.startActivity(intent);
        }
    }
}
