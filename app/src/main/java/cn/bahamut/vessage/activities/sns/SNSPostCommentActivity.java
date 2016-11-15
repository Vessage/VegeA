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
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.activities.sns.model.SNSPostComment;
import cn.bahamut.vessage.services.user.UserService;

public class SNSPostCommentActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private String postId;
    private PostCommentAdapter adapter;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_comment);
        getSupportActionBar().setTitle(R.string.sns_post_comment);
        userService = ServicesProvider.getService(UserService.class);
        postId = getIntent().getStringExtra("postId");
        findViewById(R.id.new_cmt_btn).setOnClickListener(onClickNewComment);
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
        userService = null;
    }

    private View.OnClickListener onClickNewComment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(SNSPostCommentActivity.this,"Building",Toast.LENGTH_SHORT).show();
        }
    };

    private class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.ViewHolder>{
        LinkedList<SNSPostComment> postComments = null;
        Activity context;
        private boolean noMoreData = false;
        private boolean loadingMore;

        public void loadComments(final int pageCount){
            if (noMoreData || loadingMore){
                return;
            }
            long ts = DateHelper.getUnixTimeSpan();
            if (postComments.size() > 0){
                ts = postComments.getLast().ts;
            }
            loadingMore = true;
            SNSPostManager.getInstance().getPostComment(postId, ts, pageCount,new SNSPostManager.GetPostCommentCallback() {
                @Override
                public void onGetPostComment(SNSPostComment[] comments) {
                    loadingMore = false;
                    if (comments != null){
                        if (comments.length < pageCount){
                            noMoreData = true;
                            for (SNSPostComment comment : comments) {
                                postComments.add(comment);
                            }
                        }
                        if (comments.length > 0){
                            notifyDataSetChanged();
                        }
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
            if (StringHelper.isStringNullOrWhiteSpace(comment.atNick) == false) {
                holder.extraInfoTextView.setText(comment.atNick);
            }

            String nick = userService.getUserNotedName(comment.pster);
            if (StringHelper.isStringNullOrWhiteSpace(nick)){
                holder.senderInfoTextView.setText(comment.psterNk);
            }else {
                holder.senderInfoTextView.setText(nick);
            }
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

        }
    }

    public static void showPostCommentActivity(Context context, SNSPost post) {
        showPostCommentActivity(context, post, 0);
    }

    public static void showPostCommentActivity(Context context, SNSPost post, int prepareCount){
        Intent intent = new Intent(context,SNSPostCommentActivity.class);
        intent.putExtra("postId",post.pid);
        if (prepareCount > 0) {
            intent.putExtra("prepareCount", prepareCount);
        }
        context.startActivity(intent);
    }
}