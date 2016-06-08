package cn.bahamut.vessage.conversation;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.camera.VessageCamera;
import cn.bahamut.vessage.camera.VessageCameraBase;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class RecordVessageActivity extends Activity {

    private static final int MAX_RECORD_TIME_SECOND = 16;

    private Button leftButton;
    private Button middleButton;
    private Button rightButton;
    private TextView recordingTimeLeft;
    private SurfaceView previewView;
    private VessageUser chatter;
    private ImageView smileFaceImageView;
    private TextView noBcgTipsTextView;
    private ImageView chatterImageView;

    private VessageCameraBase camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(cn.bahamut.vessage.R.layout.activity_record_vessage);
        noBcgTipsTextView  = (TextView)findViewById(R.id.tv_no_chat_bcg);
        smileFaceImageView = (ImageView)findViewById(R.id.smileFaceImageView);
        smileFaceImageView.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.raw.smile_face)));
        chatterImageView = (ImageView)findViewById(R.id.chatterImageView);
        previewView = (SurfaceView)findViewById(R.id.previewView);
        recordingTimeLeft = (TextView)findViewById(R.id.recordingTimeLeft);
        leftButton = (Button)findViewById(R.id.leftButton);
        middleButton = (Button) findViewById(R.id.middleButton);
        rightButton = (Button)findViewById(R.id.rightButton);

        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        rightButton.setOnClickListener(onRightButtonClickListener);

        hideView(leftButton);
        hideView(rightButton);
        String chatterId = getIntent().getStringExtra("chatterId");
        String chatterMobile = getIntent().getStringExtra("chatterMobile");
        prepareChatter(chatterId,chatterMobile);
        camera = new VessageCamera(RecordVessageActivity.this);
        camera.initCameraForRecordVideo(previewView);
        camera.setHandler(cameraHandler);
    }

    @Override
    protected void onDestroy() {
        camera.release();
        super.onDestroy();
    }

    private VessageCamera.OnRecordingTiming cameraHandler = new VessageCamera.OnRecordingTiming() {
        @Override
        public void onRecordingTiming(final int recordedTime) {
            recordingTimeLeft.post(new Runnable() {
                @Override
                public void run() {
                    if(recordedTime < 1){
                        middleButton.setVisibility(View.INVISIBLE);
                    }else {
                        middleButton.setVisibility(View.VISIBLE);
                    }
                    recordingTimeLeft.setText(String.valueOf(MAX_RECORD_TIME_SECOND - recordedTime));
                    if(MAX_RECORD_TIME_SECOND == recordedTime){
                        saveRecordedMedia();
                    }
                }
            });

        }
    };

    private String createVideoTmpFile(){
        File tmpVideoFile = getVideoTmpFile();
        if(tmpVideoFile.exists()){
            tmpVideoFile.delete();
        }
        return tmpVideoFile.getAbsolutePath();
    }

    private void prepareChatter(String chatterId, String chatterMobile){
        UserService userService = ServicesProvider.getService(UserService.class);
        userService.addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        VessageUser chatUser = null;
        if(!StringHelper.isStringNullOrEmpty(chatterId)){
            chatUser = userService.getUserById(chatterId);
            if(chatUser == null) {
                chatUser = new VessageUser();
                chatUser.userId = chatterId;
                chatUser.mobile = chatterMobile;
                userService.fetchUserByUserId(chatterId, UserService.DefaultUserUpdatedCallback);
            }
        }else {
            chatUser = new VessageUser();
            chatUser.mobile = chatterMobile;
            userService.fetchUserByMobile(chatterMobile);
        }
        setChatter(chatUser);
    }

    private Observer onVessageUserUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser)state.getInfo();
            if(VessageUser.isTheSameUser(user,chatter)){
                setChatter(user);
            }
        }
    };

    private void setChatter(VessageUser user) {
        this.chatter = user;
        if(StringHelper.isStringNullOrEmpty(chatter.mainChatImage)){
            showView(smileFaceImageView);
            showView(noBcgTipsTextView);
            hideView(chatterImageView);
        }else {
            hideView(smileFaceImageView);
            hideView(noBcgTipsTextView);
            showView(chatterImageView);
            ImageHelper.setImageByFileId(chatterImageView,chatter.mainChatImage);
        }
    }

    private View.OnClickListener onleftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            camera.cancelRecord();
            resetCamera();
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(camera.isRecording()){
                saveRecordedMedia();
            }else{
                MobclickAgent.onEvent(RecordVessageActivity.this,"RecordVessage");
                startRecord();
            }
        }
    };

    private void startRecord() {
        createVideoTmpFile();
        if(camera.startRecord()){
            recordingTimeLeft.setText(String.valueOf(MAX_RECORD_TIME_SECOND));
            hidePreview();
            showView(leftButton);
            showView(rightButton);
            showView(recordingTimeLeft);
            hideView(middleButton);
            middleButton.setBackgroundResource(R.mipmap.check_round);
        }else {
            Toast.makeText(RecordVessageActivity.this,R.string.start_record_error,Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecordedMedia() {
        camera.stopAndSaveRecordedVideo(new VessageCamera.CameraOnSavedVideo() {
            @Override
            public void onVideoSaved(File file) {
                File desc = getVideoTmpFile();
                FileHelper.customBufferBufferedStreamCopy(file,desc);
                askForSendVideo();
            }
        });

        resetCamera();
    }

    private void resetCamera() {
        showPreview();
        showView(middleButton);
        hideView(leftButton);
        hideView(rightButton);
        hideView(recordingTimeLeft);
        middleButton.setBackgroundResource(R.mipmap.movie);
    }

    private File getVideoTmpFile(){
        File tmpVideoFile = new File(getCacheDir(),"tmpVideo.mp4");
        return tmpVideoFile;
    }

    private void askForSendVideo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RecordVessageActivity.this)
        .setTitle(R.string.ask_send_vessage)
        .setMessage(chatter.nickName)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendVessageVideo();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MobclickAgent.onEvent(RecordVessageActivity.this,"CancelSendVessage");
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void sendVessageVideo(){
        MobclickAgent.onEvent(RecordVessageActivity.this,"ConfirmSendVessage");
        File videoFile = getVideoTmpFile();
        if(!StringHelper.isStringNullOrEmpty(chatter.userId)){
            SendVessageQueue.getInstance().sendVessageToUser(chatter.userId,videoFile);
        }else if(!StringHelper.isStringNullOrEmpty(chatter.mobile)){
            SendVessageQueue.getInstance().sendVessageToMobile(chatter.mobile,videoFile);
        }
    }

    private View.OnClickListener onRightButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private void hideView(View v){
        v.setVisibility(View.INVISIBLE);
    }

    private float originPreviewX = 0;
    private float originPreviewY = 0;
    private void hidePreview(){
        if(originPreviewX == 0 && originPreviewY == 0){
            originPreviewX = previewView.getX();
            originPreviewY = previewView.getY();
        }
        previewView.setX(0 - previewView.getWidth());
        previewView.setY(0 - previewView.getHeight());
    }

    private void showView(View v){
        v.setVisibility(View.VISIBLE);
    }
    private void showPreview(){
        previewView.setX(originPreviewX);
        previewView.setY(originPreviewY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.resumeRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.pauseRecord();
    }

}
