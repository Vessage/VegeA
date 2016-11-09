package cn.bahamut.vessage.conversation.chat;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
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
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.DateHelper;
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
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageTaskSteps;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/6/1.
 */
public class RecordChatVideoManager extends ConversationViewManagerBase{

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
            ViewHelper.setViewFrame(view,x,y,width,height);
        }

        private void renderImageViews(){
            int count = this.facesId.size();
            double width = this.container.getWidth() / 2;
            double height = this.container.getHeight() / 2;
            double diam = 0;
            if (count == 1) {
                Point point = new Point();
                getConversationViewActivity().getWindowManager().getDefaultDisplay().getSize(point);
                setViewFrame(imageViews[0],0,0,point.x,point.y);
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
                    imgview.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
                imageViews[i].setVisibility(View.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeStream(getConversationViewActivity().getResources().openRawResource(R.raw.default_face));
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

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        noBcgTipsTextView  = (TextView)findViewById(R.id.tv_no_chat_bcg);
        previewView = (SurfaceView)findViewById(R.id.preview_view);
        recordedProgress = (CircleProgressBar) findViewById(R.id.recorded_pregress);
        recordingView = findViewById(R.id.recording_view);
        leftButton = (Button)findViewById(R.id.rec_left_btn);
        middleButton = (Button) findViewById(R.id.middle_btn);
        leftButton.setOnClickListener(onleftButtonClickListener);
        middleButton.setOnClickListener(onMiddleButtonClickListener);
        hideView(leftButton);
        camera = new VessageCamera(getConversationViewActivity());

        camera.initCameraForRecordVideo(previewView);
        camera.setHandler(cameraHandler);

        chatFacesManager = new ChatFacesManager();
        chatFacesManager.init((FrameLayout) findViewById(R.id.faces_cantainer));
        onChatGroupUpdated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        camera.release();
        chatFacesManager.release();
    }

    @Override
    public void onChatGroupUpdated() {
        super.onChatGroupUpdated();
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

    private VessageCameraBase.VessageCameraHandler cameraHandler = new VessageCameraBase.VessageCameraHandler() {

        @Override
        public void onCameraPreviewReady(VessageCameraBase camera) {
            camera.stopPreview();
        }

        @Override
        public void onRecordingTiming(VessageCameraBase camera,final int recordedTime) {
            getConversationViewActivity().runOnUiThread(new Runnable() {
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
            AnimationHelper.startAnimation(getConversationViewActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    camera.cancelRecord();
                    resetCamera();
                    getConversationViewActivity().showPlayViews();
                }
            });
        }
    };

    private View.OnClickListener onMiddleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(),v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(camera.isRecording()){
                        saveRecordedMedia();
                    }else{
                        MobclickAgent.onEvent(getConversationViewActivity(),"Vege_RecordVessage");
                        startRecord();
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

    @Override
    public void onSwitchToManager() {
        super.onSwitchToManager();
        camera.startPreview();
        chatFacesManager.renderImageViews();
        chatFacesManager.refreshImageViews();
        getConversationViewActivity().showPreview();
    }

    @Override
    public void onSwitchOut() {
        super.onSwitchOut();
        camera.stopPreview();
        getConversationViewActivity().hidePreview();
    }

    public void startRecord() {
        createVideoTmpFile();
        if(camera.startRecord()){
            userClickSend = true;
            getConversationViewActivity().showPreview();
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
        hideView(recordingView);
        hideView(recordedProgress);
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
        if(!StringHelper.isNullOrEmpty(getConversation().chatterId)){
            getConversationViewActivity().startSendingProgress();
            Vessage vessage = new Vessage();
            vessage.isGroup = getConversation().type == Conversation.TYPE_GROUP_CHAT;
            vessage.typeId = Vessage.TYPE_CHAT_VIDEO;
            vessage.extraInfo = getConversationViewActivity().getSendVessageExtraInfo();
            vessage.ts = DateHelper.getUnixTimeSpan();
            if (AndroidHelper.isEmulator(getConversationViewActivity())){
                vessage.fileId = "5790435e99cc251974a42f61";
                videoFile.delete();
                SendVessageQueue.getInstance().pushSendVessageTask(getConversation().chatterId,vessage,SendVessageTaskSteps.SEND_NORMAL_VESSAGE_STEPS,null);
            }else {
                SendVessageQueue.getInstance().pushSendVessageTask(getConversation().chatterId,vessage, SendVessageTaskSteps.SEND_FILE_VESSAGE_STEPS,videoFile.getAbsolutePath());
            }

        }
    }


}
