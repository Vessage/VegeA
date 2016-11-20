package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.LinkedList;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.activities.sns.model.SNSPostComment;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.user.UserService;

public class SNSPostCommentActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private String postId;
    private PostCommentAdapter adapter;
    private UserService userService;
    private InputViewManager inputViewManager;
    private Button newCommentButton;
    private String poster;

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
        RecyclerView commentListView = (RecyclerView) findViewById(R.id.comment_list_view);
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

    private class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.ViewHolder>{
        LinkedList<SNSPostComment> postComments = null;
        Activity context;
        private boolean noMoreData = false;
        private boolean loadingMore = false;

        public void pushNewSendedComment(SNSPostComment comment){
            postComments.add(comment);
            notifyDataSetChanged();
        }

        public void loadComments(final int pageCount){
            if (noMoreData || loadingMore){
                if (noMoreData){
                    Toast.makeText(context,R.string.no_more_cmt_tips,Toast.LENGTH_SHORT).show();
                }
                return;
            }
            long ts = 0;
            if (postComments.size() > 0){
                ts = postComments.getLast().ts;
            }
            loadingMore = true;
            SNSPostManager.getInstance().getPostComment(postId, ts, pageCount,new SNSPostManager.GetPostCommentCallback() {
                @Override
                public void onGetPostComment(SNSPostComment[] comments) {
                    loadingMore = false;
                    if (comments != null){
                        for (SNSPostComment comment : comments) {
                            postComments.add(comment);
                        }
                        noMoreData = comments.length < pageCount;
                        if (comments.length > 0){
                            notifyDataSetChanged();
                        }
                        if (postComments.size() == 0 && poster.equals(UserSetting.getUserId())){
                            newCommentAtUser();
                        }
                    }else {
                        Toast.makeText(SNSPostCommentActivity.this,R.string.network_error,Toast.LENGTH_SHORT).show();
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
            View v = context.getLayoutInflater().inflate(R.layout.sns_comment_item,null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            SNSPostComment comment = postComments.get(position);
            View.OnClickListener onClickItemViews = new View.OnClickListener() {
                int pos = position;
                PostCommentAdapter.ViewHolder viewHolder = holder;
                @Override
                public void onClick(View v) {
                    onClickItemView(viewHolder,v,pos);
                }
            };
            holder.contentTextView.setText(comment.cmt);

            String dateString = AppUtil.dateToFriendlyString(SNSPostCommentActivity.this,DateHelper.getDateFromUnixTimeSpace(comment.ts));
            String atNick = StringHelper.isStringNullOrWhiteSpace(comment.atNick) ? String.format("%s",dateString) : String.format("@%s %s", comment.atNick,dateString);
            holder.extraInfoTextView.setText(atNick);

            String nick = userService.getUserNotedName(comment.pster);
            if (StringHelper.isStringNullOrWhiteSpace(nick)){
                nick = comment.psterNk;
            }
            holder.senderInfoTextView.setText(String.format("By %s",nick));

            holder.contentTextView.setOnClickListener(onClickItemViews);
            holder.itemView.setOnClickListener(onClickItemViews);
            holder.senderInfoTextView.setOnClickListener(onClickItemViews);
            holder.extraInfoTextView.setOnClickListener(onClickItemViews);
        }

        @Override
        public int getItemCount() {
            return postComments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            private TextView extraInfoTextView;
            private TextView senderInfoTextView;
            private TextView contentTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                contentTextView = (TextView)itemView.findViewById(R.id.content_text_view);
                extraInfoTextView = (TextView)itemView.findViewById(R.id.subline_extra_info);
                senderInfoTextView = (TextView)itemView.findViewById(R.id.subline_sender_info);
            }
        }

        private void onClickItemView(PostCommentAdapter.ViewHolder viewHolder, View v, int pos) {

            SNSPostComment cmt = postComments.get(pos);
            if (viewHolder.itemView == v){
                newCommentAtUser(cmt);
                return;
            }
            switch (v.getId()) {
                case R.id.subline_sender_info:
                    ConversationViewActivity.openConversation(SNSPostCommentActivity.this, cmt.pster);
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
                    public void onPostNewComment(boolean posted, String msg) {
                        hud.dismiss();
                        if (posted){
                            inputViewManager.clearEditingText();
                            inputViewManager.hideKeyboard();
                            adapter.pushNewSendedComment(newComment);
                        }else {
                            if (StringHelper.isStringNullOrWhiteSpace(msg)){
                                ProgressHUDHelper.showHud(SNSPostCommentActivity.this,R.string.network_error,R.mipmap.cross_mark,true);
                            }else {
                                ProgressHUDHelper.showHud(SNSPostCommentActivity.this,msg,R.mipmap.cross_mark,true);
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
