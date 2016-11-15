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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPostComment;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.UserService;

public class SNSMyCommentActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private SNSMyCommentActivity.ReceivedLikeAdapter adapter;
    private UserService userService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_my_comment);
        userService = ServicesProvider.getService(UserService.class);
        RecyclerView listView = (RecyclerView) findViewById(R.id.comment_list_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SNSMyCommentActivity.ReceivedLikeAdapter(this);
        listView.setAdapter(adapter);
        int prepareCount = getIntent().getIntExtra("prepareCount",DEFAULT_PAGE_COUNT);
        adapter.loadLikes(prepareCount);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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
                    adapter.loadLikes(DEFAULT_PAGE_COUNT);
                }
            }
        });
    }

    private class ReceivedLikeAdapter extends RecyclerView.Adapter<SNSMyCommentActivity.ReceivedLikeAdapter.ViewHolder>{
        LinkedList<SNSPostComment> comments = null;
        Activity context;
        private boolean noMoreData = false;
        private boolean loadingMore;

        public void loadLikes(final int pageCount){
            if (noMoreData || loadingMore){
                return;
            }
            long ts = DateHelper.getUnixTimeSpan();
            if (comments.size() > 0){
                ts = comments.getLast().ts;
            }
            loadingMore = true;
            SNSPostManager.getInstance().getMyComments(ts, pageCount, new SNSPostManager.GetPostCommentCallback() {
                @Override
                public void onGetPostComment(SNSPostComment[] result) {
                    loadingMore = false;
                    if (result != null){
                        if (result.length < pageCount){
                            noMoreData = true;
                            for (SNSPostComment like : result) {
                                comments.add(like);
                            }
                        }
                        if (result.length > 0){
                            notifyDataSetChanged();
                        }
                    }
                }
            });

        }

        public ReceivedLikeAdapter(Activity context) {
            comments = new LinkedList<>();
            this.context = context;
        }

        @Override
        public SNSMyCommentActivity.ReceivedLikeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = context.getLayoutInflater().inflate(R.layout.sns_my_comment_item,null);
            return new SNSMyCommentActivity.ReceivedLikeAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final SNSMyCommentActivity.ReceivedLikeAdapter.ViewHolder holder, final int position) {
            SNSPostComment comment = comments.get(position);
            View.OnClickListener onClickItemViews = new View.OnClickListener() {
                int pos = position;
                SNSMyCommentActivity.ReceivedLikeAdapter.ViewHolder viewHolder = holder;
                @Override
                public void onClick(View v) {
                    onClickItemView(viewHolder,v,pos);
                }
            };

            ImageHelper.setImageByFileId(holder.postImage,comment.img,R.drawable.sns_post_img_bcg);

            String nick = userService.getUserNotedName(comment.pster);
            if (StringHelper.isStringNullOrWhiteSpace(nick)){
                holder.senderInfoTextView.setText(comment.psterNk);
            }else {
                holder.senderInfoTextView.setText(nick);
            }
            holder.contentTextView.setText(comment.cmt);
            if (StringHelper.isStringNullOrWhiteSpace(comment.atNick) == false) {
                holder.extraInfoTextView.setText(String.format("@%s", comment.atNick));
            }
            holder.postImage.setOnClickListener(onClickItemViews);
            holder.itemView.setOnClickListener(onClickItemViews);
            holder.senderInfoTextView.setOnClickListener(onClickItemViews);
            holder.extraInfoTextView.setOnClickListener(onClickItemViews);
            holder.contentTextView.setOnClickListener(onClickItemViews);
        }

        private void onClickItemView(SNSMyCommentActivity.ReceivedLikeAdapter.ViewHolder viewHolder, View v, int pos) {

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private ImageView postImage;
            private TextView extraInfoTextView;
            private TextView senderInfoTextView;
            private TextView contentTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                postImage = (ImageView) itemView.findViewById(R.id.post_image);
                extraInfoTextView = (TextView) itemView.findViewById(R.id.subline_extra_info);
                senderInfoTextView = (TextView) itemView.findViewById(R.id.sender_info);
                contentTextView = (TextView) itemView.findViewById(R.id.content_text_view);

            }
        }
    }
    
    public static void showMyCommentActivity(Context context, int prepareCount){
        Intent intent = new Intent(context,SNSMyCommentActivity.class);
        if (prepareCount > 0) {
            intent.putExtra("prepareCount", prepareCount);
        }
        context.startActivity(intent);
    }
}
