package cn.bahamut.vessage.conversation.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListAdapter extends ConversationListAdapterBase {


    private static final int progressRed = Color.parseColor("#ff0000");
    private static final int progressOrange = Color.parseColor("#ffff8800");
    private static final int progressBlue = Color.parseColor("#ff33b5e5");

    public static final boolean CREATE_GROUP_CHAT_FEATURE_LOCKED = false;
    static public final int[] deviderIndex = new int[]{2};
    static public final int EXTRA_ITEM_COUNT = 1 + (CREATE_GROUP_CHAT_FEATURE_LOCKED ? 0 : 1) + deviderIndex.length;
    public static final int OPEN_CONTACT_INDEX = 0;
    public static final int START_GROUP_CHAT_INDEX = 1;

    public static boolean positionIsDevider(int position){
        for (int i : deviderIndex) {
            if(i == position){
                return true;
            }
        }
        return false;
    }

    public ConversationListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == OPEN_CONTACT_INDEX) {
            return ViewHolder.TYPE_TITLE_ITEM;
        } else if (!CREATE_GROUP_CHAT_FEATURE_LOCKED && position == START_GROUP_CHAT_INDEX) {
            return ViewHolder.TYPE_TITLE_ITEM;
        } else if (positionIsDevider(position)) {
            return ViewHolder.TYPE_DEVIDER;
        }
        return ViewHolder.TYPE_NORMAL_ITEM;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == OPEN_CONTACT_INDEX) {
            holder.title.setText(R.string.open_mobile_conversation);
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.contacts));
            holder.icon.setImageBitmap(bitmap);
        } else if (!CREATE_GROUP_CHAT_FEATURE_LOCKED && position == START_GROUP_CHAT_INDEX) {
            holder.title.setText(R.string.start_group_conversation);
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
            holder.icon.setImageBitmap(bitmap);
        } else if (holder.type == ViewHolder.TYPE_NORMAL_ITEM) {
            int realPos = position - EXTRA_ITEM_COUNT;
            ItemModel model = data.get(realPos);
            Conversation c = (Conversation) model.originModel;

            if (c.type == Conversation.TYPE_GROUP_CHAT) {
                Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
                holder.avatar.setImageBitmap(bitmap);
                ChatGroup chatCroup = chatGroupService.getCachedChatGroup(c.chatterId);
                if (chatCroup != null) {
                    holder.headline.setText(chatCroup.groupName);
                } else {
                    chatGroupService.fetchChatGroup(c.chatterId);
                }
            } else {
                VessageUser user = userService.getUserById(c.chatterId);
                if (user != null) {
                    holder.headline.setText(userService.getUserNoteOrNickName(c.chatterId));
                    ImageHelper.setImageByFileId(holder.avatar, model.avatar, AssetsDefaultConstants.getDefaultFace(c.chatterId.hashCode(), user.sex));
                } else {
                    userService.fetchUserByUserId(c.chatterId);
                }
            }

            holder.pinnedMark.setVisibility(c.isPinned ? View.VISIBLE : View.INVISIBLE);
            int progress = (int) (c.getTimeUpProgress() * 100);
            LayerDrawable layerDrawable = (LayerDrawable) holder.timeProgress.getProgressDrawable();
            Drawable progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);
            progressDrawable.clearColorFilter();
            if (c.isPinned) {
                holder.timeProgress.setProgress(100);
                progressDrawable.setColorFilter(progressBlue, PorterDuff.Mode.SRC);
                holder.pinnedMark.setVisibility(View.VISIBLE);
            } else {
                holder.timeProgress.setProgress(progress);
                holder.pinnedMark.setVisibility(View.INVISIBLE);
                if (progress < 30) {
                    progressDrawable.setColorFilter(progressRed, PorterDuff.Mode.SRC);
                } else if (progress < 60) {
                    progressDrawable.setColorFilter(progressOrange, PorterDuff.Mode.SRC);
                } else {
                    progressDrawable.setColorFilter(progressBlue, PorterDuff.Mode.SRC);
                }
            }

            holder.headline.setText(data.get(realPos).headLine);
            holder.subline.setText(data.get(realPos).subLine);
            String badge = data.get(realPos).badge;
            try {
                int badgeValue = Integer.parseInt(badge);
                holder.setBadge(badgeValue);
            } catch (NumberFormatException e) {
                holder.setBadge(0);
            }
        }
    }

    @Override
    protected void onUserProfileUpdated(VessageUser updatedUser) {
        notifyModelUpdated(updatedUser.userId);
    }

    @Override
    protected void onChatGroupUpdated(ChatGroup updatedChatGroup) {
        notifyModelUpdated(updatedChatGroup.groupId);
    }

    private void notifyModelUpdated(String uniqueId) {
        if (data == null || data.size() == 0) {
            return;
        }
        int index = EXTRA_ITEM_COUNT;
        for (ItemModel itemModel : data) {
            if (StringHelper.isStringNullOrWhiteSpace(itemModel.uniqueId) == false && itemModel.uniqueId.equals(uniqueId)) {
                notifyItemChanged(index);
            }
            index += 1;
        }
    }


    @Override
    public int getItemCount() {
        return EXTRA_ITEM_COUNT + data.size();
    }

    public Conversation getConversationOfIndex(int index){
        if(data.size() > index){
            return (Conversation)data.get(index).originModel;
        }else {
            return null;
        }
    }

    public boolean removeConversation(int position){
        if (data.size() > position){
            if (data.get(position).originModel instanceof Conversation){
                Conversation conversation = (Conversation) data.get(position).originModel;
                data.remove(position);
                ServicesProvider.getService(ConversationService.class).removeConversation(conversation.conversationId);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    public boolean pinConversation(int position){
        return setConversationPinned(position,true);
    }

    public boolean unpinConversation(int position){
        return setConversationPinned(position,false);
    }

    public boolean canPinConversation(){
        return ServicesProvider.getService(ConversationService.class).canPinMoreConversation();
    }

    private boolean setConversationPinned(int position, boolean pinned) {
        if (data.size() > position) {
            if (data.get(position).originModel instanceof Conversation) {
                Conversation conversation = (Conversation) data.get(position).originModel;
                conversation.isPinned = pinned;
                data.get(position).subLine = generateConversationSublineString(conversation);
                ServicesProvider.getService(ConversationService.class).setConversationPinned(conversation.conversationId, pinned);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    private UserService userService = ServicesProvider.getService(UserService.class);
    private ChatGroupService chatGroupService = ServicesProvider.getService(ChatGroupService.class);
    private VessageService vessageService = ServicesProvider.getService(VessageService.class);

    public int clearTimeUpConversations(){
        List<Conversation> timeUpConversations = ServicesProvider.getService(ConversationService.class).clearTimeupConversations();
        for (Conversation timeUpConversation : timeUpConversations) {
            if (data != null) {
                for (int i = data.size() - 1; i >= 0; i--) {
                    ItemModel model = data.get(i);
                    if (model.originModel instanceof Conversation) {
                        if (((Conversation) model.originModel).conversationId.equals(timeUpConversation.conversationId)) {
                            data.remove(i);
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
        return timeUpConversations.size();
    }

    public void reloadConversations() {
        data = new ArrayList<>();
        List<Conversation> list = ServicesProvider.getService(ConversationService.class).getAllConversations();
        for (Conversation conversation : list) {
            ItemModel model = new ItemModel();
            model.originModel = conversation;
            model.uniqueId = conversation.chatterId;

            model.subLine = generateConversationSublineString(conversation);

            int count = vessageService.getNotReadVessageCount(conversation.chatterId);
            if(conversation.type != Conversation.TYPE_GROUP_CHAT){
                VessageUser user = userService.getUserById(conversation.chatterId);
                if(user != null){
                    model.avatar = user.avatar;
                    model.headLine = userService.getUserNoteOrNickName(conversation.chatterId);
                }
            }else {
                ChatGroup group = ServicesProvider.getService(ChatGroupService.class).getCachedChatGroup(conversation.chatterId);
                if (group != null){
                    model.headLine = group.groupName;
                }else {
                    model.headLine = LocalizedStringHelper.getLocalizedString(R.string.group_chat);
                }
            }
            model.badge = String.format("%d",count);
            data.add(model);
        }
        notifyDataSetChanged();
    }

    private String generateConversationSublineString(Conversation conversation) {
        String subLine = null;
        long minLeft = conversation.getTimeUpMinutesLeft();
        if (minLeft % 3 > 0 && !conversation.isPinned) {
            if (minLeft > 24 * 60) {
                subLine = String.format(LocalizedStringHelper.getLocalizedString(R.string.x_days_disappear), minLeft / 60 / 24);
            } else if (minLeft > 60) {
                subLine = String.format(LocalizedStringHelper.getLocalizedString(R.string.x_hours_disappear), minLeft / 60);
            } else {
                subLine = String.format(LocalizedStringHelper.getLocalizedString(R.string.disappear_soon));
            }
        } else {
            subLine = AppUtil.dateToFriendlyString(getContext(), DateHelper.getDateFromUnixTimeSpace(conversation.lstTs));
        }
        return subLine;
    }

    public List<Conversation> getConversations(){
        List<Conversation> list = new ArrayList<Conversation>();
        for (int i = 0; i < data.size(); i++) {
            Conversation conversation = getConversationOfIndex(i);
            list.add(conversation);
        }
        return list;
    }
}
