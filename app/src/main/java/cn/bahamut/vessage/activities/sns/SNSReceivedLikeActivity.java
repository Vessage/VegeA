package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPostLike;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class SNSReceivedLikeActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private ReceivedLikeAdapter adapter;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_received_like);
        getSupportActionBar().setTitle(R.string.sns_received_likes);
        userService = ServicesProvider.getService(UserService.class);
        RecyclerView listView = (RecyclerView) findViewById(R.id.like_list);
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
                if (noMoreData){
                    Toast.makeText(context,R.string.no_more_likes_tips,Toast.LENGTH_SHORT).show();
                }
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
                        for (SNSPostLike like : result) {
                            likes.add(like);
                        }
                        noMoreData = result.length < pageCount;
                        if (result.length > 0){
                            notifyDataSetChanged();
                        }
                    }
                    if (likes.size() == 0){
                        Toast.makeText(context,R.string.no_more_likes_tips,Toast.LENGTH_SHORT).show();
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
                    onClickItemView(viewHolder, v, pos);
                }
            };

            if (StringHelper.isStringNullOrWhiteSpace(like.img)) {
                holder.postImage.setVisibility(View.INVISIBLE);
            } else {
                holder.postImage.setVisibility(View.VISIBLE);
                holder.postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageHelper.setImageByFileId(holder.postImage, like.img, R.drawable.sns_post_img_bcg);
                ImageHelper.setImageByFileIdOnView(holder.postImage, like.img, R.drawable.sns_post_img_bcg, new ImageHelper.OnSetImageCallback() {
                    @Override
                    public void onSetImageSuccess() {
                        super.onSetImageSuccess();
                        holder.postImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                });
            }

            String nick = userService.getUserNotedName(like.usrId);
            if (StringHelper.isStringNullOrWhiteSpace(nick)) {
                holder.senderInfoTextView.setText(like.nick);
            } else {
                holder.senderInfoTextView.setText(nick);
            }

            holder.textContent.setText(like.txt);

            holder.postImage.setOnClickListener(onClickItemViews);
            holder.itemView.setOnClickListener(onClickItemViews);
            holder.senderInfoTextView.setOnClickListener(onClickItemViews);
        }

        private void onClickItemView(ViewHolder viewHolder, View v, int pos) {
            SNSPostLike like = likes.get(pos);
            switch (v.getId()) {
                case R.id.post_image:
                    BitmapDrawable drawable = ((BitmapDrawable) viewHolder.postImage.getDrawable());
                    if (drawable != null) {
                        new FullScreenImageViewer.Builder(this.context).setImageFileId(like.img).show();
                    } else {
                        Toast.makeText(context, R.string.img_data_not_ready, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.sender_info:
                    VessageUser user = ServicesProvider.getService(UserService.class).getUserById(like.usrId);
                    if (user == null) {
                        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(context);
                        ServicesProvider.getService(UserService.class).fetchUserByUserId(like.usrId, new UserService.UserUpdatedCallback() {
                            @Override
                            public void updated(VessageUser user) {
                                hud.dismiss();
                                if (user != null) {
                                    openConversationWithUser(user);
                                } else {
                                    Toast.makeText(context, R.string.user_data_not_ready, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        openConversationWithUser(user);
                    }
                    break;
                default:
                    break;
            }
        }

        private void openConversationWithUser(VessageUser user) {
            Map<String, Object> extraInfo = new HashMap<>();
            extraInfo.put("activityId", SNSPostManager.ACTIVITY_ID);
            ConversationViewActivity.openConversation(SNSReceivedLikeActivity.this, user.userId, extraInfo);
        }

        @Override
        public int getItemCount() {
            return likes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            //private TextView extraInfoTextView;
            private TextView senderInfoTextView;
            private ImageView postImage;
            private TextView textContent;

            public ViewHolder(View itemView) {
                super(itemView);
                postImage = (ImageView) itemView.findViewById(R.id.post_image);
                senderInfoTextView = (TextView) itemView.findViewById(R.id.sender_info);
                textContent = (TextView) itemView.findViewById(R.id.subline_txt_content);
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
