package cn.bahamut.vessage.conversation;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.camera.VessageCamera;
import cn.bahamut.vessage.camera.VessageCameraBase;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.LocalizedStringHelper;

/**
 * Created by alexchow on 16/6/1.
 */
public class ConversationViewRecordManager extends ConversationViewActivity.ConversationViewProxyManager{
    private static final int MAX_RECORD_TIME_SECOND = 16;

    private Button leftButton;
    private Button middleButton;
    private TextView recordingTimeLeft;
    private SurfaceView previewView;
    private ImageView smileFaceImageView;
    private TextView noBcgTipsTextView;
    private ImageView chatterImageView;
    private ProgressBar sendingProgressBar;

    private boolean userClickSend = false;
    private VessageCameraBase camera;


    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        sendingProgressBar = (ProgressBar)findViewById(R.id.progress_sending);
        noBcgTipsTextView  = (TextView)findViewById(R.id.tv_no_chat_bcg);
        smileFaceImageView = (ImageView)findViewById(R.id.smile_face_img_view);
        smileFaceImageView.setImageBitmap(BitmapFactory.decodeStream(getConversationViewActivity().getResources().openRawResource(R.raw.smile_face)));
        chatterImageView = (ImageView)findViewById(R.id.chatter_img_view);
        previewView = (SurfaceView)findViewById(R.id.preview_view);
        recordingTimeLeft = (TextView)findViewById(R.id.recording_time_left_tv);
        leftButton = (Button)findViewById(R.id.left_btn);
        middleButton = (Button) findViewById(R.id.middle_btn);
        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        hideView(leftButton);
        hideView(sendingProgressBar);
        camera = new VessageCamera(getConversationViewActivity());

        camera.initCameraForRecordVideo(previewView);
        camera.setHandler(cameraHandler);

        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SENDED_VESSAGE,onSendVessage);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SENDING_PROGRESS,onSendVessage);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SEND_VESSAGE_FAILURE, onSendVessage);
        onChatterUpdated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SENDED_VESSAGE,onSendVessage);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SEND_VESSAGE_FAILURE, onSendVessage);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SENDING_PROGRESS,onSendVessage);
        camera.release();
    }

    @Override
    public void onChatterUpdated() {
        super.onChatterUpdated();
        refreshChatter();
    }

    private Observer onSendVessage = new Observer() {
        @Override
        public void update(ObserverState state) {
            SendVessageQueue.SendingInfo info = (SendVessageQueue.SendingInfo)state.getInfo();
            if(info.receiverId.equals(getChatter().userId) || info.receiverId .equals(getChatter().mobile)){
                if (info.state < 0){
                    setSendingProgressSendFaiure();
                }else if(info.state == SendVessageQueue.SendingInfo.STATE_SENDED){
                    setSendingProgressSended();
                }else if(info.state == SendVessageQueue.SendingInfo.STATE_SENDING){
                    setSendingProgress((float)info.progress);
                }
            }
        }
    };

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
                        userClickSend = false;
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

    private void refreshChatter() {
        if(StringHelper.isStringNullOrEmpty(getChatter().mainChatImage)){
            showView(smileFaceImageView);
            showView(noBcgTipsTextView);
            hideView(chatterImageView);
        }else {
            hideView(smileFaceImageView);
            hideView(noBcgTipsTextView);
            showView(chatterImageView);
            ImageHelper.setImageByFileId(chatterImageView,getChatter().mainChatImage);
        }
    }


    private View.OnClickListener onleftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            camera.cancelRecord();
            resetCamera();
            getConversationViewActivity().showPlayViews();
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(camera.isRecording()){
                saveRecordedMedia();
            }else{
                MobclickAgent.onEvent(getConversationViewActivity(),"Vege_RecordVessage");
                startRecord();
            }
        }
    };

    public void chatterImageFadeIn(){
        AlphaAnimation animation = new AlphaAnimation(0.3f,1f);
        animation.setDuration(300);
        animation.setFillAfter(true);
        chatterImageView.setAnimation(animation);
        animation.start();
    }

    public void startRecord() {
        createVideoTmpFile();
        if(camera.startRecord()){
            userClickSend = true;
            recordingTimeLeft.setText(String.valueOf(MAX_RECORD_TIME_SECOND));
            getConversationViewActivity().hidePreview();
            showView(leftButton);
            showView(recordingTimeLeft);
            hideView(middleButton);
            middleButton.setBackgroundResource(R.mipmap.check_round);
        }else {
            Toast.makeText(getConversationViewActivity(),R.string.start_record_error,Toast.LENGTH_SHORT).show();
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
        getConversationViewActivity().showPreview();
        showView(middleButton);
        hideView(leftButton);
        hideView(recordingTimeLeft);
        middleButton.setBackgroundResource(R.mipmap.movie);
    }

    private File getVideoTmpFile(){
        return new File(getConversationViewActivity().getCacheDir(),"tmpVideo.mp4");
    }

    private void askForSendVideo(){
        getConversationViewActivity().showPlayViews();
        if(userClickSend){
            sendVessageVideo();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getConversationViewActivity())
                    .setTitle(R.string.ask_send_vessage)
                    .setMessage(getChatter().nickName)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendVessageVideo();
                        }
                    });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MobclickAgent.onEvent(getConversationViewActivity(),"Vege_CancelSendVessage");
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    private void sendVessageVideo(){
        MobclickAgent.onEvent(getConversationViewActivity(),"Vege_ConfirmSendVessage");
        File videoFile = getVideoTmpFile();
        if(!StringHelper.isStringNullOrEmpty(getChatter().userId)){
            startSendingProgress();
            SendVessageQueue.getInstance().sendVessageToUser(getChatter().userId,videoFile);
        }else if(!StringHelper.isStringNullOrEmpty(getChatter().mobile)){
            startSendingProgress();
            SendVessageQueue.getInstance().sendVessageToMobile(getChatter().mobile,videoFile);
        }
    }

    private void startSendingProgress() {
        showView(sendingProgressBar);
        sendingProgressBar.setProgress(10);
        getConversationViewActivity().setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.sending_vessage));
    }

    private void setSendingProgress(float progress){
        showView(sendingProgressBar);
        sendingProgressBar.setProgress((int)(100 * progress));
    }

    private void setSendingProgressSendFaiure(){
        hideView(sendingProgressBar);
        getConversationViewActivity().setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.send_vessage_failure));
    }

    private void setSendingProgressSended(){
        getConversationViewActivity().setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.vessage_sended));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideView(sendingProgressBar);
                String noteName = getConversation().noteName;
                getConversationViewActivity().setActivityTitle(noteName);
            }
        },2000);


    }
}
