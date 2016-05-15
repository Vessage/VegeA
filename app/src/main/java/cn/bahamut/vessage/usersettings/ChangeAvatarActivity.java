package cn.bahamut.vessage.usersettings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.FileHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

public class ChangeAvatarActivity extends AppCompatActivity {
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESIZE_REQUEST_CODE = 2;

    private static final String IMAGE_FILE_NAME = "tmpAvatar.jpg";

    private RoundedImageView avatarImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_avatar);
        setTitle(R.string.change_avatar);
        findViewById(R.id.select_picture_button).setOnClickListener(onClickSelectPicture);
        findViewById(R.id.take_picture_button).setOnClickListener(onClickTakePicture);
        avatarImage = (RoundedImageView)findViewById(R.id.avatarImageView);
        String avatar = ServicesProvider.getService(UserService.class).getMyProfile().avatar;
        ImageHelper.setImageByFileId(avatarImage,avatar,R.mipmap.default_avatar);
    }

    private View.OnClickListener onClickTakePicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            choseHeadImageFromCameraCapture();
        }
    };

    private View.OnClickListener onClickSelectPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            choseHeadImageFromGallery();
        }
    };

    // 从本地相册选取图片作为头像
    private void choseHeadImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
    }

	// 启动手机相机拍摄照片作为头像
	private void choseHeadImageFromCameraCapture() {
        if (AndroidHelper.isSdcardExisting()) {
            Intent cameraIntent = new Intent(
                    "android.media.action.IMAGE_CAPTURE");
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(ChangeAvatarActivity.this, R.string.insert_sd_card, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        } else {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    resizeImage(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (AndroidHelper.isSdcardExisting()) {
                        resizeImage(getImageUri());
                    } else {
                        Toast.makeText(ChangeAvatarActivity.this, R.string.no_sd_card,
                                Toast.LENGTH_LONG).show();
                    }
                    break;

                case RESIZE_REQUEST_CODE:
                    if (data != null) {
                        showResizeImage(data);
                    }
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESIZE_REQUEST_CODE);
    }

    private void showResizeImage(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap photo = extras.getParcelable("data");
            photo.compress(Bitmap.CompressFormat.JPEG,80,baos);
            byte[] jpeg = baos.toByteArray();
            if(FileHelper.saveFile(jpeg,getAvatarSavedFile())){
                uploadImage();
            }else{
                Toast.makeText(ChangeAvatarActivity.this,R.string.save_avatar_error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage() {
        File imageFile = getAvatarSavedFile();
        if(imageFile.exists()){
            final KProgressHUD hud = KProgressHUD.create(ChangeAvatarActivity.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false)
                    .show();
            ServicesProvider.getService(FileService.class).uploadFile(imageFile.getAbsolutePath(),".jpeg",null,new FileService.OnFileListenerAdapter(){
                @Override
                public void onFileFailure(FileAccessInfo info, Object tag) {
                    hud.dismiss();
                    ProgressHUDHelper.showHud(ChangeAvatarActivity.this, R.string.upload_avatar_fail, R.mipmap.cross_mark, true);
                }

                @Override
                public void onFileSuccess(final FileAccessInfo info, Object tag) {
                    getAvatarSavedFile().renameTo(new File(getCacheDir(),info.getFileId()));
                    ServicesProvider.getService(UserService.class).changeMyAvatar(info.getFileId(), new UserService.ChangeAvatarCallback() {
                        @Override
                        public void onChangeAvatar(boolean isChanged) {
                            hud.dismiss();
                            if(isChanged){
                                ProgressHUDHelper.showHud(ChangeAvatarActivity.this, R.string.upload_avatar_suc, R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                                    @Override
                                    public void onHudDismiss() {
                                        String avatar = ServicesProvider.getService(UserService.class).getMyProfile().avatar;
                                        ImageHelper.setImageByFileId(avatarImage,avatar,R.mipmap.default_avatar);
                                    }
                                });
                            }else {
                                ProgressHUDHelper.showHud(ChangeAvatarActivity.this, R.string.upload_avatar_fail, R.mipmap.cross_mark, true);
                            }
                        }
                    });

                }
            });
        }else {
            Toast.makeText(ChangeAvatarActivity.this,R.string.no_file,Toast.LENGTH_SHORT).show();
        }
    }

    private File getAvatarSavedFile(){
        return new File(getCacheDir(),"uploadAvatar.jpg");
    }

    private Uri getImageUri() {

        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                IMAGE_FILE_NAME));
    }

}
