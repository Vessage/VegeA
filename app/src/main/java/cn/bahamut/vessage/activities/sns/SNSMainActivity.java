package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.FileHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.activities.tim.TextImageEditorActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.SelectImageSourceAlertDialogBuilder;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

public class SNSMainActivity extends AppCompatActivity {

    private static final int IMAGE_SOURCE_ALBUM_REQUEST_ID = 1;
    private static final int IMAGE_SOURCE_CAMERA_REQUEST_ID = 2;
    private static final int SNS_POST_IMAGE_WIDTH = 600;
    private static final int SNS_POST_IMAGE_QUALITY = 80;
    private static final int TEXT_IMAGE_EDITOR_REQUEST_ID = 3;
    private RecyclerView postListView;
    private SNSPostAdapter adapter;
    private TextView homeButton;
    private TextView myPostButton;
    private ProgressBar sendingProgress;
    private ImageView sendingPreviewImage;

    private boolean isUserPageMode() {
        return getIntent().getBooleanExtra("userPageMode", false);
    }

    private String specificUserId() {
        return getIntent().getStringExtra("specificUserId");
    }

    private String specificUserNick() {
        return getIntent().getStringExtra("specificUserNick");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_snsmain);
        SNSPostManager.getInstance().initManager();


        sendingPreviewImage = (ImageView)findViewById(R.id.sending_preview_image);
        postListView = (RecyclerView) findViewById(R.id.post_list_view);
        sendingProgress = (ProgressBar) findViewById(R.id.progress_sending);
        adapter = new SNSPostAdapter(this);

        if (isUserPageMode()) {
            getSupportActionBar().setTitle(String.format(LocalizedStringHelper.getLocalizedString(R.string.x_sns_posts), specificUserNick()));
            findViewById(R.id.bottom_view).getLayoutParams().height = 0;
            findViewById(R.id.bottom_view).setVisibility(View.INVISIBLE);
            adapter.setSpecificUserId(specificUserId());
            adapter.setPostType(SNSPost.TYPE_SINGLE_USER_POST);
        } else {
            getSupportActionBar().setTitle(R.string.sns);
        }

        postListView.setAdapter(adapter);

        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        postListView.setLayoutManager(lm);
        findViewById(R.id.new_post_btn).setOnClickListener(onClickBottomView);
        homeButton = (TextView) findViewById(R.id.home_btn);
        findViewById(R.id.home_btn_container).setOnClickListener(onClickBottomView);
        myPostButton = (TextView) findViewById(R.id.my_post_btn);
        myPostButton.setOnClickListener(onClickBottomView);
        hideSendingProgress();
        postListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0){
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItemCount = layoutManager.getItemCount();

                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!adapter.isLoadingMore() && totalItemCount < (lastVisibleItem + 3)) {
                    adapter.loadMorePosts();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.isInited() == false){
            refreshPost();
        }
    }
    private void refreshPost() {
        adapter.refreshPosts(new SNSPostAdapter.RefreshPostCallback() {
            @Override
            public void onRefreshCompleted(int received) {
                if (received < 0){
                    Toast.makeText(SNSMainActivity.this,R.string.get_sns_data_error,Toast.LENGTH_SHORT).show();
                }else if (received == 0){
                    Toast.makeText(SNSMainActivity.this,R.string.no_sns_posts,Toast.LENGTH_SHORT).show();
                }
                if (adapter.getMainBoardData() != null && adapter.getMainBoardData().newer) {
                    adapter.getMainBoardData().newer = false;
                    showNewerAlert();
                }
            }
        });
    }

    private void showNewerAlert() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.sns)
                .setMessage(R.string.sns_newer_tips)
                .setCancelable(true)
                .setPositiveButton(R.string.sns_post_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postNewSNSPost();
                    }
                })
                .setNegativeButton(R.string.sns_post_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SNSPostManager.getInstance().releaseManager();
    }

    private View.OnClickListener onClickBottomView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.new_post_btn:
                    postNewSNSPost();
                    break;
                case R.id.home_btn_container:case R.id.home_btn:
                    switchHomePage();
                    break;
                case R.id.my_post_btn:
                    switchMyPostsPage();
                    break;
            }
        }
    };

    private void playSendingPreviewImageAnimation(String filePath){
        Drawable drawable = BitmapDrawable.createFromPath(filePath);
        sendingPreviewImage.setImageDrawable(drawable);
        Animation mScaleAnimation = new ScaleAnimation(2.0f, 0.0f, 2.0f, 0.0f,// 整个屏幕就0.0到1.0的大小//缩放
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
        mScaleAnimation.setDuration(1200);
        mScaleAnimation.setFillAfter(true);

        sendingPreviewImage.clearAnimation();
        sendingPreviewImage.setVisibility(View.VISIBLE);


        mScaleAnimation.setAnimationListener(new AnimationHelper.AnimationListenerAdapter(){
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                sendingPreviewImage.setVisibility(View.INVISIBLE);
                sendingPreviewImage.setImageDrawable(null);
            }
        });
        sendingPreviewImage.startAnimation(mScaleAnimation);
    }

    public void showSendingProgress(){
        sendingProgress.setVisibility(View.VISIBLE);
    }

    public void hideSendingProgress(){
        sendingProgress.setVisibility(View.INVISIBLE);
    }


    private void refreshBottomButton() {
        ColorStateList tmp = myPostButton.getTextColors();
        myPostButton.setTextColor(homeButton.getTextColors());
        homeButton.setTextColor(tmp);
    }

    private void switchMyPostsPage() {
        switchPostTypePage(SNSPost.TYPE_MY_POST);
    }

    private void switchPostTypePage(int type) {
        if (type != adapter.getPostType()){
            postListView.scrollToPosition(0);
            adapter.setPostType(type);
            refreshBottomButton();
        }else {
            if (adapter.getItemCount() <= 1 ){
                refreshPost();
            }
        }
        if (type == SNSPost.TYPE_SINGLE_USER_POST) {
            getSupportActionBar().setTitle(String.format(LocalizedStringHelper.getLocalizedString(R.string.x_sns_posts), specificUserNick()));
        } else if (type == SNSPost.TYPE_MY_POST) {
            getSupportActionBar().setTitle(R.string.my_post);
        }else {
            getSupportActionBar().setTitle(R.string.sns);
        }
    }

    private void switchHomePage() {
        switchPostTypePage(SNSPost.TYPE_NORMAL_POST);
    }

    private void postNewSNSPost() {
        AnimationHelper.startAnimation(this,findViewById(R.id.new_post_btn),R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
            @Override
            public void onAnimationEnd(Animation animation) {
                showImageSourceAlert();
            }
        });

    }

    SelectImageSourceAlertDialogBuilder selectImageSourceAlertDialogBuilder;
    private void showImageSourceAlert() {
        selectImageSourceAlertDialogBuilder = new SelectImageSourceAlertDialogBuilder(SNSMainActivity.this);
        selectImageSourceAlertDialogBuilder.showSourceImageAlert(IMAGE_SOURCE_ALBUM_REQUEST_ID,IMAGE_SOURCE_CAMERA_REQUEST_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean handled = handleImageResult(requestCode, resultCode, data) ||
                handleTextImageEditorResult(requestCode, resultCode, data);

        Log.i("SNS", "Handle Activity Result:" + String.valueOf(handled));
    }

    private boolean handleTextImageEditorResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TEXT_IMAGE_EDITOR_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {

                String textContent = data.getStringExtra(TextImageEditorActivity.EDITED_TEXT_CONTENT_KEY);

                String body = null;
                if (!StringHelper.isStringNullOrWhiteSpace(textContent)) {
                    Dictionary<String, String> object = new Hashtable<>();
                    object.put("txt", textContent);
                    body = new Gson().toJson(object);
                }
                postSNSImage(data.getData().getPath(), body);
            }
            return true;
        }
        return false;
    }

    private boolean handleImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_SOURCE_ALBUM_REQUEST_ID || requestCode == IMAGE_SOURCE_CAMERA_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = null;
                Uri uri = requestCode == IMAGE_SOURCE_ALBUM_REQUEST_ID ? data.getData() : Uri.fromFile(selectImageSourceAlertDialogBuilder.getFileForSaveFromCamera());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap newBitmap = ImageHelper.scaleImageToMaxWidth(bitmap, SNS_POST_IMAGE_WIDTH);
                    File tmpImageFile = FileHelper.generateTempFile(this, "jpg");
                    ImageHelper.storeBitmap(this, newBitmap, tmpImageFile, SNS_POST_IMAGE_QUALITY);
                    new TextImageEditorActivity.Builder(this)
                            .setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.share_to_sns))
                            .setContentTextHint(LocalizedStringHelper.getLocalizedString(R.string.share_text_content_hint))
                            .setPostItemTitle(LocalizedStringHelper.getLocalizedString(R.string.share))
                            .setImageUri(Uri.fromFile(tmpImageFile))
                            .startActivity(TEXT_IMAGE_EDITOR_REQUEST_ID);

                } catch (IOException e) {
                    Toast.makeText(this, R.string.read_image_error, Toast.LENGTH_SHORT).show();
                }
            }
            selectImageSourceAlertDialogBuilder = null;
            return true;
        }
        return false;
    }

    public void postSNSImage(String filePath, final String body) {
        playSendingPreviewImageAnimation(filePath);
        showSendingProgress();
        ServicesProvider.getService(FileService.class).uploadFile(filePath, "png", filePath, new FileService.OnFileListener() {
            @Override
            public void onGetFileInfo(FileAccessInfo info, Object tag) {
            }

            @Override
            public void onGetFileInfoError(String fileId, Object tag) {
                ProgressHUDHelper.showHud(SNSMainActivity.this,R.string.network_error,R.mipmap.cross_mark,true);
                hideSendingProgress();
            }

            @Override
            public void onFileSuccess(FileAccessInfo info, Object tag) {
                //TODO:
                SNSPostManager.getInstance().newPost(info.getFileId(), body, new SNSPostManager.PostNewSNSPostCallback() {
                    @Override
                    public void onPostNewSNSPost(final SNSPost newPost) {
                        hideSendingProgress();
                        if (newPost != null) {
                            ProgressHUDHelper.showHud(SNSMainActivity.this, R.string.post_sns_post_suc, R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                                @Override
                                public void onHudDismiss() {
                                    adapter.postNew(newPost);
                                    postListView.scrollToPosition(0);
                                }
                            });
                        } else {
                            ProgressHUDHelper.showHud(SNSMainActivity.this, R.string.network_error, R.mipmap.cross_mark, true);
                        }
                    }
                });
            }

            @Override
            public void onFileFailure(FileAccessInfo info, Object tag) {
                ProgressHUDHelper.showHud(SNSMainActivity.this,R.string.network_error,R.mipmap.cross_mark,true);
                hideSendingProgress();
            }

            @Override
            public void onFileProgress(FileAccessInfo info, double progress, Object tag) {

            }
        });
    }
}
