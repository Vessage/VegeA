package cn.bahamut.vessage.conversation.vessagehandler;

import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.conversation.view.VideoPlayer;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public class VideoVessageHandler extends VessageHandlerBase{
    private View mVideoPlayerContainer;
    private VideoView mVideoView;
    private VideoPlayer player;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private TextView videoDateTextView;

    public VideoVessageHandler(ConversationViewPlayManager playVessageManager,ViewGroup vessageContainer) {
        super(playVessageManager,vessageContainer);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
        initVideoPlayer();
    }

    @Override
    public void releaseHandler() {
        super.releaseHandler();
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
    }

    @Override
    public void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage) {
        super.onPresentingVessageSeted(oldVessage, newVessage);
        if (oldVessage == null || oldVessage.typeId != newVessage.typeId){
            vessageContainer.removeAllViews();
            vessageContainer.addView(mVideoPlayerContainer);
        }
        setVideoPlayerContainerLayoutParams();
        player.setNoFile();
        player.setReadyToLoadVideo();
        updateVideoDateTextView();
    }

    private void setVideoPlayerContainerLayoutParams() {
        Point size = new Point();
        playVessageManager.getConversationViewActivity().getWindowManager().getDefaultDisplay().getSize(size);
        ViewGroup.LayoutParams params = mVideoPlayerContainer.getLayoutParams();
        params.height = size.y / 2;
        params.width = params.height * 3 / 4;
        mVideoPlayerContainer.setLayoutParams(params);
    }

    private void initVideoPlayer() {
        mVideoPlayerContainer = playVessageManager.getConversationViewActivity().getLayoutInflater().inflate(R.layout.video_vessage_container,null);
        videoDateTextView = (TextView)mVideoPlayerContainer.findViewById(R.id.video_date_tv);
        mVideoView = (VideoView)mVideoPlayerContainer.findViewById(R.id.video_view);
        mVideoCenterButton = (ImageButton)mVideoPlayerContainer.findViewById(R.id.video_view_center_btn);
        mVideoProgressBar = (ProgressBar)mVideoPlayerContainer.findViewById(R.id.video_progress);
        player = new VideoPlayer(playVessageManager.getConversationViewActivity(),mVideoView,mVideoCenterButton,mVideoProgressBar);
        player.setDelegate(playerDelegate);
    }

    private VideoPlayer.VideoPlayerDelegate playerDelegate = new VideoPlayer.VideoPlayerDelegate() {

        @Override
        public void onClickPlayButton(VideoPlayer player, VideoPlayer.VideoPlayerState state) {
            switch (state){
                case READY_TO_LOAD:reloadVessageVideo();break;
                case LOADED:player.playVideo();playVessageManager.readVessage();break;
                case PLAYING:player.pauseVideo();break;
                case LOAD_ERROR:reloadVessageVideo();break;
                case PAUSE:player.resumeVideo();
            }
        }
    };

    private void reloadVessageVideo() {
        if(this.presentingVessage != null) {
            player.setLoadingVideo();
            ServicesProvider.getService(FileService.class).fetchFileToCacheDir(presentingVessage.fileId,".mp4", null, null);
        }
    }

    private void updateVideoDateTextView() {
        if (presentingVessage != null){
            Date sendTime = DateHelper.stringToAccurateDate(presentingVessage.sendTime);
            String friendlyDateString = AppUtil.dateToFriendlyString(playVessageManager.getConversationViewActivity(),sendTime);
            String readStatus = LocalizedStringHelper.getLocalizedString(presentingVessage.isRead ? R.string.vsg_readed : R.string.vsg_unreaded);
            videoDateTextView.setText(String.format("%s %s",friendlyDateString,readStatus));
        }
    }


    private Observer onDownLoadVessageProgress = new Observer() {
        @Override
        public void update(ObserverState state) {
        }
    };

    private Observer onDownLoadVessageFail = new Observer() {
        @Override
        public void update(ObserverState state) {
            FileService.FileNotifyState fileNotifyState = (FileService.FileNotifyState)state.getInfo();
            String fetchedFileId = fileNotifyState.getFileAccessInfo().getFileId();
            if(presentingVessage != null && presentingVessage.fileId.equals(fetchedFileId)){
                player.setLoadVideoError();
            }

        }
    };

    private Observer onDownLoadVessageSuccess = new Observer() {
        @Override
        public void update(ObserverState state) {
            FileService.FileNotifyState fileNotifyState = (FileService.FileNotifyState)state.getInfo();
            String fetchedFileId = fileNotifyState.getFileAccessInfo().getFileId();
            if(presentingVessage != null && presentingVessage.fileId.equals(fetchedFileId)){
                player.setLoadedVideo();
                player.setVideoPath(fileNotifyState.getFileAccessInfo().getLocalPath(),true);
                playVessageManager.readVessage();
            }
        }
    };

    private AudioManager audioManager = null;
    private int volumeMax = 100;

    @Override
    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        if(audioManager == null){
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        int v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        v += (distanceY / 10);
        if (v < 0) {
            v = 0;
        } else if (v > volumeMax) {
            v = volumeMax;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, v, AudioManager.FLAG_PLAY_SOUND);
        //Toast.makeText(getContext(),String.format(LocalizedStringHelper.getLocalizedString(R.string.x_vol),v),Toast.LENGTH_SHORT).show();
    }
}
