package cn.bahamut.vessage.conversation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.seu.magicfilter.*;
import com.seu.magicfilter.camera.CameraEngine;
import com.seu.magicfilter.widget.MagicCameraView;
import com.umeng.message.PushAgent;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.JsonHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.*;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecordVessageActivity extends Activity {

    private static final int MAX_RECORD_TIME_SECOND = 10;

    private Button leftButton;
    private Button middleButton;
    private Button rightButton;
    private TextView recordingTimeLeft;
    private MagicCameraView previewView;
    private MagicEngine magicEngine;
    private VessageUser chatter;
    private ImageView smileFaceImageView;
    private ImageView chatterImageView;
    private Timer recordingTimer;
    private int recordedTime = 0;

    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(cn.bahamut.vessage.R.layout.activity_record_vessage);
        smileFaceImageView = (ImageView)findViewById(R.id.smileFaceImageView);
        chatterImageView = (ImageView)findViewById(R.id.chatterImageView);
        recordingTimeLeft = (TextView)findViewById(R.id.recordingTimeLeft);
        leftButton = (Button)findViewById(R.id.leftButton);
        middleButton = (Button) findViewById(R.id.middleButton);
        rightButton = (Button)findViewById(R.id.rightButton);
        previewView = (MagicCameraView)findViewById(R.id.previewView);

        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        rightButton.setOnClickListener(onRightButtonClickListener);

        hideView(leftButton);
        hideView(rightButton);

        String videoFilePath = createVideoTmpFile();
        CameraEngine.setCameraID(1);
        MagicEngine.Builder builder = new MagicEngine.Builder((MagicCameraView) findViewById(R.id.previewView));
        magicEngine = builder
                .setVideoSize(480, 640)
                .setVideoPath(videoFilePath)
                .build();
        String chatterId = getIntent().getStringExtra("chatterId");
        String chatterMobile = getIntent().getStringExtra("chatterMobile");
        prepareChatter(chatterId,chatterMobile);

        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGE_SENDED,onVessageSended);
    }

    @Override
    protected void onDestroy() {
        magicEngine.onDestroy();
        if(recordingTimer != null){
            recordingTimer.cancel();
        }
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGE_SENDED,onVessageSended);
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        magicEngine.onResume();
    }

    @Override
    protected void onStop() {
        magicEngine.onPause();
        super.onStop();
    }

    private Observer onVessageSended = new Observer() {
        @Override
        public void update(ObserverState state) {
            ProgressHUDHelper.showHud(RecordVessageActivity.this,getResources().getString(R.string.vessage_sended),R.mipmap.check_mark,true);
        }
    };
    private TimerTask recordingTimeTask;

    private TimerTask generateRecordingTimeTask() {
        return new TimerTask() {
            @Override
            public void run() {

                recordedTime++;
                recordingTimeLeft.post(new Runnable() {
                    @Override
                    public void run() {
                        recordingTimeLeft.setText(String.valueOf(MAX_RECORD_TIME_SECOND - recordedTime));
                        if(recordedTime == MAX_RECORD_TIME_SECOND){
                            saveRecordedMedia();
                            askForSendVideo();
                        }
                    }
                });

            }
        };
    }

    private File getVideoTmpFile(){
        File tmpVideoFile = new File(getCacheDir(),"tmpVideo.mp4");
        return tmpVideoFile;
    }

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
            userService.fetchUserByMobile(chatterMobile,UserService.DefaultUserUpdatedCallback);
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
            hideView(chatterImageView);
        }else {
            hideView(smileFaceImageView);
            showView(chatterImageView);
            ImageHelper.setImageByFileId(chatterImageView,chatter.mainChatImage);
        }
    }

    private View.OnClickListener onleftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveRecordedMedia();
            createVideoTmpFile();
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(recording){
                saveRecordedMedia();
                askForSendVideo();
            }else{
                startRecord();
            }
        }
    };

    private void startRecord() {
        if(recording){
            return;
        }
        recordedTime = 0;
        recordingTimeLeft.setText(String.valueOf(MAX_RECORD_TIME_SECOND));
        if(!AndroidHelper.isEmulator(this)){
            magicEngine.changeRecordingState(true);
        }
        hidePreview();
        showView(leftButton);
        showView(rightButton);
        showView(recordingTimeLeft);
        middleButton.setBackgroundResource(R.mipmap.check_round);
        recording = true;
        recordingTimer = new Timer();
        recordingTimeTask = generateRecordingTimeTask();
        recordingTimer.schedule(recordingTimeTask,1000,1000);
    }

    private void saveRecordedMedia() {
        recording = false;
        recordingTimer.cancel();
        if(AndroidHelper.isEmulator(this)){
            copyDemoVideoToTmpFile();
        }else {
            magicEngine.changeRecordingState(false);
        }
        showPreview();
        hideView(leftButton);
        hideView(rightButton);
        hideView(recordingTimeLeft);
        middleButton.setBackgroundResource(R.mipmap.movie);

        if(getVideoTmpFile().exists()){
            Log.i("filesize",String.valueOf(getVideoTmpFile().length() / 1024) + "kb");
        }
    }

    private void copyDemoVideoToTmpFile() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.demo_video);
            FileOutputStream fos = new FileOutputStream(getVideoTmpFile().getAbsolutePath());
            int readLength = 0,len = 0;
            byte[] buffer = new byte[2048];
            while ((len = inputStream.read(buffer)) != -1) {
                // 处理下载的数据
                readLength += len;
                fos.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void askForSendVideo(){
        sendVessageVideo();
    }

    private void sendVessageVideo(){
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

    private void hidePreview(){
        previewView.setX(previewView.getX() + previewView.getWidth());
    }

    private void showView(View v){
        v.setVisibility(View.VISIBLE);
    }
    private void showPreview(){
        previewView.setX(previewView.getX() - previewView.getWidth());
    }

    @Override
    protected void onResume() {
        super.onResume();
        magicEngine.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        magicEngine.onPause();
    }

}
