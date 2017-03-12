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
import android.widget.EditText;
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
import cn.bahamut.vessage.activities.sns.model.SNSPostComment;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.user.UserService;

public class SNSMyCommentActivity extends AppCompatActivity {

    private static final int DEFAULT_PAGE_COUNT = 20;
    private CommentAdapter adapter;
    private UserService userService;
    private InputViewManager inputViewManager;
    private RecyclerView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_my_comment);
        getSupportActionBar().setTitle(R.string.sns_my_comments);
        inputViewManager = new InputViewManager(this, R.id.input_view);
        inputViewManager.setListener(inputViewManagerListener);
        inputViewManager.hideInputView();
        userService = ServicesProvider.getService(UserService.class);
        listView = (RecyclerView) findViewById(R.id.comment_list_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this);
        listView.setAdapter(adapter);
        int prepareCount = getIntent().getIntExtra("prepareCount", DEFAULT_PAGE_COUNT);
        adapter.loadComments(prepareCount);
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
                    adapter.loadComments(DEFAULT_PAGE_COUNT);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inputViewManager.onDestroy();
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
        LinkedList<SNSPostComment> comments = null;
        Activity context;
        private boolean noMoreData = false;
        private boolean loadingMore;

        public void pushNewSendedComment(SNSPostComment comment) {
            comments.add(comment);
            notifyDataSetChanged();
        }

        public void loadComments(final int pageCount) {
            if (noMoreData || loadingMore) {
                if (noMoreData) {
                    Toast.makeText(context, R.string.no_more_cmt_tips, Toast.LENGTH_SHORT).show();
                    listView.clearOnScrollListeners();
                }
                return;
            }
            long ts = DateHelper.getUnixTimeSpan();
            if (comments.size() > 0) {
                ts = comments.getLast().ts;
            }
            loadingMore = true;
            SNSPostManager.getInstance().getMyComments(ts, pageCount, new SNSPostManager.GetPostCommentCallback() {
                @Override
                public void onGetPostComment(SNSPostComment[] result) {
                    loadingMore = false;
                    if (result != null) {
                        noMoreData = result.length < pageCount;
                        for (SNSPostComment like : result) {
                            comments.add(like);
                        }
                        if (result.length > 0) {
                            notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(SNSMyCommentActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    }

                    if (comments.size() == 0) {
                        Toast.makeText(context, R.string.no_more_cmt_tips, Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

        public CommentAdapter(Activity context) {
            comments = new LinkedList<>();
            this.context = context;
        }

        @Override
        public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = context.getLayoutInflater().inflate(R.layout.sns_my_comment_item, null);
            return new CommentAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final CommentAdapter.ViewHolder holder, final int position) {
            SNSPostComment comment = comments.get(position);
            View.OnClickListener onClickItemViews = new View.OnClickListener() {
                int pos = position;
                CommentAdapter.ViewHolder viewHolder = holder;

                @Override
                public void onClick(View v) {
                    onClickItemView(viewHolder, v, pos);
                }
            };

            if (StringHelper.isNullOrEmpty(comment.img)) {
                holder.postImage.setVisibility(View.INVISIBLE);
            } else {
                holder.postImage.setVisibility(View.VISIBLE);
                holder.postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageHelper.setImageByFileId(holder.postImage, comment.img, R.drawable.sns_post_img_bcg);
                ImageHelper.setImageByFileIdOnView(holder.postImage, comment.img, R.drawable.sns_post_img_bcg, new ImageHelper.OnSetImageCallback() {
                    @Override
                    public void onSetImageSuccess() {
                        super.onSetImageSuccess();
                        holder.postImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                });
            }

            String nick = userService.getUserNotedName(comment.pster);
            if (StringHelper.isStringNullOrWhiteSpace(nick)) {
                nick = comment.psterNk;
            }
            holder.senderInfoTextView.setText(nick);

            if (comment.st >= 0) {
                holder.contentTextView.setText(comment.cmt);
            } else {
                holder.contentTextView.setText(R.string.cmt_removed);
            }
            String dateString = AppUtil.dateToFriendlyString(SNSMyCommentActivity.this, DateHelper.getDateFromUnixTimeSpace(comment.ts));
            String extraInfoStr = StringHelper.isStringNullOrWhiteSpace(comment.atNick) ? String.format("%s", dateString) : String.format("@%s %s", comment.atNick, dateString);

            if (StringHelper.isStringNullOrWhiteSpace(comment.txt) == false) {
                extraInfoStr = String.format("%s  %s", extraInfoStr, comment.txt);
            }

            holder.extraInfoTextView.setText(extraInfoStr);
            holder.postImage.setOnClickListener(onClickItemViews);
            holder.itemView.setOnClickListener(onClickItemViews);
            holder.senderInfoTextView.setOnClickListener(onClickItemViews);
            holder.extraInfoTextView.setOnClickListener(onClickItemViews);
            holder.contentTextView.setOnClickListener(onClickItemViews);
        }

        private void onClickItemView(CommentAdapter.ViewHolder viewHolder, View v, int pos) {
            SNSPostComment cmt = comments.get(pos);
            if (v == viewHolder.itemView) {
                newCommentAtUser(cmt);
                return;
            }
            switch (v.getId()) {
                case R.id.post_image:
                    BitmapDrawable drawable = ((BitmapDrawable) viewHolder.postImage.getDrawable());
                    if (drawable != null) {
                        new FullScreenImageViewer.Builder(this.context).setImageFileId(cmt.img).show();
                    } else {
                        Toast.makeText(context, R.string.img_data_not_ready, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.sender_info:
                    Map<String, Object> extraInfo = new HashMap<>();
                    extraInfo.put("activityId", SNSPostManager.ACTIVITY_ID);
                    ConversationViewActivity.openConversation(SNSMyCommentActivity.this, cmt.pster, extraInfo);
                    break;
                case R.id.content_text_view:
                case R.id.subline_extra_info:
                case R.id.item_view:
                    newCommentAtUser(cmt);
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
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

    private InputViewManager.InputViewManagerListener inputViewManagerListener = new InputViewManager.InputViewManagerListenerAdapter() {
        @Override
        public void onKeyboardClosed(InputViewManager manager) {
            super.onKeyboardClosed(manager);
            manager.hideInputView();
        }

        @Override
        public void onSendButtonClicked(InputViewManager manager, EditText editText, Object data) {
            super.onSendButtonClicked(manager, editText, data);
            String content = editText.getText().toString();
            if (StringHelper.isStringNullOrWhiteSpace(content)) {
                Toast.makeText(SNSMyCommentActivity.this, R.string.sns_white_comment_tips, Toast.LENGTH_SHORT).show();
            } else {
                String senderNick = SNSPostManager.getInstance().getUserProfile().nickName;
                String atUserId = null;
                String atUserNick = null;
                if (data != null) {
                    SNSPostComment cmt = (SNSPostComment) data;
                    atUserId = cmt.pster;

                    atUserNick = userService.getUserNotedName(atUserId);
                    if (StringHelper.isStringNullOrWhiteSpace(atUserNick)) {
                        atUserNick = cmt.psterNk;
                    }

                    final SNSPostComment newComment = new SNSPostComment();
                    newComment.psterNk = senderNick;
                    newComment.pster = UserSetting.getUserId();
                    newComment.ts = DateHelper.getUnixTimeSpan();
                    newComment.img = cmt.img;
                    newComment.atNick = atUserNick;
                    newComment.cmt = content;

                    final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(SNSMyCommentActivity.this);
                    SNSPostManager.getInstance().newPostComment(cmt.postId, content, senderNick, atUserId, atUserNick, new SNSPostManager.PostNewCommentCallback() {
                        @Override
                        public void onPostNewComment(boolean posted, String postedCmtId, String msg) {
                            hud.dismiss();
                            if (posted) {
                                inputViewManager.clearEditingText();
                                inputViewManager.hideKeyboard();
                                newComment.id = postedCmtId;
                                adapter.pushNewSendedComment(newComment);
                            } else {
                                if (StringHelper.isStringNullOrWhiteSpace(msg)) {
                                    ProgressHUDHelper.showHud(SNSMyCommentActivity.this, R.string.network_error, R.drawable.cross_mark, true);
                                } else {
                                    ProgressHUDHelper.showHud(SNSMyCommentActivity.this, msg, R.drawable.cross_mark, true);
                                }
                            }
                        }
                    });
                }

            }
        }
    };

    private void newCommentAtUser(SNSPostComment comment) {
        inputViewManager.showInputView();
        String atNick = userService.getUserNotedName(comment.pster);
        if (StringHelper.isStringNullOrWhiteSpace(atNick)) {
            atNick = comment.psterNk;
        }
        inputViewManager.openKeyboard("@" + atNick, comment);
    }

    public static void showMyCommentActivity(Context context, int prepareCount) {
        Intent intent = new Intent(context, SNSMyCommentActivity.class);
        if (prepareCount > 0) {
            intent.putExtra("prepareCount", prepareCount);
        }
        context.startActivity(intent);
    }
}
