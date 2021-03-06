package cn.bahamut.vessage.conversation.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.BTSize;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.DensityUtil;
import cn.bahamut.common.IDUtil;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.chat.bubblevessage.BubbleVessageHandler;
import cn.bahamut.vessage.conversation.chat.bubblevessage.BubbleVessageHandlerManager;
import cn.bahamut.vessage.conversation.chat.bubblevessage.SelectChatImageBubbleHandler;
import cn.bahamut.vessage.conversation.chat.videochat.ConversationRecordChatVideoActivity;
import cn.bahamut.vessage.conversation.chat.views.BezierBubbleView;
import cn.bahamut.vessage.conversation.chat.views.BubbleVessageContainer;
import cn.bahamut.vessage.conversation.chat.views.ChattersBoard;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.user.ChatImage;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import cn.bahamut.vessage.usersettings.ChatImageManageActivity;
import cn.bahamut.vessage.usersettings.UpdateChatImageActivity;

/**
 * Created by alexchow on 16/6/1.
 */
@Deprecated
abstract public class PlayVessageManager extends ConversationViewManagerBase implements VessageGestureHandler {
    /*
    private static final int bubbleColorMyVessageColor = Color.parseColor("#aa0000aa");
    private static final int bubbleColorNormalVessageColor = Color.parseColor("#aaffffff");

    private static final String[][] RANDOM_HELLO_MESSAGES = {
            new String[]{"...", "。。。"},
            new String[]{"。。。", "！！！！", "..."}
    };

    static private String getRandomTextHelloMessage(boolean isGroup) {
        String[] msgs = RANDOM_HELLO_MESSAGES[isGroup ? 1 : 0];
        return msgs[((int) (Math.random() * 1000)) % msgs.length];
    }

    private static final String TAG = "PlayManager";
    private List<Vessage> vessagesQueue = new LinkedList<>();
    private int currentIndex = -1;

    private int maxIndex = -1;

    private boolean navigateVessageLocked = false;

    private ViewGroup vessageContentContainer;
    private Button mNewChatButton;
    private Button mNewImageButton;
    private Button mNewTextButton;

    private ProgressBar progressReading;

    private ChattersBoard topChattersBoard;
    private ChattersBoard bottomChattersBoard;


    private SendMoreTypeVessageManager sendMoreTypeVessageManager;
    private MessageInputViewManager inputViewManager;

    public Vessage getCurrentVessage() {
        if (currentIndex >= 0 && vessagesQueue.size() > currentIndex) {
            return vessagesQueue.get(currentIndex);
        }
        return null;
    }

    public ChattersBoard getTopChattersBoard() {
        return topChattersBoard;
    }

    public ChattersBoard getBottomChattersBoard() {
        return bottomChattersBoard;
    }

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        vessageContentContainer = (ViewGroup) findViewById(R.id.vsg_content_container);
        progressReading = (ProgressBar) activity.findViewById(R.id.progress_reading);
        progressReading.setMax(100);
        SelectChatImageBubbleHandler.instance.initSelectHandler(activity);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initChattersBoard();
        initBottomButtons();
        inputViewManager = new MessageInputViewManager(activity);
        inputViewManager.setDelegate(sendImageChatMessageManagerDelegate);
        sendMoreTypeVessageManager = new SendMoreTypeVessageManager(activity);
        initNotReadVessages();
        onChatGroupUpdated();
        SelectChatImageBubbleHandler.instance.setOnSelectedChatImageListener(onChatImageSelectedListener);
    }

    private void initChattersBoard() {
        topChattersBoard = (ChattersBoard) vessageContentContainer.findViewById(R.id.top_chatters_board);
        bottomChattersBoard = (ChattersBoard) vessageContentContainer.findViewById(R.id.bottom_chatters_board);
        topChattersBoard.setOnClickChatterListener(onClickChatterListener);
        bottomChattersBoard.setOnClickChatterListener(onClickChatterListener);
    }

    private ChattersBoard.OnClickChatterListener onClickChatterListener = new ChattersBoard.OnClickChatterListener() {

        @Override
        public void onClickedChatter(ChattersBoard.ChatterBoardChatterModel model) {
            if (UserSetting.getUserId().equals(model.chatterItem.getChatter().userId)) {
                showSelectChatImageBubbleView(model);
            }
        }
    };

    private void showSelectChatImageBubbleView(ChattersBoard.ChatterBoardChatterModel model) {
        if (selectChatImageBubbleView == null) {
            selectChatImageBubbleView = new BubbleVessageContainer(getConversationViewActivity());
            this.vessageContentContainer.addView(selectChatImageBubbleView);
            selectChatImageBubbleView.setFillColor(bubbleColorNormalVessageColor);
        }

        hideBubbleView(vessageBubbleView);
        SelectChatImageBubbleHandler handler = SelectChatImageBubbleHandler.instance;
        View contentView = handler.getContentView(getConversationViewActivity(), null);
        layoutVessageContentView(selectChatImageBubbleView, model, null, handler, contentView);
        selectChatImageBubbleView.setContentView(contentView);
        navigateVessageLocked = true;
    }

    private void hideSelectChatImageBubbleView() {
        hideBubbleView(selectChatImageBubbleView);
        showBubbleView(vessageBubbleView);
        navigateVessageLocked = false;
    }

    private SelectChatImageBubbleHandler.OnChatImageSelectedListener onChatImageSelectedListener = new SelectChatImageBubbleHandler.OnChatImageSelectedListener() {
        @Override
        public void onChatImageSelected(int index, ChatImage chatImage) {
            getBottomChattersBoard().setImageOfChatter(UserSetting.getUserId(), chatImage.imageId);
            getTopChattersBoard().setImageOfChatter(UserSetting.getUserId(), chatImage.imageId);
            hideSelectChatImageBubbleView();
        }
    };

    @Override
    public void onChatGroupUpdated() {
        getConversationViewActivity().setActivityTitle(getConversationTitle());
        prepareChatGroupChatters();
        setPresentingVessage();
    }

    private void prepareChatGroupChatters() {
        ArrayList<String> noReadyUsers = new ArrayList<>(10);
        ArrayList<VessageUser> users = new ArrayList<>(10);
        UserService userService = ServicesProvider.getService(UserService.class);
        for (String userId : getChatGroup().getChatters()) {
            VessageUser user = userService.getUserById(userId);
            if (user == null) {
                noReadyUsers.add(userId);
                user = new VessageUser();
                user.userId = userId;
            }
            users.add(user);
        }

        topChattersBoard.clearAllChatters(false);
        bottomChattersBoard.clearAllChatters(false);
        if (getChatGroup().getChatters().length > 3) {
            int cnt = users.size() / 2;
            bottomChattersBoard.addChatters(users.subList(0, cnt).toArray(new VessageUser[0]));
            topChattersBoard.addChatters(users.subList(cnt, users.size()).toArray(new VessageUser[0]));
        } else {
            bottomChattersBoard.addChatters(users.toArray(new VessageUser[0]));
        }
        userService.fetchUserProfilesByUserIds(noReadyUsers);
    }

    @Override
    public void onGroupedChatterUpdated(VessageUser chatter) {
        super.onGroupedChatterUpdated(chatter);
        topChattersBoard.updateChatter(chatter);
        bottomChattersBoard.updateChatter(chatter);
    }

    @Override
    public boolean onFling(int direction, float x, float y) {
        switch (direction) {
            case FlingDerection.LEFT:
            case FlingDerection.DOWN:
                tryShowNextVessage();
                return true;
            case FlingDerection.RIGHT:
            case FlingDerection.UP:
                tryShowPreviousVessage();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onTapUp() {
        if (navigateVessageLocked) {
            hideSelectChatImageBubbleView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return sendMoreTypeVessageManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean paused = false;

    @Override
    public void onPause() {
        super.onPause();
        hideBubbleView(vessageBubbleView);
        inputViewManager.hideImageChatInputView();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    relayoutCurrentVessage();
                }
            }, 666);
        }
        paused = false;
    }

    private void initNotReadVessages() {
        vessagesQueue.clear();
        List<Vessage> vsgs = ServicesProvider.getService(VessageService.class).getNotReadVessage(getConversation().chatterId);
        if (vsgs.size() > 0) {
            vessagesQueue.addAll(vsgs);
            setCurrentIndex(0);
        } else {
            Vessage vsg = ServicesProvider.getService(VessageService.class).getCachedNewestVessage(getConversation().chatterId);
            if (vsg == null) {
                vsg = generateDefaultVessage();
            }
            vessagesQueue.add(vsg);
            setCurrentIndex(0);
        }
    }

    private void setCurrentIndex(int index) {
        this.currentIndex = index;
        if (currentIndex > maxIndex) {
            maxIndex = currentIndex;
        }
    }

    private Vessage generateDefaultVessage() {
        Vessage vsg = new Vessage();
        vsg.isGroup = isGroupChat();
        vsg.sender = getConversation().chatterId;
        vsg.mark = Vessage.MARK_VG_GENERATE_VESSAGE;
        if (vsg.isGroup) {
            String myUserId = UserSetting.getUserId();
            for (String userId : getChatGroup().getChatters()) {
                if (!myUserId.equals(userId)) {
                    vsg.gSender = userId;
                    break;
                }
            }
        }
        vsg.typeId = Vessage.TYPE_FACE_TEXT;
        vsg.vessageId = IDUtil.generateUniqueId();
        vsg.isRead = true;
        vsg.ts = DateHelper.getUnixTimeSpan();
        vsg.body = String.format("{\"textMessage\":\"%s\"}", getRandomTextHelloMessage(vsg.isGroup));
        return vsg;
    }

    private void initBottomButtons() {
        mNewChatButton = (Button) findViewById(R.id.new_chat_btn);
        mNewImageButton = (Button) findViewById(R.id.new_image_btn);
        mNewTextButton = (Button) findViewById(R.id.new_text_btn);

        mNewChatButton.setOnClickListener(onClickMiddleButton);

        mNewChatButton.setOnLongClickListener(onLongClickChatButton);

        mNewImageButton.setOnClickListener(onClickSendImageButton);

        mNewTextButton.setOnClickListener(onClickImageChatButton);

        findViewById(R.id.btn_chat_img_mgr).setOnClickListener(onClickChatImageMgrButton);
    }

    @Override
    public void onVessagesReceived(Collection<Vessage> vessages) {
        super.onVessagesReceived(vessages);
        boolean showNext = false;
        Vessage lastVessage = getCurrentVessage();
        if (currentIndex == vessagesQueue.size() - 1 && lastVessage != null && lastVessage.isMySendingVessage()) {
            showNext = true;
        }
        this.vessagesQueue.addAll(vessages);
        if (showNext) {
            loadNextVessage();
        } else {
            refreshReadingProgress();
        }
    }


    public String getSelectedChatImageId() {
        ChattersBoard.ChatterBoardChatterModel model = getChatterImageViewOfChatterId(UserSetting.getUserId());
        if (model != null && model.chatterItem != null) {
            return model.chatterItem.getItemImage();
        }
        return null;
    }

    public void pushSendingVessage(Vessage vessage) {
        this.vessagesQueue.add(currentIndex + 1, vessage);
        loadNextVessage();
    }

    private void loadNextVessage() {
        if (vessagesQueue.size() == 0) {
            Toast.makeText(getConversationViewActivity(), R.string.no_not_read_vessages, Toast.LENGTH_SHORT).show();
        } else if (vessagesQueue.size() > 1 && vessagesQueue.size() > currentIndex + 1) {
            Vessage oldVessage = getCurrentVessage();
            setCurrentIndex(currentIndex + 1);
            setPresentingVessage(oldVessage);
        } else {
            Toast.makeText(getConversationViewActivity(), R.string.no_more_vessages, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeReadedVessages() {
        if (maxIndex >= 0 && maxIndex < vessagesQueue.size()) {
            List<Vessage> removeVessages = new ArrayList<>();
            for (int i = 0; i <= maxIndex; i++) {
                Vessage vessage = vessagesQueue.get(i);
                if (vessage.isNormalVessage()) {
                    removeVessages.add(vessage);
                }
            }
            vessagesQueue.clear();
            for (Vessage vessage : removeVessages) {
                String fileId = vessage.fileId;
                File oldVideoFile = null;
                if (vessage.typeId == Vessage.TYPE_CHAT_VIDEO) {
                    oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId, ".mp4");
                } else if (vessage.typeId == Vessage.TYPE_IMAGE) {
                    oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId, ".jpg");
                }
                if (oldVideoFile != null) {
                    try {
                        oldVideoFile.delete();
                        Log.d("ConversationView", "Delete Passed Vessage File");
                    } catch (Exception ex) {
                        oldVideoFile.deleteOnExit();
                        Log.d("ConversationView", "Delete Passed Vessage Video File On Exit");
                    }
                }
            }

            ServicesProvider.getService(VessageService.class).removeVessages(removeVessages);
        }
    }

    @Override
    public void onBackKeyPressed() {
        super.onBackKeyPressed();
        sendMoreTypeVessageManager.hideVessageTypesHub();
        inputViewManager.hideImageChatInputView();
    }

    @Override
    public void sending(int progress) {

        ProgressBar sendingProgressBar = (ProgressBar) findViewById(R.id.progress_sending);
        int sendingProgressBarVisible = View.INVISIBLE;
        if (progress == 1) {
            sendingProgressBar.setProgress(1);
            setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.sending_vessage));
            if (inputViewManager.isTyping() == false) {
                sendingProgressBarVisible = View.VISIBLE;
            }
        } else if (progress == -1) {
            setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.send_vessage_failure));
        } else if (progress == 101) {
            setActivityTitle(getConversationTitle());
        } else if (progress == 100) {
            setActivityTitle(LocalizedStringHelper.getLocalizedString(R.string.vessage_sended));
        } else {
            sendingProgressBar.setProgress(progress);
            if (inputViewManager.isTyping() == false) {
                sendingProgressBarVisible = View.VISIBLE;
            }
        }

        sendingProgressBar.setVisibility(sendingProgressBarVisible);
        if (inputViewManager != null && inputViewManager.isTyping()) {
            inputViewManager.sending(progress);
        }
    }

    private void setActivityTitle(String title) {
        getConversationViewActivity().setActivityTitle(title);
    }

    private View.OnClickListener onClickChatImageMgrButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    ChatImageManageActivity.show(getConversationViewActivity(), 1);
                }
            });
        }
    };

    private View.OnClickListener onClickImageChatButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    inputViewManager.addKeyboardNotification();
                    inputViewManager.showImageChatInputView();
                }
            });

        }
    };

    private View.OnClickListener onClickSendImageButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    sendMoreTypeVessageManager.showVessageTypesHub();
                }
            });

        }
    };

    public void tryShowPreviousVessage() {
        if (navigateVessageLocked) {
            return;
        }
        if (vessagesQueue.size() > 0 && currentIndex > 0) {
            setCurrentIndex(currentIndex - 1);
            setPresentingVessage();
        } else {
            Toast.makeText(getConversationViewActivity(), R.string.no_previous_vessages, Toast.LENGTH_SHORT).show();
        }
    }

    public void tryShowNextVessage() {
        if (navigateVessageLocked) {
            return;
        }
        if (getCurrentVessage() == null || vessagesQueue.size() <= 1) {
            Toast.makeText(getConversationViewActivity(), R.string.no_more_vessages, Toast.LENGTH_SHORT).show();
            return;
        }
        if (getCurrentVessage().isRead) {
            loadNextVessage();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getConversationViewActivity())
                .setTitle(R.string.ask_jump_vessage)
                .setMessage(R.string.jump_vessage_will_delete)
                .setPositiveButton(R.string.jump, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MobclickAgent.onEvent(getConversationViewActivity(), "Vege_JumpVessage");
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

    private void setPresentingVessage() {
        setPresentingVessage(null);
    }

    private void setPresentingVessage(Vessage oldVessage) {
        Vessage currentVessage = getCurrentVessage();
        if (currentVessage != null) {
            showBubbleVessage(oldVessage, currentVessage, null);
        }
        refreshReadingProgress();
    }

    private void refreshReadingProgress() {
        int progress = (int) (100.0 * (currentIndex + 1) / vessagesQueue.size());
        progressReading.setProgress(progress);
        if (progress == 100 || progress == 0) {
            progressReading.setVisibility(View.INVISIBLE);
        } else {
            progressReading.setVisibility(View.VISIBLE);
        }
    }

    private View.OnLongClickListener onLongClickChatButton = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.new_chat_btn) {
                sendMoreTypeVessageManager.showVessageTypesHub();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener onClickMiddleButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AnimationHelper.startAnimation(getConversationViewActivity(), v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    tryShowRecordViews();
                }
            });
        }
    };

    private void tryShowRecordViews() {

        if (isGroupChat() && getConversationViewActivity().isQuitedChatGroup()) {
            Toast.makeText(getConversationViewActivity(), R.string.u_not_in_group, Toast.LENGTH_SHORT).show();
            return;
        }

        if (ServicesProvider.getService(UserService.class).isMyProfileHaveChatBackground()) {
            Intent intent = new Intent(getConversationViewActivity(), ConversationRecordChatVideoActivity.class);
            intent.putExtra("chatterId", getConversation().chatterId);
            getConversationViewActivity().startActivityForResult(intent, ActivityRequestCode.RECORD_CHAT_VIDEO_REQUEST_ID);
        } else {
            askUploadChatBcg();
        }
    }

    public void askUploadChatBcg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getConversationViewActivity());
        builder.setTitle(R.string.need_upload_chat_bcg_title);
        builder.setMessage(R.string.need_upload_chat_bcg_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getConversationViewActivity(), UpdateChatImageActivity.class);
                getConversationViewActivity().startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setPresentingVessage();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SelectChatImageBubbleHandler.instance.releaseHandler();
        sendMoreTypeVessageManager.onDestory();
        inputViewManager.onDestory();
        removeReadedVessages();
    }

    private BubbleVessageContainer vessageBubbleView;

    private BubbleVessageContainer selectChatImageBubbleView;

    private BubbleVessageHandler vessageBubbleHandler;

    private ChattersBoard.ChatterBoardChatterModel getChatterImageViewOfChatterId(String sender) {
        ChattersBoard[] boards = new ChattersBoard[]{topChattersBoard, bottomChattersBoard};
        ChattersBoard.ChatterBoardChatterModel model = getChatterModelFromChattersBoards(boards, sender);
        if (model != null) {
            return model;
        } else {
            for (String userId : getChatGroup().getChatters()) {
                if (userId.equals(sender)) {
                    return null;
                }
            }
            getChatGroup().addChatter(sender);
            prepareChatGroupChatters();
            return getChatterModelFromChattersBoards(boards, sender);
        }
    }

    private ChattersBoard.ChatterBoardChatterModel getChatterModelFromChattersBoards(ChattersBoard[] boards, String sender) {
        for (ChattersBoard board : boards) {
            int index = board.indexOfChatter(sender);
            if (index >= 0) {
                ChattersBoard.ChatterBoardChatterModel result = board.getBoardChatterModel(index);
                return result;
            }
        }
        return null;
    }

    public void hideBubbleView(BubbleVessageContainer bubbleView) {
        bubbleView.setAlpha(0);
    }

    public void showBubbleView(final BubbleVessageContainer bubbleView) {

        AnimationHelper.startAnimation(getConversationViewActivity(), bubbleView, R.anim.ease_in, new AnimationHelper.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setDuration(160);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bubbleView.setAlpha(1);
            }
        });
    }

    private boolean showBubbleVessage(Vessage oldVessage, Vessage vessage, Vessage nextVessage) {
        if (vessageContentContainer.getWidth() == 0) {
            return false;
        }
        if (vessageBubbleHandler != null) {
            vessageBubbleHandler.onUnloadVessage(getConversationViewActivity());
        }
        BubbleVessageHandler handler = BubbleVessageHandlerManager.getBubbleVessageHandler(vessage.typeId);

        String sender = vessage.getVessageRealSenderId();

        Log.d(TAG, "Vessage Sender:" + sender);

        ChattersBoard.ChatterBoardChatterModel matchItem = getChatterImageViewOfChatterId(sender);
        if (matchItem != null) {

            if (vessageBubbleView == null) {
                vessageBubbleView = new BubbleVessageContainer(getConversationViewActivity());
                this.vessageContentContainer.addView(vessageBubbleView);
            }
            try {
                String faceId = vessage.getBodyJsonObject().getString("faceId");
                matchItem.chattersBoard.setImageOfChatter(sender, faceId);
            } catch (Exception e) {

            }

            vessageBubbleView.setFillColor(vessage.isMySendingVessage() ? bubbleColorMyVessageColor : bubbleColorNormalVessageColor);
            View contentView = handler.getContentView(getConversationViewActivity(), vessage);

            layoutVessageContentView(vessageBubbleView, matchItem, vessage, handler, contentView);

            vessageBubbleView.setContentView(contentView);

            handler.presentContent(getConversationViewActivity(), oldVessage, vessage, contentView);
            vessageBubbleHandler = handler;
            vessageBubbleView.forceLayout();
            return true;
        }
        return false;
    }

    private void layoutVessageContentView(BubbleVessageContainer bubbleView, ChattersBoard.ChatterBoardChatterModel matchItem, Vessage vessage, BubbleVessageHandler handler, View contentView) {

        int paddingStartEnd = DensityUtil.dip2px(getConversationViewActivity(), 10);
        int containerMinX = paddingStartEnd;
        int containerMaxX = vessageContentContainer.getWidth() - paddingStartEnd;

        Rect chatterImageRect = getChatterImageViewRectOfVessageContentContainer(matchItem.view, matchItem.chattersBoard);

        Log.d(TAG, "chatterImageRect:" + chatterImageRect);

        float rectCenterX = chatterImageRect.centerX();

        Log.d(TAG, "rectCenterX:" + rectCenterX);


        BTSize contentSize = handler.getContentViewSize(getConversationViewActivity(), vessage, getBubbleContentMaxSize(), contentView);

        Log.d(TAG, "contentSize:" + contentSize.toString());

        BTSize containerSize = bubbleView.sizeOfContentSize(contentSize, BezierBubbleView.BezierBubbleDirection.Up);
        Log.d(TAG, "containerSize:" + containerSize.toString());

        float containerX = rectCenterX - containerSize.width / 2;

        if (rectCenterX + containerSize.width / 2 > containerMaxX) {
            containerX -= (rectCenterX + containerSize.width / 2 - containerMaxX);
        } else if (rectCenterX - containerSize.width / 2 < containerMinX) {
            containerX += containerMinX - (rectCenterX - containerSize.width / 2);
        }

        int spaceBubbleAndChatterImage = DensityUtil.dip2px(getConversationViewActivity(), 3);

        float containerY = chatterImageRect.top - spaceBubbleAndChatterImage - containerSize.height;
        BezierBubbleView.BezierBubbleDirection d = BezierBubbleView.BezierBubbleDirection.Up;

        float startRatio = (rectCenterX - containerX) / containerSize.width;

        Log.d(TAG, "startRatio:" + startRatio);

        if (matchItem.chattersBoard == topChattersBoard) {
            d = BezierBubbleView.BezierBubbleDirection.Down;
            containerY = chatterImageRect.top + chatterImageRect.height() + spaceBubbleAndChatterImage;
        }

        Log.d(TAG, "containerXY:" + containerX + "," + containerY);

        int containerW = (int) containerSize.width;
        int containerH = (int) containerSize.height;
        bubbleView.getLayoutParams().width = containerW;
        bubbleView.getLayoutParams().height = containerH;

        bubbleView.setX(containerX);
        bubbleView.setY(containerY);

        bubbleView.setDirection(d);
        bubbleView.setStartRatio(startRatio);

        showBubbleView(bubbleView);
    }

    public void relayoutCurrentVessage() {
        Vessage vessage = getCurrentVessage();
        if (vessage != null && vessageBubbleHandler != null && vessageBubbleView.getContentView() != null) {
            String sender = vessage.getVessageRealSenderId();
            ChattersBoard.ChatterBoardChatterModel matchItem = getChatterImageViewOfChatterId(sender);
            layoutVessageContentView(vessageBubbleView, matchItem, vessage, vessageBubbleHandler, vessageBubbleView.getContentView());
            forceLayoutVessageBubbleView();
        }
    }

    private void forceLayoutVessageBubbleView() {
        vessageBubbleView.requestLayout();
        vessageBubbleView.invalidate(0, 0, 0, 0);
    }

    private Rect getChatterImageViewRectOfVessageContentContainer(View view, ChattersBoard chattersBoard) {
        int x = (int) (chattersBoard.getX() + view.getX());
        int y = (int) (chattersBoard.getY() + view.getY());
        return new Rect(x, y, x + view.getWidth(), y + view.getHeight());
    }

    BTSize getBubbleContentMaxSize() {
        int bubbleViewPadding = vessageBubbleView == null ? 0 : (int) vessageBubbleView.getContentViewPadding();
        int padding = DensityUtil.dip2px(getConversationViewActivity(), 6) + bubbleViewPadding;
        int w = vessageContentContainer.getWidth() - padding;
        int h = bottomChattersBoard.getTop() - topChattersBoard.getBottom() - padding;
        BTSize size = w > 0 && h > 0 ? new BTSize(w, h) : BTSize.ZERO;
        Log.d(TAG, "Max Content Size:" + size.toString());
        return size;
    }

    public void hideVessageBubbleView() {
        hideBubbleView(vessageBubbleView);
    }

    private MessageInputViewManager.SendImageChatMessageManagerDelegate sendImageChatMessageManagerDelegate = new MessageInputViewManager.SendImageChatMessageManagerDelegate() {
        private int cachedBottomChatterBoardHeight = -1;
        private ChattersBoard.ChatterItem[] cachedTopChatterBoardItems;

        @Override
        public boolean onFling(int direction, float velocityX, float velocityY) {
            switch (direction) {
                case FlingDerection.LEFT:
                case FlingDerection.DOWN:
                    tryShowNextVessage();
                    return true;
                case FlingDerection.RIGHT:
                case FlingDerection.UP:
                    tryShowPreviousVessage();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onTapUp() {
            inputViewManager.hideImageChatInputView();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onSoftKeyboardOpened(MessageInputViewManager sender, int keyboardHeightInPx) {
            if (cachedBottomChatterBoardHeight < 0) {
                cachedBottomChatterBoardHeight = getBottomChattersBoard().getLayoutParams().height;
            }
            getConversationViewActivity().getSupportActionBar().setShowHideAnimationEnabled(true);
            getConversationViewActivity().getSupportActionBar().hide();
            getBottomChattersBoard().getLayoutParams().height = (int) (cachedBottomChatterBoardHeight * 0.8);
            cachedTopChatterBoardItems = getTopChattersBoard().clearAllChatters(true);
            getBottomChattersBoard().addChatters(cachedTopChatterBoardItems);
            updateVessageBubble(666);
        }

        @Override
        public void onSoftKeyboardClosed(MessageInputViewManager sender) {

            getConversationViewActivity().getSupportActionBar().setShowHideAnimationEnabled(true);
            getConversationViewActivity().getSupportActionBar().show();
            getBottomChattersBoard().getLayoutParams().height = cachedBottomChatterBoardHeight;
            getBottomChattersBoard().removeChatters(cachedTopChatterBoardItems);
            getTopChattersBoard().addChatters(cachedTopChatterBoardItems);
            updateVessageBubble(333);
        }

        private void updateVessageBubble(int hideDelay) {
            hideVessageBubbleView();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    relayoutCurrentVessage();
                }
            }, hideDelay);
        }
    };
    */
}
