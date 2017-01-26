package cn.bahamut.vessage.conversation.chat;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.BTSize;
import cn.bahamut.common.DensityUtil;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.chat.bubblevessage.BubbleVessageHandler;
import cn.bahamut.vessage.conversation.chat.bubblevessage.BubbleVessageHandlerManager;
import cn.bahamut.vessage.conversation.chat.views.BezierBubbleView;
import cn.bahamut.vessage.conversation.chat.views.BubbleVessageContainer;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.services.file.FileService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by cplov on 2017/1/25.
 */

public class MessageListManager extends ConversationViewManagerBase {

    private static final int bubbleColorMyVessageColor = Color.parseColor("#aa0000aa");
    private static final int bubbleColorNormalVessageColor = Color.parseColor("#aaffffff");

    private static final String TAG = "MessageListManager";

    class ViewHolder extends RecyclerView.ViewHolder {
        public static final int VIEW_TYPE_LEFT_AVATAR = 0;
        public static final int VIEW_TYPE_TIPS = 1;
        public static final int VIEW_TYPE_RIGHT_AVATAR = 2;

        private final float startMarkPoint = DensityUtil.dip2px(AppMain.getInstance(),18);

        private BezierBubbleView bubbleView;
        private ViewGroup vessageContainer;
        private ImageView avatar;

        private TextView tipsTextView;

        private int viewType;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            vessageContainer = (ViewGroup) itemView.findViewById(R.id.content_container);
            vessageContainer.getParent().getParent().bringChildToFront((View) vessageContainer.getParent());
            if (viewType == ViewHolder.VIEW_TYPE_LEFT_AVATAR) {
                bubbleView = (BezierBubbleView) itemView.findViewById(R.id.bubble_view);
                bubbleView.setDirection(BezierBubbleView.BezierBubbleDirection.Right);
                bubbleView.setFillColor(bubbleColorNormalVessageColor);
                bubbleView.setAbsoluteStartMarkPoint(startMarkPoint);
            } else if (viewType == ViewHolder.VIEW_TYPE_RIGHT_AVATAR) {
                bubbleView = (BezierBubbleView) itemView.findViewById(R.id.bubble_view);
                bubbleView.setDirection(BezierBubbleView.BezierBubbleDirection.Left);
                bubbleView.setFillColor(bubbleColorMyVessageColor);
                bubbleView.setAbsoluteStartMarkPoint(startMarkPoint);
            }else if (viewType == ViewHolder.VIEW_TYPE_TIPS){
                tipsTextView = (TextView)itemView.findViewById(R.id.tips);
            }
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            Vessage vessage = vessages.get(position);
            return vessage.isMySendingVessage() ? ViewHolder.VIEW_TYPE_RIGHT_AVATAR : ViewHolder.VIEW_TYPE_LEFT_AVATAR;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int viewId = 0;
            switch (viewType) {
                case ViewHolder.VIEW_TYPE_LEFT_AVATAR:
                    viewId = R.layout.conversation_message_item_l;
                    break;
                case ViewHolder.VIEW_TYPE_RIGHT_AVATAR:
                    viewId = R.layout.conversation_message_item_r;
                    break;
                case ViewHolder.VIEW_TYPE_TIPS:
                    viewId = R.layout.conversation_message_tips;
                    break;
            }
            View view = getConversationViewActivity().getLayoutInflater().inflate(viewId, null);
            return new ViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Vessage vessage = vessages.get(position);
            String userId = vessage.getVessageRealSenderId();
            VessageUser user = userHashMap.get(vessage.getVessageRealSenderId());
            if (user == null) {
                UserService userService = ServicesProvider.getService(UserService.class);
                VessageUser cachedUser = userService.getUserById(userId);
                if (cachedUser == null) {
                    user = new VessageUser();
                    user.setUserId(userId);
                    userService.fetchUserByUserId(userId);
                } else {
                    user = cachedUser;
                }
                userHashMap.put(userId, user);
            }
            if (holder.viewType == ViewHolder.VIEW_TYPE_LEFT_AVATAR || holder.viewType == ViewHolder.VIEW_TYPE_RIGHT_AVATAR) {
                bindVessageContainerWithUser(holder, vessage);
                bindAvatarWithUser(holder, user);
            } else if (holder.viewType == ViewHolder.VIEW_TYPE_TIPS){
                bindTipsWithUserVessage(holder,user,vessage);
            }
        }

        private void bindTipsWithUserVessage(ViewHolder holder, VessageUser user, Vessage vessage) {

            try {
                JSONObject body = vessage.getBodyJsonObject();
                if (body != null) {
                    holder.tipsTextView.setText(body.getString("msg"));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void bindAvatarWithUser(ViewHolder holder, VessageUser user) {
            int avatarResId = AssetsDefaultConstants.getDefaultFace(user.userId.hashCode(), user.sex);
            ImageHelper.setImageByFileId(holder.avatar, user.getAvatar(), avatarResId);
        }

        private void bindVessageContainerWithUser(ViewHolder holder, Vessage vessage) {
            BubbleVessageHandler handler = BubbleVessageHandlerManager.getBubbleVessageHandler(vessage.typeId);
            Activity context = getConversationViewActivity();
            View contentView = handler.getContentView(context, vessage);
            holder.vessageContainer.removeAllViews();
            holder.vessageContainer.addView(contentView);
            handler.presentContent(getConversationViewActivity(),null,vessage,contentView);
            holder.itemView.requestLayout();
            holder.itemView.invalidate(0,0,0,0);
        }

        @Override
        public int getItemCount() {
            return vessages.size();
        }
    }

    RecyclerView messageListView;
    LinkedList<Vessage> vessages;
    HashMap<String, VessageUser> userHashMap = new HashMap<>();

    @Override
    public void initManager(ConversationViewActivity activity) {
        super.initManager(activity);
        List<Vessage> vsgs = ServicesProvider.getService(VessageService.class).getNotReadVessage(getConversation().chatterId);
        vessages = new LinkedList<>();
        vessages.addAll(vsgs);
        messageListView = (RecyclerView) activity.findViewById(R.id.vessage_list);
        messageListView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        messageListView.setAdapter(new Adapter());
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onNewVessagePushed);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeReadedVessages();
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onNewVessagePushed);
    }

    private Observer onNewVessagePushed = new Observer() {
        @Override
        public void update(ObserverState state) {
            SendVessageQueueTask task = (SendVessageQueueTask) state.getInfo();
            int index = vessages.size();
            vessages.add(task.vessage);
            messageListView.getAdapter().notifyItemInserted(index);
            messageListView.smoothScrollToPosition(index + 1);
        }
    };

    @Override
    public void onVessagesReceived(Collection<Vessage> receivedVessages) {
        super.onVessagesReceived(receivedVessages);
        int index = this.vessages.size();
        this.vessages.addAll(receivedVessages);
        messageListView.getAdapter().notifyItemRangeInserted(index, receivedVessages.size());
        messageListView.smoothScrollToPosition(index + receivedVessages.size());
    }

    private void removeReadedVessages() {

        List<Vessage> removeVessages = new ArrayList<>();

        for (Vessage vessage : vessages) {
            if (vessage.isNormalVessage()) {
                removeVessages.add(vessage);
            }
        }
        vessages.clear();
        for (Vessage vessage : removeVessages) {
            String fileId = vessage.fileId;
            File oldVideoFile = null;
            if (vessage.typeId == Vessage.TYPE_CHAT_VIDEO) {
                oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId, ".mp4");
            } else if (vessage.typeId == Vessage.TYPE_IMAGE) {
                oldVideoFile = ServicesProvider.getService(FileService.class).getFile(fileId, ".jpg");
            }
            if (oldVideoFile != null) {
                try {
                    oldVideoFile.delete();
                    Log.d("ConversationView", "Delete Passed Vessage File");
                } catch (Exception ex) {
                    oldVideoFile.deleteOnExit();
                    Log.d("ConversationView", "Delete Passed Vessage Video File On Exit");
                }
            }
        }
        ServicesProvider.getService(VessageService.class).removeVessages(removeVessages);

    }
}