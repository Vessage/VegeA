package cn.bahamut.vessage.conversation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.umeng.message.PushAgent;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.Vessage;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;
import cn.bahamut.vessage.services.file.FileAccessInfo;
import cn.bahamut.vessage.services.file.FileService;

public class ConversationViewActivity extends AppCompatActivity {

    private View mVideoPlayerContainer;
    private VideoView mVideoView;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private VideoPlayer player;

    private RoundedImageView mChatterButton;
    private ImageButton mRecordVideoButton;
    private ImageButton mNextVideoButton;

    private Conversation conversation;
    private VessageUser chatter;
    private List<Vessage> notReadVessages = new LinkedList<Vessage>();
    private Vessage presentingVessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_view);

        String conversationId = getIntent().getStringExtra("conversationId");
        if(conversationId == null){
            finish();
            Toast.makeText(this,R.string.no_conversation,Toast.LENGTH_LONG);
        }else{
            conversation = ServicesProvider.getService(ConversationService.class).openConversation(conversationId);
            if(conversation == null){
                finish();
                Toast.makeText(this, R.string.no_conversation, Toast.LENGTH_LONG);
            }else{
                setActivityTitle(conversation.noteName);
                initNotifications();
                initVideoPlayer();
                initBottomButtons();
                prepareChatter();
                initNotReadVessages();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,Menu.FIRST,0,R.string.note_conversation).setIcon(android.R.drawable.ic_menu_edit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Menu.FIRST:
                showNoteConversationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNoteConversationDialog() {

    }

    private void initNotReadVessages() {
        notReadVessages.clear();
        if(chatter != null && !StringHelper.isStringNullOrEmpty(chatter.userId)){
            List<Vessage> vsgs = ServicesProvider.getService(VessageService.class).getNotReadVessage(chatter.userId);
            notReadVessages.addAll(vsgs);
        }
        setPresentingVessage();
    }

    private void initBottomButtons() {
        mChatterButton = (RoundedImageView)findViewById(R.id.chatterButton);
        mRecordVideoButton = (ImageButton)findViewById(R.id.recordVideoButton);
        mNextVideoButton = (ImageButton)findViewById(R.id.nextMsgButton);

        mRecordVideoButton.setOnClickListener(onClickRecordButton);
        mChatterButton.setOnClickListener(onClickChatterButton);
        mNextVideoButton.setOnClickListener(onClickNextVessageButton);
    }

    private void initNotifications() {
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).addObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);

        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_NEW_VESSAGE_RECEIVED, onNewVessageReceived);
    }

    private void initVideoPlayer() {
        mVideoPlayerContainer = findViewById(R.id.videoPlayerContainer);
        mVideoView = (VideoView)findViewById(R.id.videoView);
        mVideoCenterButton = (ImageButton)findViewById(R.id.videoViewCenterButton);
        mVideoProgressBar = (ProgressBar)findViewById(R.id.videoViewProgressBar);
        player = new VideoPlayer(mVideoView,mVideoCenterButton,mVideoProgressBar);
    }

    private VideoPlayer.VideoPlayerDelegate playerDelegate = new VideoPlayer.VideoPlayerDelegate() {

        @Override
        public void onClickPlayButton(VideoPlayer player, VideoPlayer.VideoPlayerState state) {
            switch (state){
                case READY_TO_LOAD:reloadVessageVideo();break;
                case LOADED:player.playVideo();break;
                case PLAYING:player.pauseVideo();break;
                case LOAD_ERROR:reloadVessageVideo();break;
                case PAUSE:player.resumeVideo();
            }
        }

        @Override
        public void onClickPlayer(VideoPlayer player, VideoPlayer.VideoPlayerState state) {
            switch (state){
                case READY_TO_LOAD:reloadVessageVideo();break;
                case LOADED:player.playVideo();break;
                case PLAYING:player.pauseVideo();break;
                case LOAD_ERROR:reloadVessageVideo();break;
                case PAUSE:player.resumeVideo();
            }
        }
    };

    private void reloadVessageVideo() {
        if(presentingVessage != null) {
            player.setLoadingVideo();
            ServicesProvider.getService(FileService.class).fetchFileToCacheDir(presentingVessage.fileId, null, null);
        }
    }

    private Observer onDownLoadVessageProgress = new Observer() {
        @Override
        public void update(ObserverState state) {
            FileAccessInfo file = (FileAccessInfo)state.getInfo();
            if(presentingVessage != null && presentingVessage.fileId.equals(file.getFileId())){

            }
        }
    };

    private Observer onDownLoadVessageFail = new Observer() {
        @Override
        public void update(ObserverState state) {
            FileAccessInfo file = (FileAccessInfo)state.getInfo();
            if(presentingVessage != null && presentingVessage.fileId.equals(file.getFileId())){
                player.setLoadVideoError();
            }

        }
    };

    private Observer onDownLoadVessageSuccess = new Observer() {
        @Override
        public void update(ObserverState state) {
            FileAccessInfo file = (FileAccessInfo)state.getInfo();
            if(presentingVessage != null && presentingVessage.fileId.equals(file.getFileId())){
                player.setLoadedVideo();
                player.setVideoPath(file.getLocalPath(),true);
            }

        }
    };

    private void prepareChatter() {
        UserService userService = ServicesProvider.getService(UserService.class);
        userService.addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        VessageUser chatUser = null;
        if(!StringHelper.isStringNullOrEmpty(conversation.chatterId)){
            chatUser = userService.getUserById(conversation.chatterId);
            if(chatUser == null) {
                chatUser = new VessageUser();
                chatUser.userId = conversation.chatterId;
                chatUser.mobile = conversation.chatterMobile;
                userService.fetchUserByUserId(conversation.chatterId, UserService.DefaultUserUpdatedCallback);
            }
        }else {
            chatUser = new VessageUser();
            chatUser.mobile = conversation.chatterMobile;
            userService.fetchUserByMobile(chatUser.mobile,UserService.DefaultUserUpdatedCallback);
        }
        setChatter(chatUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_SUCCESS,onDownLoadVessageSuccess);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_PROGRESS,onDownLoadVessageProgress);
        ServicesProvider.getService(FileService.class).deleteObserver(FileService.NOTIFY_FILE_DOWNLOAD_FAIL,onDownLoadVessageFail);
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onVessageUserUpdated);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGE_RECEIVED, onNewVessageReceived);
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
        mChatterButton.setImageResource(R.mipmap.default_avatar);
        if(!StringHelper.isStringNullOrEmpty(chatter.mainChatImage)){
            ImageHelper.setImageByFileId(mChatterButton,chatter.avatar,R.mipmap.default_avatar);
        }
    }

    private Observer onNewVessageReceived = new Observer() {
        @Override
        public void update(ObserverState state) {
            Vessage vsg = (Vessage)state.getInfo();
            if(vsg.sender.equals(conversation.chatterId)){
                notReadVessages.add(vsg);
            }
        }
    };

    private void setActivityTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    private View.OnClickListener onClickNextVessageButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loadNextVessage();
        }
    };

    private View.OnClickListener onClickChatterButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener onClickRecordButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("chatterId", chatter.userId);
            intent.putExtra("chatterMobile",chatter.mobile);
            intent.setClass(ConversationViewActivity.this,RecordVessageActivity.class);
            startActivity(intent);
        }
    };

    private void loadNextVessage(){
        if(notReadVessages.size() > 1){
            Vessage vsg = presentingVessage;
            notReadVessages.remove(0);
            setPresentingVessage();
            ServicesProvider.getService(VessageService.class).removeVessage(vsg);
        }
    }

    private void setPresentingVessage() {
        if(notReadVessages.size() > 0){
            mVideoPlayerContainer.setVisibility(View.VISIBLE);
            this.presentingVessage = notReadVessages.get(0);
            player.setReadyToLoadVideo();
        }else {
            mVideoPlayerContainer.setVisibility(View.INVISIBLE);
        }

        if(notReadVessages.size() > 1){
            mNextVideoButton.setVisibility(View.VISIBLE);
        }else {
            mNextVideoButton.setVisibility(View.INVISIBLE);
        }
    }
}
