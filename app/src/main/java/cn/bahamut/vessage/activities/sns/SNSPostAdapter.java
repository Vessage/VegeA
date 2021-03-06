package cn.bahamut.vessage.activities.sns;

import android.app.Activity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.DateHelper;
import cn.bahamut.common.FullScreenImageViewer;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSMainBoardData;
import cn.bahamut.vessage.activities.sns.model.SNSPost;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2016/11/14.
 */
public class SNSPostAdapter extends RecyclerView.Adapter<SNSPostAdapter.ViewHolder> {
    private static final String TAG = "SNSPostAdapter";

    private static final int DEFAULT_LOAD_COUNT = 20;
    private int postType = SNSPost.TYPE_NORMAL_POST;
    private Activity context;

    private SNSMainBoardData mainBoardData;

    private LinkedList<SNSPost>[] posts = new LinkedList[]{new LinkedList<>(), new LinkedList<>(), new LinkedList<>()};
    private boolean[] noMoreData = new boolean[]{false, false, false};

    private boolean loadingMore = false;

    private String specificUserId;
    private int imageItemWidth = 0;
    private int imageItemMaxWidth = 0;

    public SNSPostAdapter(Activity context) {
        this.context = context;
    }

    public void postNew(SNSPost newPost) {
        posts[getPostType()].add(0, newPost);
        notifyDataSetChanged();
    }

    public void setSpecificUserId(String specificUserId) {
        this.specificUserId = specificUserId;
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
                        addPosts(posts,SNSPost.TYPE_MY_POST);
                    }else if (posts == null && callback == null){
                        Toast.makeText(context,R.string.refresh_my_post_error,Toast.LENGTH_SHORT).show();
                    }
                    if (callback != null) {
                        callback.onRefreshCompleted(posts != null ? posts.length : -1);
                    }
                }
            });
        } else if (getPostType() == SNSPost.TYPE_SINGLE_USER_POST) {
            if (StringHelper.isStringNullOrWhiteSpace(specificUserId)) {
                return;
            }
            SNSPostManager.getInstance().getSingleUserPosts(specificUserId, DateHelper.getUnixTimeSpan(), DEFAULT_LOAD_COUNT, new SNSPostManager.GetPostCallback() {
                @Override
                public void onGetPosts(SNSPost[] posts) {
                    if (posts != null && posts.length > 0) {
                        SNSPostAdapter.this.posts[SNSPost.TYPE_SINGLE_USER_POST].clear();
                        addPosts(posts, SNSPost.TYPE_SINGLE_USER_POST);
                    } else if (posts == null && callback == null) {
                        Toast.makeText(context, R.string.get_sns_data_error, Toast.LENGTH_SHORT).show();
                    }
                    if (callback != null) {
                        callback.onRefreshCompleted(posts != null ? posts.length : -1);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onRefreshCompleted(0);
            }
        }
    }

    private void addPosts(SNSPost[] posts,int postType) {
        for (SNSPost post : posts) {
            this.posts[postType].add(post);
        }
        noMoreData[postType] = posts.length < DEFAULT_LOAD_COUNT;
        notifyDataSetChanged();
    }

    public void loadMorePosts() {
        if (noMoreData[getPostType()]) {
            return;
        }
        if (posts[getPostType()].size() == 0) {
            refreshPosts();
        } else {
            loadingMore = true;
            final int type = getPostType();
            if (type == SNSPost.TYPE_SINGLE_USER_POST) {
                SNSPostManager.getInstance().getSingleUserPosts(specificUserId, posts[getPostType()].getLast().ts, 20, new SNSPostManager.GetPostCallback() {
                    @Override
                    public void onGetPosts(SNSPost[] posts) {
                        loadingMore = false;
                        addPosts(posts, type);

                    }
                });
            } else {

                SNSPostManager.getInstance().getSNSPosts(type, posts[getPostType()].getLast().ts, 20, new SNSPostManager.GetPostCallback() {
                    @Override
                    public void onGetPosts(SNSPost[] posts) {
                        loadingMore = false;
                        addPosts(posts, type);

                    }
                });
            }
        }
    }


    @Override
    public int getItemCount() {
        if (getPostType() == SNSPost.TYPE_SINGLE_USER_POST) {
            return posts[getPostType()].size() + (loadingMore ? 1 : 0);
        } else {
            if (mainBoardData == null) {
                return 0;
            }
            return posts[getPostType()].size() + 1 + (loadingMore ? 1 : 0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getPostType() == SNSPost.TYPE_SINGLE_USER_POST) {
            if (loadingMore && position == getItemCount() - 1) {
                return ViewHolder.VIEW_TYPE_FOOTER;
            }
            return ViewHolder.VIEW_TYPE_POST_ITEM;
        }

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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder.getViewType() == ViewHolder.VIEW_TYPE_MAIN_INFO_ITEM) {
            holder.getMainInfoItemHolder().commentTextView.setText(String.format("+%d", mainBoardData.ncmt));
            holder.getMainInfoItemHolder().likeTextView.setText(String.format("+%d", mainBoardData.nlks));
            String annc = getPostType() == SNSPost.TYPE_NORMAL_POST ?
                    StringHelper.isStringNullOrWhiteSpace(mainBoardData.annc) ? LocalizedStringHelper.getLocalizedString(R.string.default_sns_annc) : mainBoardData.annc :
                    LocalizedStringHelper.getLocalizedString(R.string.my_post_tips);
            holder.getMainInfoItemHolder().anncTextView.setText(String.format(annc, SNSPostManager.getInstance().getUserProfile().nickName));
            holder.getMainInfoItemHolder().commentTextView.setOnClickListener(onClickMainInfoViews);
            holder.getMainInfoItemHolder().commentView.setOnClickListener(onClickMainInfoViews);
            holder.getMainInfoItemHolder().likeView.setOnClickListener(onClickMainInfoViews);
            holder.getMainInfoItemHolder().likeTextView.setOnClickListener(onClickMainInfoViews);
        } else if (holder.getViewType() == ViewHolder.VIEW_TYPE_POST_ITEM) {
            final int realPos = getPostType() == SNSPost.TYPE_SINGLE_USER_POST ? position : position - 1;
            View.OnClickListener onClickPostItemViews = new View.OnClickListener() {
                int pos = realPos;
                ViewHolder viewHolder = holder;

                @Override
                public void onClick(View v) {
                    onClickItemView(viewHolder, v, pos);
                }
            };

            holder.getPostItemHolder().itemView.setOnClickListener(onClickPostItemViews);
            holder.getPostItemHolder().likeButton.setOnClickListener(onClickPostItemViews);
            holder.getPostItemHolder().newCommentButton.setOnClickListener(onClickPostItemViews);

            SNSPost post = posts[getPostType()].get(realPos);

            if (UserSetting.getUserId().equals(post.usrId)) {
                holder.getPostItemHolder().chatButton.setVisibility(View.INVISIBLE);
            } else {
                holder.getPostItemHolder().chatButton.setOnClickListener(onClickPostItemViews);
                holder.getPostItemHolder().chatButton.setVisibility(View.VISIBLE);
            }

            holder.getPostItemHolder().moreButton.setOnClickListener(onClickPostItemViews);

            holder.getPostItemHolder().moreButton.setVisibility(post.usrId.equals(UserSetting.getUserId()) ? View.VISIBLE : View.INVISIBLE);
            holder.getPostItemHolder().likeTextView.setText(String.valueOf(post.lc));
            UserService userService = ServicesProvider.getService(UserService.class);
            String noteName = userService.getUserNotedNameIfExists(post.usrId);
            if (StringHelper.isStringNullOrWhiteSpace(noteName)) {
                noteName = post.pster;
            }

            holder.getPostItemHolder().textContent.setText(null);
            try {
                JSONObject bodyObject = post.getBodyObject();
                holder.getPostItemHolder().textContent.setText(bodyObject.getString("txt"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.getPostItemHolder().senderTextView.setText(noteName);

            String dateString = AppUtil.dateToFriendlyString(context, post.getPostDate());

            if (post.st == SNSPost.STATE_PRIVATE) {
                holder.getPostItemHolder().infoTextView.setText(String.format("%s %s", dateString, LocalizedStringHelper.getLocalizedString(R.string.sns_state_private)));
            } else if (post.atpv > 0) {
                holder.getPostItemHolder().infoTextView.setText(String.format("%s %s", dateString, LocalizedStringHelper.getLocalizedString(R.string.sns_auto_private)));
            } else {
                holder.getPostItemHolder().infoTextView.setText(dateString);
            }
            holder.getPostItemHolder().commentTextView.setText(String.valueOf(post.cmtCnt));

            VessageUser user = userService.getUserById(post.usrId);
            if (user != null) {
                if (!StringHelper.isStringNullOrWhiteSpace(user.avatar)) {
                    ImageHelper.setImageByFileId(context, holder.getPostItemHolder().avatarImageView, user.avatar);
                } else if (!StringHelper.isStringNullOrWhiteSpace(user.accountId)) {
                    holder.getPostItemHolder().avatarImageView.setImageResource(AssetsDefaultConstants.getDefaultFace(user.accountId.hashCode(), user.sex));
                } else {
                    holder.getPostItemHolder().avatarImageView.setImageResource(R.drawable.default_avatar);
                }
            } else {
                holder.getPostItemHolder().avatarImageView.setImageResource(R.drawable.default_avatar);
            }
            refreshPostImage(holder, holder.getPostItemHolder().imagesContainer, post);
        }
    }

    private View.OnClickListener onClickMainInfoViews = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.red_heart:
                case R.id.like_cnt:
                    mainBoardData.nlks = 0;
                    SNSReceivedLikeActivity.showReceivedLikeActivity(context, mainBoardData.nlks);
                    notifyItemChanged(0);
                    break;
                case R.id.cmt_cnt:
                case R.id.cmt_icon:
                    mainBoardData.ncmt = 0;
                    SNSMyCommentActivity.showMyCommentActivity(context, mainBoardData.ncmt);
                    notifyItemChanged(0);
                    break;
                default:break;
            }
        }
    };

    private void onClickItemView(ViewHolder viewHolder, View v, int postIndex) {
        SNSPost post = posts[getPostType()].get(postIndex);
        switch (v.getId()) {
            case R.id.more_btn:
                moreOperate(viewHolder, post);
                break;
            case R.id.like_btn:
                likePost(viewHolder, v, post);
                break;
            case R.id.new_cmt_btn:
                showCommentActivity(post, v);
                break;
            case R.id.chat_btn:
                chatWithSender(post, v);
                break;
            default:
                showCommentActivity(post, viewHolder.getPostItemHolder().newCommentButton);
                break;
        }
    }

    private void refreshPostImage(final ViewHolder holder, View v, SNSPost post) {
        List<String> imgList = new ArrayList<>(9);
        if (!StringHelper.isStringNullOrWhiteSpace(post.img)) {
            imgList.add(post.img);
        }
        try {
            JSONArray postBody = post.getBodyObject().getJSONArray("imgs");
            for (int i = 0; i < postBody.length(); i++) {
                String img = postBody.getString(i);
                if (!StringHelper.isStringNullOrWhiteSpace(img)) {
                    imgList.add(img);
                }
            }
        } catch (JSONException e) {
        }

        holder.postItemHolder.imagesContainer.removeAllViews();
        if (imageItemWidth == 0) {
            imageItemMaxWidth = context.getWindowManager().getDefaultDisplay().getWidth() - holder.postItemHolder.imagesContainer.getPaddingLeft() * 2;
            imageItemWidth = imageItemMaxWidth / 3;
        }
        int itemMinWidth = imageItemWidth;
        if (imgList.size() == 1) {
            itemMinWidth = imageItemMaxWidth / 2;
        }
        boolean setWidth = imgList.size() > 1;
        for (int i = 0; i < imgList.size(); i++) {
            final ImageView imageView = new ImageView(context);
            imageView.setMaxHeight(imageItemMaxWidth);
            imageView.setMaxWidth(imageItemMaxWidth);
            imageView.setMinimumWidth(itemMinWidth);
            final String fileId = imgList.get(i);
            if (setWidth) {
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(imageItemWidth, imageItemWidth);
                imageView.setLayoutParams(layoutParams);
                imageView.setPadding(10, 10, 10, 10);
            } else {
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(itemMinWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(layoutParams);
                imageView.setAdjustViewBounds(true);
            }
            holder.postItemHolder.imagesContainer.addView(imageView);
            ImageHelper.setImageByFileIdOnView(context, imageView, fileId, R.drawable.sns_post_img_bcg, new ImageHelper.OnSetImageCallback() {
                @Override
                public void onSetImageSuccess() {
                    super.onSetImageSuccess();
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new FullScreenImageViewer.Builder(context).setImageFileId(fileId).show();
                        }
                    });
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            });
        }

        if (imgList.size() == 4) {
            holder.postItemHolder.imagesContainer.setColumnCount(2);
        } else {
            holder.postItemHolder.imagesContainer.setColumnCount(3);
        }
    }

    private void chatWithSender(SNSPost post, View v) {
        final String userId = post.usrId;
        AnimationHelper.startAnimation(context, v, R.anim.button_scale_anim, new AnimationHelper.AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("activityId", SNSPostManager.ACTIVITY_ID);
                ConversationViewActivity.openConversation(context, userId, extraInfo);
            }
        });

    }

    private void showCommentActivity(final SNSPost post, View v) {
        AnimationHelper.startAnimation(context,v,R.anim.button_scale_anim,new AnimationHelper.AnimationListenerAdapter(){
            @Override
            public void onAnimationEnd(Animation animation) {
                SNSPostCommentActivity.showPostCommentActivity(context,post);
            }
        });

    }

    private void moreOperate(final ViewHolder viewHolder, final SNSPost post) {
        if (post.usrId.equals(UserSetting.getUserId())){
            PopupMenu popupMenu = new PopupMenu(context,viewHolder.getPostItemHolder().moreButton);
            popupMenu.getMenu().add(0,0,1,R.string.remove_sns_post);
            if (post.st == SNSPost.STATE_NORMAL) {
                popupMenu.getMenu().add(0, 0, 2, R.string.private_sns_post);
            } else if (post.st == SNSPost.STATE_PRIVATE) {
                popupMenu.getMenu().add(0, 0, 3, R.string.public_sns_post);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                private void modifyPostState(String pid, final int state) {
                    final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(context);
                    SNSPostManager.getInstance().modifyPostState(pid, state, new SNSPostManager.RequestSuccessCallback() {
                        @Override
                        public void onCompleted(Boolean isOk) {
                            hud.dismiss();
                            if (isOk) {
                                post.st = state;
                                if (state < 0) {
                                    deletePostById(post.pid);
                                } else {
                                    notifyItemChanged(viewHolder.getAdapterPosition());
                                }
                            } else {
                                ProgressHUDHelper.showHud(context, R.string.network_error, R.drawable.cross_mark, true);
                            }
                        }
                    });
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getOrder()) {
                        case 1:
                            modifyPostState(post.pid, SNSPost.STATE_REMOVED);
                            break;
                        case 2:
                            modifyPostState(post.pid, SNSPost.STATE_PRIVATE);
                            break;
                        case 3:
                            modifyPostState(post.pid, SNSPost.STATE_NORMAL);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            popupMenu.show();
        }
    }


    private void deletePostById(String postId) {
        for (LinkedList<SNSPost> snsPosts : posts) {
            for (int i = 0; i < snsPosts.size(); i++) {
                if (snsPosts.get(i).pid.equals(postId)){
                    snsPosts.remove(i);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    private void likePost(final ViewHolder viewHolder, final View v, final SNSPost post) {
        ServicesProvider.getService(ConversationService.class).expireConversation(post.usrId);
        AnimationHelper.startAnimation(context,v,R.anim.button_scale_anim);
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

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        setPostType(postType, true);
    }

    public void setPostType(int postType, boolean autoRefresh) {
        if (this.postType != postType) {
            this.postType = postType;

            if (autoRefresh && posts[getPostType()].size() == 0) {
                refreshPosts();
            }else {
                notifyDataSetChanged();
            }
        }
    }

    private void setMainBoardData(SNSMainBoardData mainBoardData) {
        this.mainBoardData = mainBoardData;
        this.posts[SNSPost.TYPE_NORMAL_POST].clear();
        ServicesProvider.getService(ExtraActivitiesService.class).clearActivityAllBadge(SNSPostManager.ACTIVITY_ID);
        addPosts(mainBoardData.posts,SNSPost.TYPE_NORMAL_POST);
    }

    public SNSMainBoardData getMainBoardData() {
        return mainBoardData;
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

            View likeButton;
            View newCommentButton;
            View chatButton;
            View moreButton;
            TextView likeTextView;
            TextView commentTextView;
            View redHeartView;
            View commentIconView;
            TextView infoTextView;
            TextView senderTextView;
            ImageView avatarImageView;
            TextView textContent;
            View itemView;
            GridLayout imagesContainer;

            PostItemHolder(View itemView) {
                this.itemView = itemView;
                avatarImageView = (ImageView) itemView.findViewById(R.id.avatar);
                textContent = (TextView) itemView.findViewById(R.id.text_content);
                textContent.setMovementMethod(LinkMovementMethod.getInstance());
                redHeartView = itemView.findViewById(R.id.red_heart);
                commentIconView = itemView.findViewById(R.id.cmt_icon);
                likeTextView = (TextView) itemView.findViewById(R.id.like_cnt);
                commentTextView = (TextView) itemView.findViewById(R.id.cmt_cnt);
                likeButton = itemView.findViewById(R.id.like_btn);
                newCommentButton = itemView.findViewById(R.id.new_cmt_btn);
                moreButton = itemView.findViewById(R.id.more_btn);
                chatButton = itemView.findViewById(R.id.chat_btn);
                imagesContainer = (GridLayout) itemView.findViewById(R.id.post_image_container);
                infoTextView = (TextView) itemView.findViewById(R.id.info_tv);
                senderTextView = (TextView) itemView.findViewById(R.id.sender_tv);
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
