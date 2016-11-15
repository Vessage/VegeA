package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSMainBoardData;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.LocalizedStringHelper;

/**
 * Created by alexchow on 2016/11/14.
 */
public class SNSPostAdapter extends RecyclerView.Adapter<SNSPostAdapter.ViewHolder> {
    private static final String TAG = "SNSPostAdapter";

    private static final int DEFAULT_LOAD_COUNT = 20;
    private int postType = SNSPost.TYPE_NORMAL_POST;
    private Activity context;

    private SNSMainBoardData mainBoardData;
    private LinkedList<SNSPost>[] posts = new LinkedList[]{new LinkedList<>(), new LinkedList<>()};

    private boolean[] noMoreData = new boolean[]{false,false};
    private boolean loadingMore = false;

    public SNSPostAdapter(Activity context) {
        this.context = context;
    }

    public interface RefreshPostCallback{
        void onRefreshCompleted(int received);
    }

    public boolean isInited(){
        return mainBoardData != null;
    }

    public boolean isLoadingMore(){
        return loadingMore;
    }

    public void refreshPosts(){
        refreshPosts(null);
    }
    
    

    public void refreshPosts(final RefreshPostCallback callback){

        if (getPostType() == SNSPost.TYPE_NORMAL_POST){
            SNSPostManager.getInstance().getMainBoardData(DEFAULT_LOAD_COUNT, new SNSPostManager.GetMainBoardDataCallback() {
                @Override
                public void onGetMainBoardDataCompleted(SNSMainBoardData data) {
                    if (data != null){
                        setMainBoardData(data);
                    }else if (callback == null){
                        Toast.makeText(context,R.string.get_sns_data_error,Toast.LENGTH_SHORT).show();
                    }
                    if (callback != null) {
                        callback.onRefreshCompleted(data != null ? data.posts.length : -1);
                    }
                }
            });
        }else if (getPostType() == SNSPost.TYPE_MY_POST){
            SNSPostManager.getInstance().getSNSMyPosts(DateHelper.getUnixTimeSpan(), DEFAULT_LOAD_COUNT, new SNSPostManager.GetPostCallback() {
                @Override
                public void onGetPosts(SNSPost[] posts) {

                    if (posts != null && posts.length > 0){
                        SNSPostAdapter.this.posts[SNSPost.TYPE_MY_POST].clear();
                        addPosts(posts);
                    }else if (posts == null && callback == null){
                        Toast.makeText(context,R.string.refresh_my_post_error,Toast.LENGTH_SHORT).show();
                    }
                    if (callback != null) {
                        callback.onRefreshCompleted(posts != null ? posts.length : -1);
                    }
                }
            });
        }else {
            if (callback != null) {
                callback.onRefreshCompleted(0);
            }
        }
    }

    private void addPosts(SNSPost[] posts) {
        for (SNSPost post : posts) {
            this.posts[getPostType()].add(post);
        }
        noMoreData[getPostType()] = posts.length < DEFAULT_LOAD_COUNT;
        notifyDataSetChanged();
    }

    public void loadMorePosts(){
        if (noMoreData[getPostType()]){
            return;
        }
        Log.d(TAG,"Load More Posts");
        if (posts[getPostType()].size() == 0){
            refreshPosts();
        }else {
            loadingMore = true;
            SNSPostManager.getInstance().getSNSPosts(getPostType(), posts[getPostType()].getLast().ts, 20, new SNSPostManager.GetPostCallback() {
                @Override
                public void onGetPosts(SNSPost[] posts) {
                    loadingMore = false;
                    addPosts(posts);

                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return ViewHolder.VIEW_TYPE_MAIN_INFO_ITEM;
        }else if (loadingMore && position == getItemCount() - 1){
            return ViewHolder.VIEW_TYPE_FOOTER;
        }else {
            return ViewHolder.VIEW_TYPE_POST_ITEM;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = null;
        if (viewType == ViewHolder.VIEW_TYPE_MAIN_INFO_ITEM) {
            item = context.getLayoutInflater().inflate(R.layout.sns_main_info_item, null);
        } else if (viewType == ViewHolder.VIEW_TYPE_POST_ITEM) {
            item = context.getLayoutInflater().inflate(R.layout.sns_post_item, null);
        } else if (viewType == ViewHolder.VIEW_TYPE_FOOTER) {
            item = context.getLayoutInflater().inflate(R.layout.view_list_refresh_footer, null);
        }
        return new ViewHolder(item, viewType);
    }

    private Rect displayRect;

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder.getViewType() == ViewHolder.VIEW_TYPE_MAIN_INFO_ITEM){
            holder.getMainInfoItemHolder().commentTextView.setText(String.format("+%d",mainBoardData.ncmt));
            holder.getMainInfoItemHolder().likeTextView.setText(String.format("+%d",mainBoardData.nlks));
            String annc = getPostType() == SNSPost.TYPE_NORMAL_POST ?
                    StringHelper.isStringNullOrWhiteSpace(mainBoardData.annc) ? LocalizedStringHelper.getLocalizedString(R.string.default_sns_annc) : mainBoardData.annc :
                    LocalizedStringHelper.getLocalizedString(R.string.my_post);
            holder.getMainInfoItemHolder().anncTextView.setText(String.format(annc, SNSPostManager.getInstance().getUserProfile().nickName));
            holder.getMainInfoItemHolder().commentTextView.setOnClickListener(onClickMainInfoViews);
            holder.getMainInfoItemHolder().commentView.setOnClickListener(onClickMainInfoViews);
            holder.getMainInfoItemHolder().likeView.setOnClickListener(onClickMainInfoViews);
            holder.getMainInfoItemHolder().likeTextView.setOnClickListener(onClickMainInfoViews);
        }else if (holder.getViewType() == ViewHolder.VIEW_TYPE_POST_ITEM){
            final int realPos = position - 1;
            View.OnClickListener onClickPostItemViews = new View.OnClickListener() {
                int pos = realPos;
                ViewHolder viewHolder = holder;
                @Override
                public void onClick(View v) {
                    onClickItemView(viewHolder,v,pos);
                }
            };
            ImageView postImage = holder.getPostItemHolder().imageView;
            holder.getPostItemHolder().likeButton.setOnClickListener(onClickPostItemViews);
            holder.getPostItemHolder().newCommentButton.setOnClickListener(onClickPostItemViews);
            holder.getPostItemHolder().chatButton.setOnClickListener(onClickPostItemViews);
            postImage.setOnClickListener(onClickPostItemViews);
            holder.getPostItemHolder().refreshImageButton.setOnClickListener(onClickPostItemViews);
            SNSPost post = posts[getPostType()].get(realPos);
            if (displayRect == null){
                displayRect = new Rect();
                context.getWindowManager().getDefaultDisplay().getRectSize(displayRect);
            }
            postImage.getLayoutParams().height = displayRect.width();
            refreshPostImage(holder,postImage,post);
        }
    }

    private View.OnClickListener onClickMainInfoViews = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.red_heart:
                case R.id.like_cnt:
                    SNSReceivedLikeActivity.showReceivedLikeActivity(context, mainBoardData.nlks == 0 ? 20 : mainBoardData.nlks);
                    break;
                case R.id.cmt_cnt:
                case R.id.cmt_icon:
                    SNSMyCommentActivity.showMyCommentActivity(context, mainBoardData.ncmt == 0 ? 20 : mainBoardData.ncmt);
                    break;
                default:break;
            }
        }
    };

    private void onClickItemView(ViewHolder viewHolder, View v, int postIndex) {
        SNSPost post = posts[getPostType()].get(postIndex);
        switch (v.getId()) {
            case R.id.like_btn:likePost(viewHolder, v, post);break;
            case R.id.new_cmt_btn:showCommentActivity(post);break;
            case R.id.chat_btn:chatWithSender(post);break;
            case R.id.post_image:clickPostImage(viewHolder,viewHolder.getPostItemHolder().imageView,post);break;
            case R.id.refresh_image_btn:refreshPostImage(viewHolder,v,post);break;
            default:break;
        }
    }

    private void refreshPostImage(final ViewHolder holder, View v, SNSPost post) {
        holder.getPostItemHolder().refreshImageButton.setVisibility(View.INVISIBLE);
        ImageHelper.getImageByFileId(post.img, new ImageHelper.OnGetImageCallback() {
            @Override
            public void onGetImageDrawable(Drawable drawable) {
                holder.getPostItemHolder().imageView.setImageDrawable(drawable);
            }

            @Override
            public void onGetImageResId(int resId) {
                holder.getPostItemHolder().imageView.setImageResource(resId);
            }

            @Override
            public void onGetImageFailed() {
                holder.getPostItemHolder().refreshImageButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void clickPostImage(ViewHolder viewHolder, ImageView imageView, SNSPost post) {
        BitmapDrawable drawable = ((BitmapDrawable)imageView.getDrawable());
        if (drawable != null){
            Bitmap bitmap = drawable.getBitmap();
            byte[] bytes = ImageHelper.bitmap2Bytes(bitmap);
            Intent intent = new Intent(context, FullScreenImageViewer.class);
            intent.putExtra("data",bytes);
            context.startActivity(intent);
        }else {
            Toast.makeText(context,R.string.img_data_not_ready,Toast.LENGTH_SHORT).show();
        }
    }

    private void chatWithSender(SNSPost post) {
        ConversationViewActivity.openConversation(context,post.usrId);
    }

    private void showCommentActivity(SNSPost post) {
        SNSPostCommentActivity.showPostCommentActivity(context,post);
    }

    private void likePost(final ViewHolder viewHolder, final View v, final SNSPost post) {
        if(!SNSPostManager.getInstance().likedInCached(post.pid)){
            SNSPostManager.getInstance().likePost(post.pid, new SNSPostManager.RequestSuccessCallback() {
                @Override
                public void onCompleted(Boolean isOk) {
                    if (isOk){
                        playAddOneLikeAnimation(viewHolder,post.lc + 1);
                    }else {
                        Toast.makeText(context,R.string.network_error,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            AnimationHelper.startAnimation(context,viewHolder.getPostItemHolder().redHeartView,R.anim.button_scale_anim);
        }
    }

    private void playAddOneLikeAnimation(ViewHolder viewHolder, final int finalLikes) {
        final TextView tv = viewHolder.getPostItemHolder().likeTextView;

        AnimationHelper.startAnimation(context, tv, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                tv.setText("+1");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                tv.setText(String.valueOf(finalLikes));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mainBoardData == null){
            return 0;
        }
        return posts[getPostType()].size() + 1 + (loadingMore ? 1 : 0);
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        if (this.postType != postType) {
            this.postType = postType;
            if (posts[getPostType()].size() == 0) {
                refreshPosts();
            }
        }
    }

    private void setMainBoardData(SNSMainBoardData mainBoardData) {
        this.mainBoardData = mainBoardData;
        this.posts[SNSPost.TYPE_NORMAL_POST].clear();
        addPosts(mainBoardData.posts);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        static final int VIEW_TYPE_MAIN_INFO_ITEM = 0;
        static final int VIEW_TYPE_POST_ITEM = 1;
        static final int VIEW_TYPE_FOOTER = 2;

        public int getViewType() {
            return viewType;
        }

        PostItemHolder getPostItemHolder() {
            return postItemHolder;
        }

        MainInfoItemHolder getMainInfoItemHolder() {
            return mainInfoItemHolder;
        }

        class MainInfoItemHolder{
            TextView anncTextView;
            View likeView;
            View commentView;
            TextView likeTextView;
            TextView commentTextView;
            MainInfoItemHolder(View itemView) {
                likeView = itemView.findViewById(R.id.red_heart);
                commentView = itemView.findViewById(R.id.cmt_icon);
                anncTextView = (TextView) itemView.findViewById(R.id.annc_text);
                likeTextView = (TextView) itemView.findViewById(R.id.like_cnt);
                commentTextView = (TextView) itemView.findViewById(R.id.cmt_cnt);
            }
        }

        class PostItemHolder{

            ImageView imageView;
            View likeButton;
            View newCommentButton;
            View chatButton;
            TextView likeTextView;
            TextView commentTextView;
            View redHeartView;
            View commentIconView;
            View refreshImageButton;

            PostItemHolder(View itemView) {
                redHeartView = itemView.findViewById(R.id.red_heart);
                commentIconView = itemView.findViewById(R.id.cmt_icon);
                likeTextView = (TextView) itemView.findViewById(R.id.like_cnt);
                commentTextView = (TextView) itemView.findViewById(R.id.cmt_cnt);
                likeButton = itemView.findViewById(R.id.like_btn);
                newCommentButton = itemView.findViewById(R.id.new_cmt_btn);
                chatButton = itemView.findViewById(R.id.chat_btn);
                imageView = (ImageView) itemView.findViewById(R.id.post_image);
                refreshImageButton = itemView.findViewById(R.id.refresh_image_btn);
            }
        }

        private MainInfoItemHolder mainInfoItemHolder;
        private PostItemHolder postItemHolder;
        private int viewType;

        public ViewHolder(View itemView,int viewType) {
            super(itemView);
            this.viewType = viewType;
            if (viewType == VIEW_TYPE_MAIN_INFO_ITEM){
                mainInfoItemHolder = new MainInfoItemHolder(itemView);
            }else if(viewType == VIEW_TYPE_POST_ITEM) {
                postItemHolder = new PostItemHolder(itemView);
            }
        }
    }
}
