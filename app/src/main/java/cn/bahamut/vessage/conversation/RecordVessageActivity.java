package cn.bahamut.vessage.conversation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.Image;
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

import com.seu.magicfilter.*;
import com.seu.magicfilter.widget.MagicCameraView;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.*;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.UserService;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecordVessageActivity extends Activity {

    private static final int MAX_RECORD_TIME_SECOND = 10;

    private ImageButton leftButton;
    private ImageButton middleButton;
    private ImageButton rightButton;
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
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(cn.bahamut.vessage.R.layout.activity_record_vessage);
        smileFaceImageView = (ImageView)findViewById(R.id.smileFaceImageView);
        chatterImageView = (ImageView)findViewById(R.id.chatterImageView);
        recordingTimeLeft = (TextView)findViewById(R.id.recordingTimeLeft);
        leftButton = (ImageButton)findViewById(R.id.leftButton);
        middleButton = (ImageButton) findViewById(R.id.middleButton);
        rightButton = (ImageButton)findViewById(R.id.rightButton);
        previewView = (MagicCameraView)findViewById(R.id.previewView);

        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        rightButton.setOnClickListener(onRightButtonClickListener);

        hideView(leftButton);
        hideView(rightButton);

        String videoFilePath = createVideoTmpFile();
        MagicEngine.Builder builder = new MagicEngine.Builder((MagicCameraView) findViewById(R.id.previewView));
        magicEngine = builder
                .setVideoSize(480, 640)
                .setVideoPath(videoFilePath)
                .build();

        String chatterId = getIntent().getStringExtra("chatterId");
        String chatterMobile = getIntent().getStringExtra("chatterMobile");
        prepareChatter(chatterId,chatterMobile);
    }

    private TimerTask recordingTimeTask = new TimerTask() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        magicEngine.onDestroy();
        recordingTimer.cancel();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        magicEngine.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        magicEngine.onPause();
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
        magicEngine.changeRecordingState(true);
        hidePreview();
        showView(leftButton);
        showView(rightButton);
        showView(recordingTimeLeft);
        middleButton.setImageResource(R.mipmap.check_round);
        recording = true;
        recordingTimer = new Timer();
        recordingTimer.schedule(recordingTimeTask,1000,1000);
    }

    private void saveRecordedMedia() {
        recording = false;
        recordingTimer.cancel();
        magicEngine.changeRecordingState(false);
        showPreview();
        hideView(leftButton);
        hideView(rightButton);
        hideView(recordingTimeLeft);
        middleButton.setImageResource(R.mipmap.movie);

        if(getVideoTmpFile().exists()){
            Log.i("filesize",String.valueOf(getVideoTmpFile().length() / 1024) + "kb");
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
        hideView(previewView);
    }

    private void showView(View v){
        v.setVisibility(View.VISIBLE);
    }
    private void showPreview(){
        showView(previewView);
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
