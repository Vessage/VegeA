package cn.bahamut.vessage.usersettings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.camera.VessageCamera;
import cn.bahamut.vessage.camera.VessageCameraBase;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.user.UserService;

public class ChangeChatBackgroundActivity extends Activity {
    private static final int IMAGE_REQUEST_CODE = 1;
    private static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";
    public static final int RESULT_CODE_SET_BACGROUND_SUCCESS = 1;

    private SurfaceView previewView;
    private ImageView demoImageView;
    private ImageView chatterImageView;

    private Button leftButton;
    private Button middleButton;
    private Button rightButton;
    private View rightButtonTips;

    private View selectPicContainer;
    private Button selectPicButton;

    private VessageCameraBase camera;

    private boolean isPreviewingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_change_chat_background);
        previewView = (SurfaceView)findViewById(R.id.preview_view);
        chatterImageView = (ImageView)findViewById(R.id.chatter_img_view);
        chatterImageView.setVisibility(View.INVISIBLE);
        demoImageView = (ImageView)findViewById(R.id.demo_img_view);

        demoImageView.setVisibility(View.INVISIBLE);
        demoImageView.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.raw.demo_face)));

        leftButton = (Button)findViewById(R.id.left_btn);
        middleButton = (Button) findViewById(R.id.middle_btn);
        rightButton = (Button)findViewById(R.id.right_btn);
        rightButtonTips = findViewById(R.id.right_btn_tips);
        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        rightButton.setOnClickListener(onRightButtonClickListener);

        selectPicContainer = findViewById(R.id.select_pic_btn_container);
        selectPicButton = (Button)findViewById(R.id.select_pic_btn);
        selectPicButton.setOnClickListener(onSelectPicButtonClickListener);

        setIsPreviewingImage(false);

        camera = new VessageCamera(this);
        camera.initCameraForRecordTakePicture(previewView);
    }

    private View.OnClickListener onleftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setIsPreviewingImage(false);
            camera.startPreview();
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPreviewingImage){
                uploadImage();
            }else {
                takePicture();
            }
        }
    };

    private View.OnClickListener onSelectPicButtonClickListener = new View.OnClickListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        } else if(requestCode == IMAGE_REQUEST_CODE){
            Uri uri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                setChatterImageAndSetPreview(bitmap,false);
            } catch (IOException e) {
                Toast.makeText(this,R.string.read_image_error,Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setChatterImageAndSetPreview(Bitmap bitmap,boolean isTakePicture) {
        File file = getTmpImageSaveFile();
        if (file.exists()) {
            file.delete();
        }

        Bitmap bitmapForDetectFaces = null;
        if(bitmap.getWidth() > 480){
            float scaleRate = 480.0f / bitmap.getWidth();//缩小的比例
            Matrix matrix = new Matrix();
            matrix.setScale(scaleRate,scaleRate);
            Bitmap bitmapScaled = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = bitmapScaled;
            bitmapForDetectFaces = bitmapScaled.copy(Bitmap.Config.RGB_565,true);
        }else {
            bitmapForDetectFaces = bitmap.copy(Bitmap.Config.RGB_565,true);
        }
        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        FaceDetector faceDetector = new FaceDetector(bitmapForDetectFaces.getWidth(),bitmapForDetectFaces.getHeight(),1);
        faceDetector.findFaces(bitmapForDetectFaces,faces);
        if(faces.length > 0 && faces[0] == null){
            Toast.makeText(ChangeChatBackgroundActivity.this,R.string.no_face_detected,Toast.LENGTH_SHORT).show();
            if(isTakePicture){
                camera.startPreview();
            }
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 67, baos);
        byte[] newJpeg = baos.toByteArray();
        chatterImageView.setImageBitmap(bitmap);
        Log.i("Chatter Image Size",String.format("%d * %d",bitmap.getWidth(),bitmap.getHeight()));
        if (FileHelper.saveFile(newJpeg, file)) {
            Log.i("Chat Background",String.format("Picture File Size:%s KB",String.valueOf(file.length() / 1024)));
            setIsPreviewingImage(true);
        }else {
            Toast.makeText(this,R.string.save_image_error,Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener onRightButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPreviewVisible(demoImageView.getVisibility() == View.INVISIBLE);
            if(demoImageView.getVisibility() == View.INVISIBLE){
                demoImageView.setVisibility(View.VISIBLE);
                middleButton.setVisibility(View.INVISIBLE);
                selectPicContainer.setVisibility(View.INVISIBLE);
                rightButton.setBackgroundResource(R.mipmap.close);
            }else {
                demoImageView.setVisibility(View.INVISIBLE);
                middleButton.setVisibility(View.VISIBLE);
                selectPicContainer.setVisibility(View.VISIBLE);
                rightButton.setBackgroundResource(R.mipmap.profile);
            }

        }
    };

    private void setPreviewVisible(boolean hidden) {
        if(hidden){
            previewView.setVisibility(View.INVISIBLE);
        }else {
            previewView.setVisibility(View.VISIBLE);
        }
    }

    private File getTmpImageSaveFile(){
        return new File(ChangeChatBackgroundActivity.this.getCacheDir(),"tmpBcg.jpeg");
    }

    private void takePicture() {
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ChangeChatBackgroundActivity.this);
        camera.takePicture(new VessageCameraBase.OnTokePicture(){
            @Override
            public void onTokeJEPGPicture(Bitmap jpeg) {
                hud.dismiss();
                if(jpeg == null){
                    Toast.makeText(ChangeChatBackgroundActivity.this,R.string.take_picture_from_camera_fail,Toast.LENGTH_SHORT).show();
                    return;
                }
                setChatterImageAndSetPreview(jpeg,true);
            }
        });

    }

    private void uploadImage() {
        File imageFile = getTmpImageSaveFile();
        if(imageFile.exists()){
            final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ChangeChatBackgroundActivity.this);
            ServicesProvider.getService(FileService.class).uploadFile(imageFile.getAbsolutePath(),".jpeg",null,new FileService.OnFileListenerAdapter(){
                @Override
                public void onFileFailure(FileAccessInfo info, Object tag) {
                    hud.dismiss();
                    ProgressHUDHelper.showHud(ChangeChatBackgroundActivity.this, R.string.upload_chat_bcg_fail, R.mipmap.cross_mark, true);
                }

                @Override
                public void onFileSuccess(FileAccessInfo info, Object tag) {
                    ServicesProvider.getService(UserService.class).changeMyChatImage(info.getFileId(), new UserService.ChangeChatBackgroundImageCallback() {
                        @Override
                        public void onChangeChatBackgroundImage(boolean isChanged) {
                            hud.dismiss();
                            if(isChanged){
                                MobclickAgent.onEvent(ChangeChatBackgroundActivity.this,"Vege_FinishSetupChatBcg");
                                ProgressHUDHelper.showHud(ChangeChatBackgroundActivity.this, R.string.upload_chat_bcg_suc, R.mipmap.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                                    @Override
                                    public void onHudDismiss() {
                                        finishReturnSuccess();
                                    }
                                });
                            }else {
                                ProgressHUDHelper.showHud(ChangeChatBackgroundActivity.this, R.string.upload_chat_bcg_fail, R.mipmap.cross_mark, true);
                            }
                        }
                    });

                }
            });
        }else {
            Toast.makeText(ChangeChatBackgroundActivity.this,R.string.no_file,Toast.LENGTH_SHORT).show();
        }
    }

    private void finishReturnSuccess() {
        setResult(RESULT_CODE_SET_BACGROUND_SUCCESS);
        finishActivity(getIntent().getIntExtra(KEY_REQUEST_CODE,0));
        finish();
    }


    private void setIsPreviewingImage(boolean previewingImage) {
        isPreviewingImage = previewingImage;
        chatterImageView.setVisibility(previewingImage ? View.VISIBLE : View.INVISIBLE);
        leftButton.setVisibility(previewingImage ? View.VISIBLE : View.INVISIBLE);
        previewView.setVisibility(previewingImage ? View.INVISIBLE : View.VISIBLE);
        setPreviewVisible(isPreviewingImage);
        if(isPreviewingImage){
            rightButton.setVisibility(View.INVISIBLE);
            rightButtonTips.setVisibility(View.INVISIBLE);
            selectPicContainer.setVisibility(View.INVISIBLE);
            middleButton.setBackgroundResource(R.mipmap.check_round);
        }else {
            selectPicContainer.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);
            rightButtonTips.setVisibility(View.VISIBLE);
            middleButton.setBackgroundResource(R.mipmap.camera);
        }
    }

    static public void startChangeChatBackgroundActivity(Activity context,int requestCode){
        Intent intent = new Intent(context, ChangeChatBackgroundActivity.class);
        intent.putExtra(KEY_REQUEST_CODE,requestCode);
        context.startActivityForResult(intent,requestCode);
    }
}
