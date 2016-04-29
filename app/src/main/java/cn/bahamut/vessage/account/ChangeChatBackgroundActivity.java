package cn.bahamut.vessage.account;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
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
        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        rightButton.setOnTouchListener(onRightButtonTouchListener);

        setIsPreviewingImage(false);

        camera = new VessageCamera(this);
        camera.initCameraForRecordTakePicture(previewView);
    }

    private View.OnClickListener onleftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setIsPreviewingImage(false);
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPreviewingImage){
                uploadImage();
            }else {
                takePicture();
                setIsPreviewingImage(true);
            }
        }
    };


    private View.OnTouchListener onRightButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                demoImageView.setVisibility(View.VISIBLE);
                previewView.setVisibility(View.INVISIBLE);
                middleButton.setVisibility(View.INVISIBLE);
            }else {
                demoImageView.setVisibility(View.INVISIBLE);
                previewView.setVisibility(View.VISIBLE);
                middleButton.setVisibility(View.VISIBLE);
            }
            return false;
        }
    };

    private File getTmpImageSaveFile(){
        return new File(ChangeChatBackgroundActivity.this.getCacheDir(),"tmpBcg.jpeg");
    }

    private void takePicture() {
        camera.takePicture(new VessageCameraBase.OnTokePicture(){
            @Override
            public void onTokeJEPGPicture(byte[] jpeg) {
                File file = getTmpImageSaveFile();
                if (file.exists()) {
                    file.delete();
                }
                if (FileHelper.saveFile(jpeg, file)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    chatterImageView.setImageBitmap(bitmap);
                    setIsPreviewingImage(true);
                }
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
        if(isPreviewingImage){
            middleButton.setBackgroundResource(R.mipmap.check_round);
        }else {
            middleButton.setBackgroundResource(R.mipmap.camera);
        }
    }

    static public void startChangeChatBackgroundActivity(Activity context,int requestCode){
        Intent intent = new Intent(context, ChangeChatBackgroundActivity.class);
        intent.putExtra(KEY_REQUEST_CODE,requestCode);
        context.startActivityForResult(intent,requestCode);
    }
}
