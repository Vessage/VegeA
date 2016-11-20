package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Date;

import cn.bahamut.common.BTSize;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.DensityUtil;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.chat.views.VideoPlayer;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 2016/11/4.
 */

public class VideoBubbleVessageHandler implements BubbleVessageHandler {

    private Activity context;
    private TextView dateTextView;
    private VideoView videoView;
    private ImageButton centerButton;
    private VideoPlayer player;
    private Vessage presentingVessage;
    private ProgressBar progressBar;

    @Override
    public BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView) {
        this.context = context;
        float defaultWidth = DensityUtil.dip2px(context, 180);
        float defaultHeight = DensityUtil.dip2px(context, 240);

        if (maxLimitedSize.width >= defaultWidth && maxLimitedSize.height >= defaultHeight) {
            return new BTSize(defaultWidth, defaultHeight);
        } else if (maxLimitedSize.height > maxLimitedSize.width) {
            return new BTSize(maxLimitedSize.width, maxLimitedSize.width * defaultHeight / defaultWidth);
        } else if (maxLimitedSize.width > maxLimitedSize.height) {
            return new BTSize(maxLimitedSize.height * defaultWidth / defaultHeight, maxLimitedSize.height);
        }
        return BTSize.ZERO;
    }

    @Override
    public ViewGroup getContentView(Activity context, Vessage vessage) {
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
        this.context = context;
        return initVideoPlayer(context);
    }

    @Override
    public void presentContent(Activity context, Vessage oldVessage, Vessage newVessage, View contentView) {
        this.context = context;
        this.presentingVessage = newVessage;
        player.setNoFile();
        if (presentingVessage.isMySendingVessage()) {
            player.setLoadedVideo();
            player.setVideoPath(presentingVessage.fileId, false);
        } else {
            player.setReadyToLoadVideo();
        }
        updateVideoDateTextView();
    }

    @Override
    public void onUnloadVessage(Activity context) {
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
        presentingVessage = null;
    }

    @Override
    public void onPrepareVessage(Activity context, Vessage vessage) {

    }

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        return new VideoBubbleVessageHandler();
    }

    private ViewGroup initVideoPlayer(Activity context) {
        ViewGroup vg = (ViewGroup) context.getLayoutInflater().inflate(R.layout.vessage_content_video,null);
        dateTextView = (TextView)vg.findViewById(R.id.date_tv);
        videoView = (VideoView)vg.findViewById(R.id.video_view);
        centerButton = (ImageButton)vg.findViewById(R.id.center_btn);
        progressBar = (ProgressBar)vg.findViewById(R.id.progress);
        player = new VideoPlayer(context,videoView, centerButton, progressBar);
        player.setDelegate(playerDelegate);
        return vg;
    }


    private VideoPlayer.VideoPlayerDelegate playerDelegate = new VideoPlayer.VideoPlayerDelegate() {

        @Override
        public void onVideoCompleted(VideoPlayer player) {

        }

        @Override
        public void onStateChanged(VideoPlayer player, VideoPlayer.VideoPlayerState old, VideoPlayer.VideoPlayerState newState) {
            if (newState == VideoPlayer.VideoPlayerState.LOADED){
                player.playVideo();
                ServicesProvider.getService(VessageService.class).readVessage(presentingVessage);
                updateVideoDateTextView();
            }
        }

        @Override
        public void onClickPlayButton(VideoPlayer player, VideoPlayer.VideoPlayerState state) {
            switch (state){
                case READY_TO_LOAD:reloadVessageVideo();break;
                case COMPLETED:
                case LOADED:
                    player.playVideo();
                    break;
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
            Date sendTime = DateHelper.getDateFromUnixTimeSpace(presentingVessage.ts);
            String friendlyDateString = AppUtil.dateToFriendlyString(context,sendTime);
            String readStatus = LocalizedStringHelper.getLocalizedString(presentingVessage.isRead ? R.string.vsg_readed : R.string.vsg_unreaded);
            dateTextView.setText(String.format("%s %s",friendlyDateString,readStatus));
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
            if(state.getInfo() != null){
                FileService.FileNotifyState fileNotifyState = (FileService.FileNotifyState)state.getInfo();
                String fetchedFileId = fileNotifyState.getFileAccessInfo().getFileId();
                if(presentingVessage != null && presentingVessage.fileId != null && presentingVessage.fileId.equals(fetchedFileId)){
                    player.setLoadedVideo();
                    player.setVideoPath(fileNotifyState.getFileAccessInfo().getLocalPath(),true);
                }else {
                    player.setLoadVideoError();
                }
            }

        }
    };
}
