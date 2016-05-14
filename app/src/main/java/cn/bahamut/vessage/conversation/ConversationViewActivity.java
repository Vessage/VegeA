package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.umeng.analytics.MobclickAgent;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.Vessage;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;
import cn.bahamut.vessage.services.file.FileService;

public class ConversationViewActivity extends AppCompatActivity {

    private static final int REQUEST_CHANGE_NOTE_CODE = 1;
    private View mVideoPlayerContainer;
    private VideoView mVideoView;
    private ImageButton mVideoCenterButton;
    private ProgressBar mVideoProgressBar;
    private VideoPlayer player;

    private TextView badgeTextView;

    private RoundedImageView mChatterButton;
    private Button mRecordVideoButton;
    private Button mNextVideoButton;

    private Conversation conversation;
    private VessageUser chatter;
    private List<Vessage> notReadVessages = new LinkedList<Vessage>();
    private Vessage presentingVessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_view);

        String conversationId = getIntent().getStringExtra("conversationId");
        if(conversationId == null){
            finish();
            Toast.makeText(this,R.string.no_conversation,Toast.LENGTH_SHORT).show();
        }else{
            conversation = ServicesProvider.getService(ConversationService.class).openConversation(conversationId);
            if(conversation == null){
                finish();
                Toast.makeText(this, R.string.no_conversation, Toast.LENGTH_SHORT).show();
            }else{
                setActivityTitle(conversation.noteName);
                badgeTextView = (TextView)findViewById(R.id.badgeTextView);
                badgeTextView.setVisibility(View.INVISIBLE);
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

    private void showNoteConversationDialog() {
        EditPropertyActivity.showEditPropertyActivity(this,REQUEST_CHANGE_NOTE_CODE,R.string.note_conversation,conversation.noteName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CHANGE_NOTE_CODE && resultCode == EditPropertyActivity.RESULT_CODE_SAVED_PROPERTY){
            String newNoteName = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
            ServicesProvider.getService(ConversationService.class).setConversationNoteName(conversation.conversationId,newNoteName);
            setActivityTitle(newNoteName);
        }

    }

    private void initNotReadVessages() {
        notReadVessages.clear();
        if(chatter != null && !StringHelper.isStringNullOrEmpty(chatter.userId)){
            List<Vessage> vsgs = ServicesProvider.getService(VessageService.class).getNotReadVessage(chatter.userId);
            if(vsgs.size() > 0){
                notReadVessages.addAll(vsgs);
            }else {
                Vessage vsg = ServicesProvider.getService(VessageService.class).getCachedNewestVessage(chatter.userId);
                if (vsg != null){
                    notReadVessages.add(vsg);
                }
            }
        }
        setPresentingVessage();
    }

    private void initBottomButtons() {
        mChatterButton = (RoundedImageView)findViewById(R.id.chatterButton);
        mRecordVideoButton = (Button)findViewById(R.id.recordVideoButton);
        mNextVideoButton = (Button)findViewById(R.id.nextMsgButton);

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
        player = new VideoPlayer(this,mVideoView,mVideoCenterButton,mVideoProgressBar);
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
        if(!presentingVessage.isRead()){
            MobclickAgent.onEvent(ConversationViewActivity.this,"ReadVessage");
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
                userService.fetchUserByUserId(conversation.chatterId);
            }
        }else {
            chatUser = new VessageUser();
            chatUser.mobile = conversation.chatterMobile;
            userService.fetchUserByMobile(chatUser.mobile);
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
                updateBadge();
                updateNextButton();
            }
        }
    };

    private void setActivityTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    private View.OnClickListener onClickNextVessageButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(presentingVessage.isRead()){
                loadNextVessage();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ConversationViewActivity.this)
                    .setTitle(R.string.ask_jump_vessage)
                    .setMessage(R.string.jump_vessage_will_delete)
                    .setPositiveButton(R.string.jump, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobclickAgent.onEvent(ConversationViewActivity.this,"JumpVessage");
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

    private View.OnClickListener onClickChatterButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String msg;
            if(StringHelper.isStringNullOrEmpty(chatter.accountId)){
                msg = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
            }else {
                msg = LocalizedStringHelper.getLocalizedString(R.string.account) + ":" + chatter.accountId;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ConversationViewActivity.this)
                    .setTitle(conversation.noteName)
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.setCancelable(true);
            builder.show();
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

        updateBadge();
        updateNextButton();
    }

    private void updateNextButton() {
        if(notReadVessages.size() > 1){
            mNextVideoButton.setVisibility(View.VISIBLE);
        }else {
            mNextVideoButton.setVisibility(View.INVISIBLE);
        }
    }

    private void updateBadge(){
        if(notReadVessages.size() > 0){
            setBadge(notReadVessages.size() - (presentingVessage.isRead() ? 1 : 0));
        }else {
            setBadge(0);
        }
    }

    public static void openConversationView(Context context, Conversation conversation){
        MobclickAgent.onEvent(context,"OpenConversation");
        Intent intent = new Intent();
        intent.putExtra("conversationId",conversation.conversationId);
        intent.setClass(context, ConversationViewActivity.class);
        context.startActivity(intent);
    }
}
