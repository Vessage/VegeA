package cn.bahamut.vessage.activities.littlepaper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.UsersListActivity;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperManager;
import cn.bahamut.vessage.activities.littlepaper.model.LittlePaperMessage;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class LittlePaperDetailActivity extends Activity {

    private static final int SELECT_USER_REQUEST_CODE = 1;
    private LittlePaperMessage paperMessage;
    private String myUserId;
    private View postmenButton;
    private View openPaperButton;
    private View postPaperButton;
    private Button tipsButton;
    private TextView paperContentTextView;
    private TextView receiverInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ltp_activity_paper_detail);

        ImageView backgroundImageView = (ImageView)findViewById(R.id.bcg_img_view);
        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.little_paper_white));
        backgroundImageView.setImageBitmap(bitmap);

        String paperId = getIntent().getStringExtra("paperId");
        if(StringHelper.notStringNullOrWhiteSpace(paperId)){
            paperMessage = LittlePaperManager.getInstance().getPaperMessageByPaperId(paperId);
        }
        if(paperMessage == null){
            Toast.makeText(this,R.string.little_paper_not_found,Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
        postmenButton = findViewById(R.id.postmen);
        postmenButton.setOnClickListener(onClickPostmen);
        openPaperButton = findViewById(R.id.open_paper);
        postPaperButton = findViewById(R.id.post_paper);
        openPaperButton.setOnClickListener(onClickOpenPaper);
        postPaperButton.setOnClickListener(onClickPostPaper);
        tipsButton = (Button) findViewById(R.id.tips_button);
        tipsButton.setOnClickListener(onClickTipsButton);
        paperContentTextView = (TextView)findViewById(R.id.little_paper_content);
        receiverInfoTextView = (TextView)findViewById(R.id.receiver_info);

        refreshPaper();
    }

    private View.OnClickListener onClickTipsButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String userId = null;
            if(paperMessage.isMySended(myUserId) && paperMessage.isOpened){
                userId = paperMessage.receiver;
            }else if(paperMessage.isMyOpened(myUserId)){
                userId = paperMessage.sender;
            }
            if (userId != null) {
                UserService userService = ServicesProvider.getService(UserService.class);
                VessageUser user = userService.getUserById(userId);
                if (user == null) {
                    final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperDetailActivity.this);
                    userService.fetchUserByUserId(userId, new UserService.UserUpdatedCallback() {
                        @Override
                        public void updated(VessageUser user) {
                            hud.dismiss();
                            if (user != null) {
                                Map<String, Object> extraInfo = new HashMap<>();
                                extraInfo.put("activityId", LittlePaperManager.LITTLE_PAPER_ACTIVITY_ID);
                                Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(user.userId, extraInfo);
                                ConversationViewActivity.openConversationView(LittlePaperDetailActivity.this, conversation);
                            } else {
                                Toast.makeText(LittlePaperDetailActivity.this, R.string.user_data_not_ready, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Map<String, Object> extraInfo = new HashMap<>();
                    extraInfo.put("activityId", LittlePaperManager.LITTLE_PAPER_ACTIVITY_ID);
                    Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(user.userId, extraInfo);
                    ConversationViewActivity.openConversationView(LittlePaperDetailActivity.this, conversation);
                }
            }

        }
    };

    private View.OnClickListener onClickPostPaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String title =  LocalizedStringHelper.getLocalizedString(R.string.little_paper_select_receiver);
            new UsersListActivity.ShowSelectUserActivityBuilder(LittlePaperDetailActivity.this)
                    .setConversationUserIdList()
                    .setCanSelectMobile(true)
                    .setCanSelectNearUser(true)
                    .setTitle(title)
                    .showActivity(SELECT_USER_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_USER_REQUEST_CODE && requestCode == resultCode){
            List<String> userIds = data.getStringArrayListExtra(UsersListActivity.SELECTED_USER_IDS_ARRAY_KEY);
            postPaperToNextReceiver(userIds.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void postPaperToNextReceiver(String nextReceiver) {
        if(StringHelper.notNullOrEmpty(paperMessage.postmenString) && paperMessage.postmenString.contains(nextReceiver)){
            Toast.makeText(this,R.string.little_paper_posted_by_user,Toast.LENGTH_LONG).show();
            return;
        }
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperDetailActivity.this);
        LittlePaperManager.getInstance().postPaperToNextUser(paperMessage.paperId, nextReceiver, false, new LittlePaperManager.OnPostPaperToNextUserCallback() {
            @Override
            public void onPostPaperToNextUser(boolean suc, String message) {
                hud.dismiss();
                if(suc){
                    MobclickAgent.onEvent(LittlePaperDetailActivity.this,"LittlePaper_PostNext");
                    ProgressHUDHelper.showHud(LittlePaperDetailActivity.this, R.string.little_paper_send_suc, R.drawable.check_mark, true, new ProgressHUDHelper.OnDismiss() {
                        @Override
                        public void onHudDismiss() {
                            finish();
                        }
                    });
                }else {
                    ProgressHUDHelper.showHud(LittlePaperDetailActivity.this, message, R.drawable.cross_mark, true);
                }
            }
        });
    }

    private View.OnClickListener onClickOpenPaper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LittlePaperDetailActivity.this)
                    .setTitle(R.string.little_paper_ask_open_paper)
                    .setMessage(R.string.little_paper_ask_open_paper_message)
                    .setPositiveButton(R.string.little_paper_send_ask, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendAskReadMessageToSender();
                        }
                    });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    };

    private void sendAskReadMessageToSender() {
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperDetailActivity.this);
        LittlePaperManager.getInstance().askReadPaper(paperMessage.paperId, new LittlePaperManager.LittlePaperManagerOperateCallback() {
            @Override
            public void onCallback(boolean isOk, String errorMessage) {
                hud.dismiss();
                if(isOk){
                    Toast.makeText(LittlePaperDetailActivity.this,R.string.little_paper_ask_msg_sended,Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(LittlePaperDetailActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openAcceptLessPaper() {
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(LittlePaperDetailActivity.this);
        LittlePaperManager.getInstance().openAcceptlessPaperMessage(paperMessage.paperId, new LittlePaperManager.OnOpenPaperMessageCallback() {
            @Override
            public void onOpenPaperMessage(LittlePaperMessage openedMessage, String error) {
                hud.dismiss();
                if(openedMessage != null){
                    LittlePaperDetailActivity.this.paperMessage = openedMessage;
                    refreshPaper();
                    MobclickAgent.onEvent(LittlePaperDetailActivity.this,"LittlePaper_OpenPaper");
                }else {
                    Toast.makeText(LittlePaperDetailActivity.this, error,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private View.OnClickListener onClickPostmen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(StringHelper.notNullOrEmpty(paperMessage.postmenString)) {
                String[] userIds = paperMessage.postmenString.split(";");
                ArrayList<String> userIdList = new ArrayList<>(userIds.length);
                for (String userId : userIds) {
                    userIdList.add(userId);
                }
                if (userIdList.size() < 3) {
                    userIdList = new ArrayList<>();
                }
                new UsersListActivity
                        .ShowUserListActivityBuilder(LittlePaperDetailActivity.this)
                        .setRemoveMyProfile(true)
                        .setTitle(LocalizedStringHelper.getLocalizedString(R.string.little_paper_posters))
                        .setUserIdList(userIdList)
                        .showActivity();
            }
        }
    };

    private void refreshPaper() {
        receiverInfoTextView.setText(paperMessage.receiverInfo);
        if(paperMessage.isMySended(myUserId)){
            paperContentTextView.setVisibility(View.VISIBLE);
            paperContentTextView.setText(paperMessage.message);
            openPaperButton.setVisibility(View.INVISIBLE);
            postPaperButton.setVisibility(View.INVISIBLE);
            tipsButton.setVisibility(View.VISIBLE);
            if(paperMessage.isOpened){
                tipsButton.setText(R.string.little_paper_opened_by_receiver);
            }else {
                tipsButton.setText(R.string.little_paper_posting);
            }
        }else if(paperMessage.isMyOpened(myUserId)){
            paperContentTextView.setVisibility(View.VISIBLE);
            paperContentTextView.setText(paperMessage.message);
            openPaperButton.setVisibility(View.INVISIBLE);
            postPaperButton.setVisibility(View.INVISIBLE);
            tipsButton.setVisibility(View.VISIBLE);
            tipsButton.setText(R.string.little_paper_you_opened_paper);
        }else if(paperMessage.isMyPosted(myUserId)){

            paperContentTextView.setVisibility(View.INVISIBLE);
            openPaperButton.setVisibility(View.INVISIBLE);
            postPaperButton.setVisibility(View.INVISIBLE);
            tipsButton.setVisibility(View.VISIBLE);
            tipsButton.setText(R.string.little_paper_you_posted_paper);

        }else if(paperMessage.isMyReceived(myUserId)){
            paperContentTextView.setVisibility(View.INVISIBLE);
            openPaperButton.setVisibility(View.VISIBLE);
            postPaperButton.setVisibility(View.VISIBLE);
            tipsButton.setVisibility(View.INVISIBLE);
        }
        if(StringHelper.isStringNullOrWhiteSpace(paperMessage.postmenString)){
            postmenButton.setVisibility(View.INVISIBLE);
        }else {
            postmenButton.setVisibility(View.VISIBLE);
        }
    }

    public static void showLittlePaperDetailActivity(Activity context, String paperId) {
        Intent intent = new Intent(context,LittlePaperDetailActivity.class);
        intent.putExtra("paperId",paperId);
        context.startActivity(intent);
    }
}
