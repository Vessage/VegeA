package cn.bahamut.vessage.activities.tim;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.common.ImageConverter;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;

public class TextImageEditorActivity extends AppCompatActivity {

    public static final String EDITED_TEXT_CONTENT_KEY = "editedTextContent";
    private EditText contentEditText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tim_activity_text_image_editor);
        contentEditText = (EditText) findViewById(R.id.content_et);
        imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setOnClickListener(onClickImageView);
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        contentEditText.setText(getIntent().getStringExtra("textContent"));
        String contentEditTextHint = getIntent().getStringExtra("textContentHint");
        if (StringHelper.isStringNullOrWhiteSpace(contentEditTextHint) == false) {
            contentEditText.setHint(contentEditTextHint);
        }

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
            ImageHelper.setImageByFileId(imageView, imageFileId);
        } else if (imageUri != null) {
            imageView.setImageURI(imageUri);
        }
    }

    private View.OnClickListener onClickImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int imageResId = getIntent().getIntExtra("imageResId", 0);
            byte[] imageData = getIntent().getByteArrayExtra("imageData");
            String imageFileId = getIntent().getStringExtra("imageFileId");
            Uri imageUri = getIntent().getData();
            if (imageResId != 0) {
                new FullScreenImageViewer.Builder(TextImageEditorActivity.this).setImageResId(imageResId).show();
            } else if (imageData != null && imageData.length > 0) {
                new FullScreenImageViewer.Builder(TextImageEditorActivity.this).setImageData(imageData).show();
            } else if (StringHelper.isStringNullOrWhiteSpace(imageFileId) == false) {
                new FullScreenImageViewer.Builder(TextImageEditorActivity.this).setImageFileId(imageFileId).show();
            } else if (imageUri != null) {
                new FullScreenImageViewer.Builder(TextImageEditorActivity.this).setImageUri(imageUri).show();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String postItemTitle = getIntent().getStringExtra("postTitle");
        if (StringHelper.isStringNullOrWhiteSpace(postItemTitle)) {
            menu.add(1, 1, 1, R.string.post_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add(1, 1, 1, postItemTitle).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent intent = new Intent(getIntent());
            intent.putExtra(EDITED_TEXT_CONTENT_KEY, contentEditText.getText().toString());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    static public class Builder {
        private Activity context;
        private Intent intent;

        public Builder(Activity context) {
            this.context = context;
            intent = new Intent(context, TextImageEditorActivity.class);
        }

        public Builder setContentText(String contentText) {
            intent.putExtra("textContent", contentText);
            return this;
        }

        public Builder setContentTextHint(String contentTextHint) {
            intent.putExtra("textContentHint", contentTextHint);
            return this;
        }

        public Builder setImageResId(int imageResId) {
            intent.putExtra("imageResId", imageResId);
            return this;
        }

        public Builder setImageData(byte[] imageData) {
            intent.putExtra("imageData", imageData);
            return this;
        }

        public Builder setImageFileId(String imageFileId) {
            intent.putExtra("imageFileId", imageFileId);
            return this;
        }

        public Builder setImageUri(Uri imageUri) {
            intent.setData(imageUri);
            return this;
        }

        public Builder setPostItemTitle(String postTitle) {
            intent.putExtra("postTitle", postTitle);
            return this;
        }

        public Builder setActivityTitle(String title) {
            intent.putExtra("title", title);
            return this;
        }

        public void startActivity() {
            context.startActivity(intent);
        }

        public void startActivity(int requestId) {
            context.startActivityForResult(intent, requestId);
        }
    }
}
