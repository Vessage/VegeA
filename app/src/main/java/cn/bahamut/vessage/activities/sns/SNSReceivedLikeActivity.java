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
import cn.bahamut.vessage.activities.sns.model.SNSPostLike;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.UserService;

public class SNSReceivedLikeActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private ReceivedLikeAdapter adapter;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_received_like);
        userService = ServicesProvider.getService(UserService.class);
        RecyclerView listView = (RecyclerView) findViewById(R.id.comment_list_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SNSReceivedLikeActivity.ReceivedLikeAdapter(this);
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


    private class ReceivedLikeAdapter extends RecyclerView.Adapter<ReceivedLikeAdapter.ViewHolder>{
        LinkedList<SNSPostLike> likes = null;
        Activity context;
        private boolean noMoreData = false;
        private boolean loadingMore;

        public void loadLikes(final int pageCount){
            if (noMoreData || loadingMore){
                return;
            }
            long ts = DateHelper.getUnixTimeSpan();
            if (likes.size() > 0){
                ts = likes.getLast().ts;
            }
            loadingMore = true;
            SNSPostManager.getInstance().getMyReceivedLikes(ts, pageCount, new SNSPostManager.GetPostLikeCallback() {
                @Override
                public void onGetPostLike(SNSPostLike[] result) {
                    loadingMore = false;
                    if (result != null){
                        if (result.length < pageCount){
                            noMoreData = true;
                            for (SNSPostLike like : result) {
                                likes.add(like);
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
            likes = new LinkedList<>();
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = context.getLayoutInflater().inflate(R.layout.sns_like_post_item,null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            SNSPostLike like = likes.get(position);
            View.OnClickListener onClickItemViews = new View.OnClickListener() {
                int pos = position;
                ViewHolder viewHolder = holder;
                @Override
                public void onClick(View v) {
                    onClickItemView(viewHolder,v,pos);
                }
            };

            ImageHelper.setImageByFileId(holder.postImage,like.img,R.drawable.sns_post_img_bcg);

            String nick = userService.getUserNotedName(like.usrId);
            if (StringHelper.isStringNullOrWhiteSpace(nick)){
                holder.senderInfoTextView.setText(like.nick);
            }else {
                holder.senderInfoTextView.setText(nick);
            }
            holder.postImage.setOnClickListener(onClickItemViews);
            holder.itemView.setOnClickListener(onClickItemViews);
            holder.senderInfoTextView.setOnClickListener(onClickItemViews);
        }

        private void onClickItemView(ViewHolder viewHolder, View v, int pos) {

        }

        @Override
        public int getItemCount() {
            return likes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            //private TextView extraInfoTextView;
            private TextView senderInfoTextView;
            private ImageView postImage;

            public ViewHolder(View itemView) {
                super(itemView);
                postImage = (ImageView) itemView.findViewById(R.id.post_image);
                senderInfoTextView = (TextView) itemView.findViewById(R.id.sender_info);
            }
        }
    }


    public static void showReceivedLikeActivity(Context context, int prepareCount){
        Intent intent = new Intent(context,SNSReceivedLikeActivity.class);
        if (prepareCount > 0) {
            intent.putExtra("prepareCount", prepareCount);
        }
        context.startActivity(intent);
    }
}
