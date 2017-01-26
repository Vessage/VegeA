package cn.bahamut.vessage.conversation.chat.videochat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.FileHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.common.ViewHelper;
import cn.bahamut.common.progressbar.CircleProgressBar;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.camera.VessageCamera;
import cn.bahamut.vessage.camera.VessageCameraBase;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/6/1.
 */
public class RecordChatVideoManager{

    class ChatFacesManager
    {
        private FrameLayout container;
        private Map<String,String> facesId;
        private RoundedImageView[] imageViews = new RoundedImageView[5];
        void init(FrameLayout facesContainer){
            this.container = facesContainer;
            for (int i = 0; i < imageViews.length; i++) {
                imageViews[i] = new RoundedImageView(getActivity());
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
            ViewHelper.setViewFrame(view,x,y,width,height);
        }

        private void renderImageViews(){
            int count = this.facesId.size();
            Point containerSize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(containerSize);
            double width = containerSize.x / 2;
            double height = containerSize.y / 2;
            double diam = 0;
            if (count == 1) {
                setViewFrame(imageViews[0],0,0,containerSize.x,containerSize.y);
            }else if( count == 2){
                if (width < height) {
                    width = Math.min(height,containerSize.x);
                }else if (height < width){
                    height = Math.min(width,containerSize.y);
                }
                diam = Math.min(width, height);
                setViewFrame(imageViews[0],0, 0 , diam, diam);
                setViewFrame(imageViews[1],containerSize.x - diam, containerSize.y - diam , diam, diam);
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
                setViewFrame(imageViews[1],containerSize.x - diam , 0, diam, diam);
                setViewFrame(imageViews[2],0, containerSize.y - diam , diam, diam);
                setViewFrame(imageViews[3],containerSize.x - diam , containerSize.y - diam, diam, diam);
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
                    imgview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ImageHelper.setImageByFileId(imgview,faceId);
                }else {
                    Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getResources().openRawResource(R.raw.default_face));
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
                imageViews[i].setVisibility(View.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getResources().openRawResource(R.raw.default_face));
                imageViews[i].setImageBitmap(bitmap);
            }
            renderImageViews();
        }
    }
    private static final int MAX_RECORD_TIME_SECOND = 16;

    private Button leftButton;
    private Button middleButton;
    private CircleProgressBar recordedProgress;
    private View recordingView;
    private SurfaceView previewView;
    private TextView noBcgTipsTextView;


    private boolean userClickSend = false;
    private VessageCameraBase camera;
    private ChatFacesManager chatFacesManager;

    private ConversationRecordChatVideoActivity activity;

    public void initManager(ConversationRecordChatVideoActivity activity) {
        this.activity = activity;
        noBcgTipsTextView  = (TextView)findViewById(R.id.tv_no_chat_bcg);
        previewView = (SurfaceView)findViewById(R.id.preview_view);
        recordedProgress = (CircleProgressBar) findViewById(R.id.recorded_pregress);
        recordingView = findViewById(R.id.recording_view);
        leftButton = (Button)findViewById(R.id.rec_left_btn);
        middleButton = (Button) findViewById(R.id.middle_btn);
        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        camera = new VessageCamera(getActivity());
        camera.initCameraForRecordVideo(previewView);
        camera.setHandler(cameraHandler);
    }

    public void onResume(){
        if (chatFacesManager == null){
            chatFacesManager = new ChatFacesManager();
            chatFacesManager.init((FrameLayout) findViewById(R.id.faces_cantainer));
            onChatGroupUpdated();
        }
        chatterImageFadeIn();
    }

    private void hideView(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    private View findViewById(int viewId) {
        return activity.findViewById(viewId);
    }

    public void onDestroy() {
        camera.release();
        chatFacesManager.release();
    }

    public void onChatGroupUpdated() {
        noBcgTipsTextView.setVisibility(View.INVISIBLE);
        Map<String,String> map = new HashMap<>();
        UserService userService = ServicesProvider.getService(UserService.class);
        for (String userId : getChatGroup().getChatters()) {
            if (userId.equals(UserSetting.getUserId())){
                continue;
            }
            VessageUser user = userService.getUserById(userId);
            if(user != null){
                map.put(userId,user.mainChatImage);
            }else {
                map.put(userId,null);
                userService.fetchUserByUserId(userId);
            }
        }
        chatFacesManager.setFacesIds(map);
    }

    private ChatGroup getChatGroup() {
        return getActivity().getChatGroup();
    }

    private VessageCameraBase.VessageCameraHandler cameraHandler = new VessageCameraBase.VessageCameraHandler() {

        @Override
        public void onCameraPreviewReady(VessageCameraBase camera) {
            startRecord();
        }

        @Override
        public void onRecordingTiming(VessageCameraBase camera,final int recordedTime) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(recordedTime < 1){
                        middleButton.setVisibility(View.INVISIBLE);
                    }else {
                        middleButton.setVisibility(View.VISIBLE);
                    }
                    int left = MAX_RECORD_TIME_SECOND - recordedTime;
                    recordingView.setVisibility(left % 2 == 0 ? View.INVISIBLE : View.VISIBLE);
                    recordedProgress.setProgress(recordedTime * 100 / MAX_RECORD_TIME_SECOND);
                    if(left == 0){
                        userClickSend = false;
                        saveRecordedMedia();
                    }
                }
            });


        }
    };

    private ConversationRecordChatVideoActivity getActivity() {
        return activity;
    }

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
            AnimationHelper.startAnimation(getActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    camera.cancelRecord();
                    resetCamera();
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            });
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(camera.isRecording()){
                        saveRecordedMedia();
                    }
                }
            });

        }
    };

    public void chatterImageFadeIn(){
        AlphaAnimation animation = new AlphaAnimation(0.3f,1f);
        animation.setDuration(300);
        animation.setFillAfter(true);
        findViewById(R.id.faces_cantainer).setAnimation(animation);
        animation.start();
    }

    public void onSwitchToManager() {
        chatFacesManager.renderImageViews();
        chatFacesManager.refreshImageViews();
    }

    public void onSwitchOut() {
        onleftButtonClickListener.onClick(leftButton);
    }

    public void startRecord() {
        createVideoTmpFile();
        if(camera.startRecord()){
            userClickSend = true;
            showView(leftButton);
            showView(recordedProgress);
            recordedProgress.setProgress(0);
            showView(recordingView);
            hideView(middleButton);
            middleButton.setBackgroundResource(R.mipmap.check_round);
        }else {
            showView(leftButton);
            hideView(middleButton);
            hideView(recordingView);
            hideView(recordedProgress);
            Toast.makeText(getActivity(),R.string.start_record_error,Toast.LENGTH_SHORT).show();
        }
    }

    private void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void saveRecordedMedia() {
        camera.stopAndSaveRecordedVideo(new VessageCamera.CameraOnSavedVideo() {
            @Override
            public void onVideoSaved(File file) {
                File desc = getVideoTmpFile();
                FileHelper.customBufferBufferedStreamCopy(file,desc);
                finishRecordVideo();
            }
        });

        resetCamera();
    }

    private void resetCamera() {
        showView(middleButton);
        hideView(leftButton);
        hideView(recordingView);
        hideView(recordedProgress);
    }

    private File tmpFile = null;
    private File getVideoTmpFile(){
        if (tmpFile == null){
            tmpFile = FileHelper.generateTempFile(getActivity(),"mp4");
        }
        return tmpFile;
    }

    private void finishRecordVideo(){
        Intent data = new Intent();
        data.putExtra("file",getVideoTmpFile().getAbsolutePath());
        data.putExtra("confirm",!userClickSend);
        getActivity().setResult(Activity.RESULT_OK,data);
        getActivity().finish();
    }
}
