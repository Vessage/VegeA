package cn.bahamut.vessage.activities.tim;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;

import java.io.File;
import java.io.IOException;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.activities.sns.SNSMainActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.LocalizedStringHelper;

public class TextImageSaveAndShareActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean shared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tim_activity_text_image_save_and_share);
        setTitle(R.string.tim_save_share);
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        ((ImageView) findViewById(R.id.imageView)).setImageURI(uri);
        findViewById(R.id.share_to_sns).setOnClickListener(this);
        findViewById(R.id.share_to_wechat_timeline).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.vg_mask).setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, R.string.done).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            if (shared) {
                finishTIMActivities();
            } else {
                showFinishTIMActivitiesAlert();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFinishTIMActivitiesAlert() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.tim_image_not_shared)
                .setMessage(R.string.tim_sure_finish_tim)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishTIMActivities();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void finishTIMActivities() {
        if (TextImagePreviewActivity.instance != null) {
            TextImagePreviewActivity.instance.finish();
        }
        if (TextImageStartActivity.instance != null) {
            TextImageStartActivity.instance.finish();
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_to_sns:
                shareImageToSNS();
                break;
            case R.id.share_to_wechat_timeline:
                shareImageToWX();
                break;
            case R.id.save:
                saveImage();
                break;
            default:
                break;
        }
    }

    public File shotContentView(boolean withVGMask) {
        try {
            Bitmap bitmap = createContentBitmap(withVGMask);
            File tmpViewShotFile = File.createTempFile("t2m_img", ".png");
            ImageHelper.storeBitmap2PNG(this, bitmap, tmpViewShotFile, 1);
            return tmpViewShotFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private Bitmap createContentBitmap(boolean withVGMask) {
        findViewById(R.id.vg_mask).setVisibility(withVGMask ? View.VISIBLE : View.INVISIBLE);
        View contentView = findViewById(R.id.content_container);
        Bitmap bitmap = ImageHelper.convertViewToBitmap(contentView);
        findViewById(R.id.vg_mask).setVisibility(View.INVISIBLE);
        return bitmap;
    }

    private void saveImage() {
        MediaStore.Images.Media.insertImage(getContentResolver(), createContentBitmap(true), LocalizedStringHelper.getLocalizedString(R.string.app_name), "Create By VGChat");
        Toast.makeText(this, R.string.save_image_to_album_suc, Toast.LENGTH_SHORT).show();
        shared = true;
    }

    private void shareImageToSNS() {
        Uri uri = Uri.fromFile(shotContentView(false));
        Intent intent = new Intent(this, SNSMainActivity.class);
        intent.setData(uri);
        ExtraActivitiesActivity.startExtraActivity(this, "1003", intent);
        shared = true;
    }

    private void shareImageToWX() {
        if (AppMain.getInstance().getWechatApi() == null) {
            Toast.makeText(this, R.string.wxapi_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = createContentBitmap(true);
        if (bitmap != null) {
            WXImageObject object = new WXImageObject(bitmap);
            WXMediaMessage mediaMessage = new WXMediaMessage();
            mediaMessage.mediaObject = object;
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.message = mediaMessage;
            req.transaction = String.valueOf(System.currentTimeMillis());
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
            if (AppMain.getInstance().getWechatApi().sendReq(req)) {
                Toast.makeText(this, R.string.jump_weixin, Toast.LENGTH_SHORT).show();
                shared = true;
            } else {
                Toast.makeText(this, R.string.wxapi_not_ready, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.tim_prepare_image_error, Toast.LENGTH_SHORT).show();
        }

    }
}
