package cn.bahamut.vessage.conversation.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cn.bahamut.common.AnimationHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/4/2.
 */
public abstract class ConversationListAdapterBase extends RecyclerView.Adapter<ConversationListAdapterBase.ViewHolder> {

    interface ItemListener {
        void onClickNavItem(ConversationListAdapterBase adapter, ViewHolder viewHolder, int viewId);

        void onClickItem(ConversationListAdapterBase adapter, ViewHolder viewHolder, int itemModelPosition);

        void onLongClickItem(ConversationListAdapterBase adapter, ViewHolder viewHolder, int itemModelPosition);
    }

    private Context context;

    protected List<ItemModel> data;
    protected LayoutInflater mInflater = null;

    protected ItemListener itemListener;

    protected Context getContext() {
        return context;
    }

    public void setItemListener(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public ItemListener getItemListener() {
        return itemListener;
    }

    protected static class ItemModel {

        public String uniqueId = null;

        public String avatar;
        public String headLine;
        public String subLine;
        public int badge;
        public Object originModel;
    }

    protected int getItemModelPosition(ViewHolder viewHolder) {
        return viewHolder.getAdapterPosition();
    }

    //ViewHolder静态类
    protected class ViewHolder extends RecyclerView.ViewHolder {

        static public final int TYPE_NAV = 0;
        static public final int TYPE_DEVIDER = 1;
        static public final int TYPE_NORMAL_ITEM = 2;
        static public final int TYPE_TITLE_ITEM = 3;
        public int type;

        //Normal Item Views
        public ImageView avatar;
        public TextView headline;
        public TextView subline;
        public TextView badge;
        public ProgressBar timeProgress;
        public View pinnedMark;

        //Title Item Views
        public ImageView icon;
        public TextView title;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.type = viewType;
            if (viewType == ViewHolder.TYPE_NAV) {
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemListener != null) {
                            itemListener.onClickNavItem(ConversationListAdapterBase.this, ViewHolder.this, v.getId());
                        }
                    }
                };

                for (int i = 0; i < ((ViewGroup) itemView).getChildCount(); i++) {
                    ((ViewGroup) itemView).getChildAt(i).setOnClickListener(listener);
                }
            }
            if (viewType == ViewHolder.TYPE_TITLE_ITEM) {
                title = (TextView) itemView.findViewById(R.id.title);
                icon = (ImageView) itemView.findViewById(R.id.icon);
                itemView.setOnClickListener(onClickItemListener);
                itemView.setOnLongClickListener(onLongClickItemListener);
            } else if (viewType == ViewHolder.TYPE_NORMAL_ITEM) {
                avatar = (ImageView) itemView.findViewById(R.id.avatar_img_view);
                headline = (TextView) itemView.findViewById(R.id.headline_text);
                subline = (TextView) itemView.findViewById(R.id.subline_text);
                timeProgress = (ProgressBar) itemView.findViewById(R.id.time_progress);
                badge = (TextView) itemView.findViewById(R.id.badge_tv);
                pinnedMark = itemView.findViewById(R.id.pinned_mark);
                itemView.setOnClickListener(onClickItemListener);
                itemView.setOnLongClickListener(onLongClickItemListener);
            }
        }

        private View.OnClickListener onClickItemListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemListener != null) {
                    itemListener.onClickItem(ConversationListAdapterBase.this, ViewHolder.this, getItemModelPosition(ViewHolder.this));
                }
            }
        };

        private View.OnLongClickListener onLongClickItemListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemListener != null) {
                    itemListener.onLongClickItem(ConversationListAdapterBase.this, ViewHolder.this, getItemModelPosition(ViewHolder.this));
                }
                return false;
            }
        };

        public void setBadge(int badge){
            if(badge == 0){
                setBadge(null);
            }else {
                setBadge(String.valueOf(badge));
            }
        }

        private void setBadge(String badgeValue){
            if(StringHelper.isNullOrEmpty(badgeValue)){
                badge.setVisibility(View.INVISIBLE);
            }else {
                badge.setVisibility(View.VISIBLE);
                badge.setText(badgeValue);
                AnimationHelper.startAnimation(AppMain.getInstance(),badge,R.anim.button_scale_anim);
            }
        }
    }

    public ConversationListAdapterBase(Context context){
        this.context = context;

        ServicesProvider.getService(ChatGroupService.class).addObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED,onChatterProfileUpdated);
        ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onChatterProfileUpdated);

        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == ViewHolder.TYPE_NAV) {
            view = mInflater.inflate(R.layout.conversation_list_nav_item, null);
        } else if (viewType == ViewHolder.TYPE_DEVIDER) {
            view = mInflater.inflate(R.layout.view_section_header, null);
        } else if (viewType == ViewHolder.TYPE_TITLE_ITEM) {
            view = mInflater.inflate(R.layout.conversation_list_extra_item, null);
        } else if (viewType == ViewHolder.TYPE_NORMAL_ITEM) {
            view = mInflater.inflate(R.layout.conversation_list_view_item, null);
        }
        return new ViewHolder(view, viewType);
    }

    protected void onUserProfileUpdated(VessageUser updatedUser) {

    }

    protected void onChatGroupUpdated(ChatGroup updatedChatGroup) {

    }

    private Observer onChatterProfileUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            if (state.getNotifyType().equals(UserService.NOTIFY_USER_PROFILE_UPDATED)) {
                VessageUser user = (VessageUser) state.getInfo();
                if (user != null) {
                    onUserProfileUpdated(user);
                }
            } else if (state.getNotifyType().equals(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED)) {
                ChatGroup chatGroup = (ChatGroup) state.getInfo();
                if (chatGroup != null) {
                    onChatGroupUpdated(chatGroup);
                }
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        ServicesProvider.getService(ChatGroupService.class).deleteObserver(ChatGroupService.NOTIFY_CHAT_GROUP_UPDATED,onChatterProfileUpdated);
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onChatterProfileUpdated);
        super.finalize();
    }
}
