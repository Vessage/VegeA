package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.HashMap;
import java.util.LinkedList;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.activities.sns.model.SNSPostComment;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.userprofile.OpenConversationDelegate;
import cn.bahamut.vessage.userprofile.UserProfileView;

public class SNSPostCommentActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private String postId;
    private PostCommentAdapter adapter;
    private UserService userService;
    private InputViewManager inputViewManager;
    private Button newCommentButton;
    private String poster;
    private RecyclerView commentListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_comment);
        getSupportActionBar().setTitle(R.string.sns_post_comment);
        inputViewManager = new InputViewManager(this, R.id.input_view);
        inputViewManager.hideInputView();
        inputViewManager.setListener(inputViewManagerListener);
        userService = ServicesProvider.getService(UserService.class);
        postId = getIntent().getStringExtra("postId");
        poster = getIntent().getStringExtra("poster");
        newCommentButton = (Button) findViewById(R.id.new_cmt_btn);
        newCommentButton.setOnClickListener(onClickNewComment);
        commentListView = (RecyclerView) findViewById(R.id.comment_list_view);
        commentListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostCommentAdapter(this);
        commentListView.setAdapter(adapter);
        int prepareCount = getIntent().getIntExtra("prepareCount",DEFAULT_PAGE_COUNT);
        adapter.loadComments(prepareCount);
        commentListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItemCount = layoutManager.getItemCount();

                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (totalItemCount < (lastVisibleItem + 3)) {
                    adapter.loadComments(DEFAULT_PAGE_COUNT);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inputViewManager.onDestroy();
        userService = null;
    }

    private View.OnClickListener onClickNewComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            newCommentAtUser(null);
        }
    };

    private class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.ViewHolder> {
        LinkedList<SNSPostComment> postComments = null;
        Activity context;
        private boolean noMoreData = false;
        private boolean loadingMore = false;

        public void pushNewSendedComment(SNSPostComment comment) {
            postComments.add(comment);
            notifyDataSetChanged();
        }

        public void loadComments(final int pageCount) {
            if (noMoreData || loadingMore) {
                if (noMoreData) {
                    commentListView.clearOnScrollListeners();
                    Toast.makeText(context, R.string.no_more_cmt_tips, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            long ts = 0;
            if (postComments.size() > 0) {
                ts = postComments.getLast().ts;
            }
            loadingMore = true;
            SNSPostManager.getInstance().getPostComment(postId, ts, pageCount, new SNSPostManager.GetPostCommentCallback() {
                @Override
                public void onGetPostComment(SNSPostComment[] comments) {
                    loadingMore = false;
                    if (comments != null) {
                        noMoreData = comments.length < pageCount;
                        for (SNSPostComment comment : comments) {
                            postComments.add(comment);
                        }
                        if (comments.length > 0) {
                            notifyDataSetChanged();
                        }
                        if (postComments.size() == 0 && poster.equals(UserSetting.getUserId())) {
                            newCommentAtUser();
                        }
                    } else {
                        Toast.makeText(SNSPostCommentActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        public PostCommentAdapter(Activity context) {
            postComments = new LinkedList<>();
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = context.getLayoutInflater().inflate(R.layout.sns_comment_item, null);
            return new ViewHolder(v);
        }

        private class OnClickItemViewsListener implements View.OnClickListener {
            int pos;
            PostCommentAdapter.ViewHolder viewHolder;

            public OnClickItemViewsListener(PostCommentAdapter.ViewHolder holder, int position) {
                this.pos = position;
                this.viewHolder = holder;
            }

            @Override
            public void onClick(View v) {
                onClickItemView(viewHolder, v, pos);
            }
        }

        private class OnLongClickItemViewListener implements View.OnLongClickListener {
            int pos;
            PostCommentAdapter.ViewHolder viewHolder;

            public OnLongClickItemViewListener(PostCommentAdapter.ViewHolder holder, int position) {
                this.pos = position;
                this.viewHolder = holder;
            }

            @Override
            public boolean onLongClick(View v) {
                onLongClickItemView(viewHolder, v, pos);
                return false;
            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            SNSPostComment comment = postComments.get(position);

            if (comment.st >= 0) {
                holder.contentTextView.setText(comment.cmt);
            } else {
                holder.contentTextView.setText(R.string.cmt_removed);
            }

            String dateString = AppUtil.dateToFriendlyString(SNSPostCommentActivity.this, DateHelper.getDateFromUnixTimeSpace(comment.ts));
            String atNick = StringHelper.isStringNullOrWhiteSpace(comment.atNick) ? String.format("%s", dateString) : String.format("@%s %s", comment.atNick, dateString);
            holder.extraInfoTextView.setText(atNick);

            String nick = userService.getUserNotedName(comment.pster);
            if (StringHelper.isStringNullOrWhiteSpace(nick)) {
                nick = comment.psterNk;
            }
            holder.senderInfoTextView.setText(String.format("By %s", nick));

            View.OnClickListener onClickItemViews = comment.st >= 0 ? new OnClickItemViewsListener(holder, position) : null;
            holder.contentTextView.setOnClickListener(onClickItemViews);
            holder.itemView.setOnClickListener(onClickItemViews);
            holder.senderInfoTextView.setOnClickListener(onClickItemViews);
            holder.extraInfoTextView.setOnClickListener(onClickItemViews);

            View.OnLongClickListener onLongClickListener = comment.st >= 0 ? new OnLongClickItemViewListener(holder, position) : null;
            holder.itemView.setOnLongClickListener(onLongClickListener);
            holder.contentTextView.setOnLongClickListener(onLongClickListener);
            holder.senderInfoTextView.setOnLongClickListener(onLongClickListener);
            holder.extraInfoTextView.setOnLongClickListener(onLongClickListener);
        }

        @Override
        public int getItemCount() {
            return postComments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView extraInfoTextView;
            private TextView senderInfoTextView;
            private TextView contentTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                contentTextView = (TextView) itemView.findViewById(R.id.content_text_view);
                extraInfoTextView = (TextView) itemView.findViewById(R.id.subline_extra_info);
                senderInfoTextView = (TextView) itemView.findViewById(R.id.subline_sender_info);
                contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        private void onClickItemView(PostCommentAdapter.ViewHolder viewHolder, View v, int pos) {

            SNSPostComment cmt = postComments.get(pos);
            if (viewHolder.itemView == v) {
                newCommentAtUser(cmt);
                return;
            }
            switch (v.getId()) {
                case R.id.subline_sender_info:
                    showUserProfileView(cmt.pster);
                    break;
                case R.id.subline_extra_info:
                case R.id.content_text_view:
                case R.id.item_view:
                    newCommentAtUser(cmt);
                    break;
                default:
                    break;
            }
        }

        private void onLongClickItemView(final PostCommentAdapter.ViewHolder viewHolder, View v, final int pos) {
            SNSPostComment cmt = postComments.get(pos);
            String userId = UserSetting.getUserId();
            if (cmt.st >= 0 && (userId.equals(cmt.pster) || userId.equals(poster))) {
                PopupMenu popupMenu = new PopupMenu(SNSPostCommentActivity.this, viewHolder.itemView);
                popupMenu.getMenu().add(0, 0, 1, R.string.remove_sns_post_cmt);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getOrder()) {
                            case 1:
                                SNSPostComment cmt = postComments.get(pos);
                                removeComment(cmt, pos, viewHolder);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        }

        private void removeComment(final SNSPostComment postComment, int pos, final ViewHolder viewHolder) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SNSPostCommentActivity.this)
                    .setTitle(R.string.ask_remove_sns_post_cmt)
                    .setMessage(R.string.remove_sns_post_cmt_msg)
                    .setNegativeButton(R.string.no, null)
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(SNSPostCommentActivity.this);
                            SNSPostManager.getInstance().deleteSNSPostComment(postComment.postId, postComment.id, UserSetting.getUserId().equals(postComment.pster), new SNSPostManager.RequestSuccessCallback() {
                                @Override
                                public void onCompleted(Boolean isOk) {
                                    hud.dismiss();
                                    if (isOk) {
                                        postComment.st = -1;
                                        viewHolder.contentTextView.setText(R.string.cmt_removed);
                                    } else {
                                        Toast.makeText(SNSPostCommentActivity.this, R.string.delete_sns_cmt_error, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
            builder.show();
        }
    }

    private void showUserProfileView(String posterId) {
        UserService userService = ServicesProvider.getService(UserService.class);
        final OpenConversationDelegate delegate = new OpenConversationDelegate();
        delegate.conversationExtraInfo = new HashMap<>();
        delegate.conversationExtraInfo.put("activityId", SNSPostManager.ACTIVITY_ID);
        VessageUser poster = userService.getUserById(posterId);
        if (poster == null) {
            userService.fetchUserByUserId(posterId, new UserService.UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if (user != null) {
                        UserProfileView profileView = new UserProfileView(SNSPostCommentActivity.this, user);
                        profileView.delegate = delegate;
                        profileView.show();
                    } else {
                        Toast.makeText(SNSPostCommentActivity.this, R.string.no_such_user, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            UserProfileView profileView = new UserProfileView(SNSPostCommentActivity.this, poster);
            profileView.delegate = delegate;
            profileView.show();
        }
    }

    private InputViewManager.InputViewManagerListener inputViewManagerListener = new InputViewManager.InputViewManagerListenerAdapter() {

        @Override
        public void onKeyboardOpened(InputViewManager manager) {
            super.onKeyboardOpened(manager);
            newCommentButton.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onKeyboardClosed(InputViewManager manager) {
            super.onKeyboardClosed(manager);
            manager.hideInputView();
            newCommentButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSendButtonClicked(InputViewManager manager, EditText editText, Object data) {
            super.onSendButtonClicked(manager, editText, data);
            String content = editText.getText().toString();
            if (StringHelper.isStringNullOrWhiteSpace(content)){
                Toast.makeText(SNSPostCommentActivity.this,R.string.sns_white_comment_tips,Toast.LENGTH_SHORT).show();
            }else {
                ServicesProvider.getService(ConversationService.class).expireConversation(poster);
                String senderNick = SNSPostManager.getInstance().getUserProfile().nickName;
                String atUserId = null;
                String atUserNick = null;
                if (data != null){
                    SNSPostComment cmt = (SNSPostComment)data;
                    atUserId = cmt.pster;
                    atUserNick = userService.getUserNotedName(atUserId);
                    if (StringHelper.isStringNullOrWhiteSpace(atUserNick)){
                        atUserNick = cmt.psterNk;
                    }
                }

                final SNSPostComment newComment = new SNSPostComment();
                newComment.psterNk = senderNick;
                newComment.pster = UserSetting.getUserId();
                newComment.ts = DateHelper.getUnixTimeSpan();
                newComment.atNick = atUserNick;
                newComment.cmt = content;

                final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(SNSPostCommentActivity.this);
                SNSPostManager.getInstance().newPostComment(postId, content, senderNick, atUserId, atUserNick, new SNSPostManager.PostNewCommentCallback() {
                    @Override
                    public void onPostNewComment(boolean posted, String postedCmtId, String msg) {
                        hud.dismiss();
                        if (posted){
                            inputViewManager.clearEditingText();
                            inputViewManager.hideKeyboard();
                            newComment.id = postedCmtId;
                            adapter.pushNewSendedComment(newComment);
                        }else {
                            if (StringHelper.isStringNullOrWhiteSpace(msg)){
                                ProgressHUDHelper.showHud(SNSPostCommentActivity.this, R.string.network_error, R.drawable.cross_mark, true);
                            }else {
                                ProgressHUDHelper.showHud(SNSPostCommentActivity.this, msg, R.drawable.cross_mark, true);
                            }
                        }
                    }
                });
            }
        }
    };

    private void newCommentAtUser() {
        newCommentAtUser(null);
    }

    private void newCommentAtUser(SNSPostComment comment) {

        inputViewManager.showInputView();
        if (comment == null) {
            inputViewManager.openKeyboard();
        } else {
            String atNick = userService.getUserNotedName(comment.pster);
            if (StringHelper.isStringNullOrWhiteSpace(atNick)){
                atNick = comment.psterNk;
            }
            inputViewManager.openKeyboard("@" + atNick, comment);
        }
    }

    public static void showPostCommentActivity(Context context, SNSPost post) {
        showPostCommentActivity(context, post, 0);
    }

    public static void showPostCommentActivity(Context context, SNSPost post, int prepareCount){
        Intent intent = new Intent(context,SNSPostCommentActivity.class);
        intent.putExtra("postId",post.pid);
        intent.putExtra("poster",post.usrId);
        if (prepareCount > 0) {
            intent.putExtra("prepareCount", prepareCount);
        }
        context.startActivity(intent);
    }
}
