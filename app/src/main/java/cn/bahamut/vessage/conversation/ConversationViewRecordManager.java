package cn.bahamut.vessage.conversation;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.camera.VessageCamera;
import cn.bahamut.vessage.camera.VessageCameraBase;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/6/1.
 */
public class ConversationViewRecordManager extends ConversationViewActivity.ConversationViewProxyManager{

    class ChatFacesManager
    {
        private FrameLayout container;
        private Map<String,String> facesId;
        private RoundedImageView[] imageViews = new RoundedImageView[5];
        void init(FrameLayout facesContainer){
            this.container = facesContainer;
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i] = new RoundedImageView(getConversationViewActivity());
                imageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            facesId = new HashMap<>();
            ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onUserProfileUpdated);
        }

        void release(){
            ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onUserProfileUpdated);
            imageViews = null;
            facesId.clear();
            facesId = null;
        }

        private Observer onUserProfileUpdated = new Observer() {
            @Override
            public void update(ObserverState state) {
                VessageUser user = (VessageUser) state.getInfo();
                if(facesId.containsKey(user.userId)){
                    if(!StringHelper.isStringNullOrWhiteSpace(user.mainChatImage)){
                        facesId.put(user.userId,user.mainChatImage);
                        refreshImageViews();
                    }
                }
            }
        };

        private void setViewFrame(View view, double x, double y, double width, double height) {
            view.setX((int)x);
            view.setY((int)y);
            view.getLayoutParams().height = (int)height;
            view.getLayoutParams().width = (int)width;

        }

        private void renderImageViews(){
            int count = this.facesId.size();
            double width = this.container.getWidth() / 2;
            double height = this.container.getHeight() / 2;
            double diam = 0;
            if (count == 1) {
                setViewFrame(imageViews[0],0,0,this.container.getWidth(),this.container.getHeight());
            }else if( count == 2){
                if (width < height) {
                    width = Math.min(height,this.container.getWidth());
                }else if (height < width){
                    height = Math.min(width,this.container.getHeight());
                }
                diam = Math.min(width, height);
                setViewFrame(imageViews[0],0, 0 , diam, diam);
                setViewFrame(imageViews[1],this.container.getWidth() - diam, this.container.getHeight() - diam , diam, diam);
            }else if (count == 3){
                diam = Math.min(width, height);
                setViewFrame(imageViews[0],width - diam, height - diam , diam, diam);
                setViewFrame(imageViews[1],width, height - diam , diam, diam);
                setViewFrame(imageViews[2],width - diam / 2, height , diam, diam);
            }else if(count == 4){
                diam = Math.min(width, height);
                setViewFrame(imageViews[0],width - diam, height - diam , diam, diam);
                setViewFrame(imageViews[1],width, height - diam , diam, diam);
                setViewFrame(imageViews[2],width - diam, height , diam, diam);
                setViewFrame(imageViews[3],width, height , diam, diam);
            }else if (count == 5){
                diam = Math.min(width, height);
                setViewFrame(imageViews[0],0, 0 , diam, diam);
                setViewFrame(imageViews[1],this.container.getWidth() - diam , 0, diam, diam);
                setViewFrame(imageViews[2],0, this.container.getHeight() - diam , diam, diam);
                setViewFrame(imageViews[3],this.container.getWidth() - diam , this.container.getHeight() - diam, diam, diam);
                setViewFrame(imageViews[4],width - diam / 2, height - diam / 2 , diam, diam);
            }
            if(count == 1){
                imageViews[0].setCornerRadius(0);
            }else {
                for (int i = 0; i < count; i++) {
                    imageViews[i].setCornerRadius((float) diam / 2);
                }
            }
        }

        public void setFacesIds(Map<String,String> facesId) {
            this.facesId.clear();
            this.facesId.putAll(facesId);
            prepareImageViews();
            refreshImageViews();
        }

        private void refreshImageViews() {
            int i = 0;
            for (String userId : this.facesId.keySet()) {
                String faceId = this.facesId.get(userId);
                ImageView imgview = imageViews[i];
                if (!StringHelper.isStringNullOrWhiteSpace(faceId)){
                    ImageHelper.setImageByFileId(imgview,faceId);
                }else {
                    Bitmap bitmap = BitmapFactory.decodeStream(getConversationViewActivity().getResources().openRawResource(R.raw.default_face));
                    imgview.setImageBitmap(bitmap);
                    imgview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                i++;
            }
        }

        private void prepareImageViews() {
            container.removeAllViews();
            for (int i = 0; i < facesId.size(); i++) {
                container.addView(imageViews[i]);
                Bitmap bitmap = BitmapFactory.decodeStream(getConversationViewActivity().getResources().openRawResource(R.raw.default_face));
                imageViews[i].setImageBitmap(bitmap);
            }
            renderImageViews();
        }
    }
    private static final int MAX_RECORD_TIME_SECOND = 16;

    private Button leftButton;
    private Button middleButton;
    private TextView recordingTimeLeft;
    private SurfaceView previewView;
    private TextView noBcgTipsTextView;
    private ProgressBar sendingProgressBar;

    private boolean userClickSend = false;
    private VessageCameraBase camera;
    private ChatFacesManager chatFacesManager;

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        sendingProgressBar = (ProgressBar)findViewById(R.id.progress_sending);
        noBcgTipsTextView  = (TextView)findViewById(R.id.tv_no_chat_bcg);
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

        chatFacesManager = new ChatFacesManager();
        chatFacesManager.init((FrameLayout) findViewById(R.id.faces_cantainer));

        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SENDED_VESSAGE,onSendVessage);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SENDING_PROGRESS,onSendVessage);
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_SEND_VESSAGE_FAILURE, onSendVessage);
        if(isGroupChat()){
            onChatGroupUpdated();
        }else {
            onChatterUpdated();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        camera.stopPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        camera.startPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatFacesManager.release();
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SENDED_VESSAGE,onSendVessage);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SEND_VESSAGE_FAILURE, onSendVessage);
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_SENDING_PROGRESS,onSendVessage);
        camera.release();
    }

    @Override
    public void onChatterUpdated() {
        super.onChatterUpdated();
        Map<String,String> map = new HashMap<String,String>();
        map.put(getChatter().userId,getChatter().mainChatImage);
        chatFacesManager.setFacesIds(map);
        if (StringHelper.isStringNullOrWhiteSpace(getChatter().mainChatImage)){
            noBcgTipsTextView.setVisibility(View.VISIBLE);
        }else {
            noBcgTipsTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onChatGroupUpdated() {
        super.onChatGroupUpdated();
        noBcgTipsTextView.setVisibility(View.INVISIBLE);
        Map<String,String> map = new HashMap<>();
        UserService userService = ServicesProvider.getService(UserService.class);
        for (String userId : getChatGroup().getChatters()) {
            VessageUser user = userService.getUserById(userId);
            if(user != null){
                map.put(userId,user.mainChatImage);
            }else {
                userService.fetchUserByUserId(userId);
            }
        }
        chatFacesManager.setFacesIds(map);
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
        findViewById(R.id.faces_cantainer).setAnimation(animation);
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
        if(!StringHelper.isNullOrEmpty(getChatter().userId)){
            startSendingProgress();
            SendVessageQueue.getInstance().sendVessageToUser(getChatter().userId,videoFile,getConversation().isGroup);
        }else if(!StringHelper.isNullOrEmpty(getChatter().mobile)){
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
                getConversationViewActivity().setActivityTitle(getConversationTitle());
            }
        },2000);


    }
}
