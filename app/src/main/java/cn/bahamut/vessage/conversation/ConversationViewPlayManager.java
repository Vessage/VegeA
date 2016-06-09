package cn.bahamut.vessage.conversation;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/6/1.
 */
public class ConversationViewPlayManager extends ConversationViewActivity.ConversationViewProxyManager{
    private List<Vessage> notReadVessages = new LinkedList<>();
    private Vessage presentingVessage;

    private View mVideoPlayerContainer;
    private VideoView mVideoView;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private VideoPlayer player;
    private TextView videoDateTextView;
    private TextView badgeTextView;
    private Button mRecordVideoButton;
    private Button mNextVideoButton;

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        badgeTextView = (TextView)findViewById(R.id.badgeTextView);
        badgeTextView.setVisibility(View.INVISIBLE);
        initVideoPlayer();
        initBottomButtons();
        initNotReadVessages();
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
    }


    private void setBadge(int badge){
        if(badge == 0){
            setBadge(null);
        }else {
            setBadge(String.valueOf(badge));
        }
    }

    private void setBadge(String badge){
        if(StringHelper.isStringNullOrEmpty(badge)){
            badgeTextView.setVisibility(View.INVISIBLE);
        }else {
            badgeTextView.setVisibility(View.VISIBLE);
            badgeTextView.setText(badge);
        }
    }

    private void initNotReadVessages() {
        notReadVessages.clear();
        if(getChatter() != null && !StringHelper.isStringNullOrEmpty(getChatter().userId)){
            List<Vessage> vsgs = ServicesProvider.getService(VessageService.class).getNotReadVessage(getChatter().userId);
            if(vsgs.size() > 0){
                notReadVessages.addAll(vsgs);
            }else {
                Vessage vsg = ServicesProvider.getService(VessageService.class).getCachedNewestVessage(getChatter().userId);
                if (vsg != null){
                    notReadVessages.add(vsg);
                }
            }
        }
        setPresentingVessage();
    }

    private void initBottomButtons() {
        mRecordVideoButton = (Button)findViewById(R.id.recordVideoButton);
        mNextVideoButton = (Button)findViewById(R.id.nextMsgButton);

        mRecordVideoButton.setOnClickListener(onClickRecordButton);
        mNextVideoButton.setOnClickListener(onClickNextVessageButton);

        findViewById(R.id.noMsgTipsTextView).setOnClickListener(onClickRecordButton);
    }

    private void initVideoPlayer() {
        videoDateTextView = (TextView)findViewById(R.id.videoDateTextView);
        mVideoPlayerContainer = findViewById(R.id.videoPlayerContainer);
        mVideoView = (VideoView)findViewById(R.id.videoView);
        mVideoCenterButton = (ImageButton)findViewById(R.id.videoViewCenterButton);
        mVideoProgressBar = (ProgressBar)findViewById(R.id.videoViewProgressBar);
        player = new VideoPlayer(this.getConversationViewActivity(),mVideoView,mVideoCenterButton,mVideoProgressBar);
        player.setDelegate(playerDelegate);
    }

    private VideoPlayer.VideoPlayerDelegate playerDelegate = new VideoPlayer.VideoPlayerDelegate() {

        @Override
        public void onClickPlayButton(VideoPlayer player, VideoPlayer.VideoPlayerState state) {
            switch (state){
                case READY_TO_LOAD:reloadVessageVideo();break;
                case LOADED:player.playVideo();readVessage();break;
                case PLAYING:player.pauseVideo();break;
                case LOAD_ERROR:reloadVessageVideo();break;
                case PAUSE:player.resumeVideo();
            }
        }
    };

    private void readVessage() {
        if(!presentingVessage.isRead){
            MobclickAgent.onEvent(getConversationViewActivity(),"Vege_ReadVessage");
        }
        ServicesProvider.getService(VessageService.class).readVessage(presentingVessage);
        updateBadge();
    }

    private void reloadVessageVideo() {
        if(presentingVessage != null) {
            player.setLoadingVideo();
            ServicesProvider.getService(FileService.class).fetchFileToCacheDir(presentingVessage.fileId,".mp4", null, null);
        }
    }
    private void setPresentingVessage() {

        if(notReadVessages.size() > 0){
            mVideoPlayerContainer.setVisibility(View.VISIBLE);
            this.presentingVessage = notReadVessages.get(0);
            player.setReadyToLoadVideo();
            updateVideoDateTextView();
        }else {
            mVideoPlayerContainer.setVisibility(View.INVISIBLE);
        }
        updateBadge();
        updateNextButton();
    }

    @Override
    public void onVessagesReceived(Collection<Vessage> vessages) {
        super.onVessagesReceived(vessages);
        notReadVessages.addAll(vessages);
        setPresentingVessage();
    }

    private void loadNextVessage(){
        if(notReadVessages.size() > 1){
            Vessage vsg = presentingVessage;
            String fileId = vsg.fileId;
            notReadVessages.remove(0);
            player.setNoFile();
            setPresentingVessage();
            ServicesProvider.getService(VessageService.class).removeVessage(vsg);
            File oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId,".mp4");
            if(oldVideoFile != null){
                try{
                    oldVideoFile.delete();
                    Log.d("ConversationView","Delete Passed Vessage Video File");
                }catch (Exception ex){
                    oldVideoFile.deleteOnExit();
                    Log.d("ConversationView","Delete Passed Vessage Video File On Exit");
                }
            }
        }
    }

    private void updateVideoDateTextView() {
        if (presentingVessage != null){
            Date sendTime = DateHelper.stringToAccurateDate(presentingVessage.sendTime);
            String friendlyDateString = AppUtil.dateToFriendlyString(getConversationViewActivity(),sendTime);
            String readStatus = LocalizedStringHelper.getLocalizedString(presentingVessage.isRead ? R.string.vsg_readed : R.string.vsg_unreaded);
            videoDateTextView.setText(String.format("%s %s",friendlyDateString,readStatus));
        }
    }

    private void updateNextButton() {
        if(notReadVessages.size() > 1){
            mNextVideoButton.setVisibility(View.VISIBLE);
        }else {
            mNextVideoButton.setVisibility(View.INVISIBLE);
        }
    }

    private void updateBadge(){
        if(getChatter() != null && StringHelper.isStringNullOrWhiteSpace(getChatter().userId) == false){
            int badge = ServicesProvider.getService(VessageService.class).getNotReadVessageCount(getChatter().userId);
            setBadge(badge);
        }else {
            setBadge(0);
        }
    }


    private View.OnClickListener onClickNextVessageButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(presentingVessage.isRead){
                loadNextVessage();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getConversationViewActivity())
                    .setTitle(R.string.ask_jump_vessage)
                    .setMessage(R.string.jump_vessage_will_delete)
                    .setPositiveButton(R.string.jump, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobclickAgent.onEvent(getConversationViewActivity(),"Vege_JumpVessage");
                            loadNextVessage();
                        }
                    });

            builder.setNegativeButton(R.string.cancel_jump, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    };

    private View.OnClickListener onClickRecordButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getConversationViewActivity().tryShowRecordViews();
        }
    };

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
                readVessage();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
    }
}
