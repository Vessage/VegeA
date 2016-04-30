package cn.bahamut.vessage.account;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
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

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.camera.VessageCamera;
import cn.bahamut.vessage.camera.VessageCameraBase;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

public class ChangeChatBackgroundActivity extends Activity {
    private static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";
    public static final int RESULT_CODE_SET_BACGROUND_SUCCESS = 1;

    private SurfaceView previewView;
    private ImageView chatterImageView;
    private ImageView demoImageView;

    private Button leftButton;
    private Button middleButton;
    private Button rightButton;
    private View rightButtonTips;

    private VessageCameraBase camera;

    private boolean isPreviewingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_change_chat_background);
        chatterImageView = (ImageView)findViewById(R.id.chatterImageView);
        previewView = (SurfaceView)findViewById(R.id.previewView);
        demoImageView = (ImageView)findViewById(R.id.demoImageView);
        demoImageView.setVisibility(View.INVISIBLE);
        demoImageView.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.raw.demo_face)));

        leftButton = (Button)findViewById(R.id.leftButton);
        middleButton = (Button) findViewById(R.id.middleButton);
        rightButton = (Button)findViewById(R.id.rightButton);
        rightButtonTips = findViewById(R.id.rightButtonTips);
        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        rightButton.setOnClickListener(onRightButtonClickListener);

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


    private View.OnClickListener onRightButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPreviewVisible(demoImageView.getVisibility() == View.INVISIBLE);
            if(demoImageView.getVisibility() == View.INVISIBLE){
                demoImageView.setVisibility(View.VISIBLE);
                middleButton.setVisibility(View.INVISIBLE);
                rightButton.setBackgroundResource(R.mipmap.close);
            }else {
                demoImageView.setVisibility(View.INVISIBLE);
                middleButton.setVisibility(View.VISIBLE);
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
        final KProgressHUD hud = KProgressHUD.create(ChangeChatBackgroundActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .show();
        camera.takePicture(new VessageCameraBase.OnTokePicture(){
            @Override
            public void onTokeJEPGPicture(byte[] jpeg) {
                File file = getTmpImageSaveFile();
                if (file.exists()) {
                    file.delete();
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg,0,jpeg.length);
                Configuration config = getResources().getConfiguration();
                if (config.orientation==1)
                { // 坚拍
                    Matrix matrix = new Matrix();
                    matrix.reset();
                    matrix.postRotate(270);
                    Bitmap bMapRotate = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(),
                            matrix, true);
                    bitmap = bMapRotate;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] newJpeg = baos.toByteArray();
                chatterImageView.setImageBitmap(bitmap);

                if (FileHelper.saveFile(newJpeg, file)) {
                    setIsPreviewingImage(true);
                }
                hud.dismiss();
            }

            @Override
            public void onTakeRawPicture(byte[] raw) {
                super.onTakeRawPicture(raw);
            }
        });

    }

    private void uploadImage() {
        File imageFile = getTmpImageSaveFile();
        if(imageFile.exists()){
            final KProgressHUD hud = KProgressHUD.create(ChangeChatBackgroundActivity.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false)
                    .show();
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
                                MobclickAgent.onEvent(ChangeChatBackgroundActivity.this,"FinishSetupChatBcg");
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
        leftButton.setVisibility(previewingImage ? View.VISIBLE : View.INVISIBLE);
        chatterImageView.setVisibility(previewingImage ? View.VISIBLE : View.INVISIBLE);
        previewView.setVisibility(previewingImage ? View.INVISIBLE : View.VISIBLE);
        setPreviewVisible(isPreviewingImage);
        if(isPreviewingImage){
            rightButton.setVisibility(View.INVISIBLE);
            rightButtonTips.setVisibility(View.INVISIBLE);
            middleButton.setBackgroundResource(R.mipmap.check_round);
        }else {
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
