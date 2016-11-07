package cn.bahamut.vessage.conversation.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.VessageService;
import io.realm.Realm;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListAdapter extends ConversationListAdapterBase {

    public static final boolean CREATE_GROUP_CHAT_FEATURE_LOCKED = true;
    static public final int[] deviderIndex = new int[]{1};
    static public final int EXTRA_ITEM_COUNT = 1 + (CREATE_GROUP_CHAT_FEATURE_LOCKED ? 0 : 1) + deviderIndex.length;
    public static final int OPEN_CONTACT_INDEX = 0;
    public static final int START_GROUP_CHAT_INDEX = 2;

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
                Realm realm = ServicesProvider.getService(ConversationService.class).getRealm();
                realm.beginTransaction();
                conversation.deleteFromRealm();
                realm.commitTransaction();
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
        if (data.size() > position){
            if (data.get(position).originModel instanceof Conversation){
                Conversation conversation = (Conversation) data.get(position).originModel;
                Realm realm = ServicesProvider.getService(ConversationService.class).getRealm();
                realm.beginTransaction();
                conversation.isPinned = pinned;
                realm.commitTransaction();
                return true;
            }
        }
        return false;
    }

    private UserService userService = ServicesProvider.getService(UserService.class);
    private ChatGroupService chatGroupService = ServicesProvider.getService(ChatGroupService.class);
    private VessageService vessageService = ServicesProvider.getService(VessageService.class);

    public int clearTimeUpConversations(){
        Realm realm = ServicesProvider.getService(ConversationService.class).getRealm();
        List<Conversation> list = ServicesProvider.getService(ConversationService.class).getAllConversations();
        List<Conversation> timeUpConversations = new LinkedList<>();
        for (Conversation conversation : list) {
            if(!conversation.isPinned && conversation.getTimeUpProgress() < 0.03){
                timeUpConversations.add(conversation);
            }
        }
        if(timeUpConversations.size() > 0){
            realm.beginTransaction();
            for (Conversation timeUpConversation : timeUpConversations) {
                timeUpConversation.deleteFromRealm();
            }
            realm.commitTransaction();
            notifyDataSetChanged();
        }
        return timeUpConversations.size();
    }

    public void reloadConversations() {
        data = new ArrayList<>();
        List<Conversation> list = ServicesProvider.getService(ConversationService.class).getAllConversations();
        for (Conversation conversation : list) {
            ItemModel model = new ItemModel();
            model.originModel = conversation;
            model.subLine = AppUtil.dateToFriendlyString(getContext(),conversation.sLastMessageTime);
            int count = vessageService.getNotReadVessageCount(conversation.chatterId);
            if(!conversation.isGroup){
                VessageUser user = userService.getUserById(conversation.chatterId);
                if(user != null){
                    model.avatar = user.avatar;
                    model.headLine = userService.getUserNoteOrNickName(conversation.chatterId);
                }
            }
            model.badge = String.format("%d",count);
            data.add(model);
        }
        notifyDataSetChanged();
    }

    public List<Conversation> getConversations(){
        List<Conversation> list = new ArrayList<Conversation>();
        for (int i = 0; i < data.size(); i++) {
            Conversation conversation = getConversationOfIndex(i);
            list.add(conversation);
        }
        return list;
    }

    @Override
    public int getCount() {
        return EXTRA_ITEM_COUNT + data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == OPEN_CONTACT_INDEX) {
            convertView = mInflater.inflate(R.layout.conversation_list_extra_item, null);
            ((TextView) convertView.findViewById(R.id.title)).setText(R.string.open_mobile_conversation);
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.contacts));
            ((ImageView) convertView.findViewById(R.id.icon)).setImageBitmap(bitmap);
            return convertView;
        } else if (!CREATE_GROUP_CHAT_FEATURE_LOCKED && position == START_GROUP_CHAT_INDEX) { // Feature locked
            convertView = mInflater.inflate(R.layout.conversation_list_extra_item, null);
            ((TextView) convertView.findViewById(R.id.title)).setText(R.string.start_group_conversation);
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
            ((ImageView) convertView.findViewById(R.id.icon)).setImageBitmap(bitmap);
            return convertView;
        }else if(positionIsDevider(position)){
            convertView = mInflater.inflate(R.layout.list_view_section_header, null);
            return convertView;
        }
        int realPos = position - EXTRA_ITEM_COUNT;
        convertView = super.getView(realPos, convertView, parent);
        ItemModel model = data.get(realPos);
        ConversationListAdapterBase.ViewHolder holder = (ViewHolder) convertView.getTag();
        Conversation c = (Conversation) model.originModel;

        if (c.isGroup) {
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
                ImageHelper.setImageByFileId(holder.avatar, model.avatar, AssetsDefaultConstants.getDefaultFace(c.chatterId.hashCode()));
            } else {
                userService.fetchUserByUserId(c.chatterId);
            }
        }

        holder.pinnedMark.setVisibility(c.isPinned ? View.VISIBLE : View.INVISIBLE);
        int progress = (int) (c.getTimeUpProgress() * 100);
        LayerDrawable layerDrawable = (LayerDrawable) holder.timeProgress.getProgressDrawable();
        Drawable progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);
        progressDrawable.clearColorFilter();
        if (c.isPinned){
            holder.timeProgress.setProgress(100);
            progressDrawable.setColorFilter(progressBlue, PorterDuff.Mode.SRC);
        }else {
            holder.timeProgress.setProgress(progress);
            if(progress < 30){
                progressDrawable.setColorFilter(progressRed, PorterDuff.Mode.SRC);
            }else if (progress < 60){
                progressDrawable.setColorFilter(progressOrange, PorterDuff.Mode.SRC);
            } else {
                progressDrawable.setColorFilter(progressBlue, PorterDuff.Mode.SRC);
            }
        }
        return convertView;
    }

    private static final int progressRed = Color.parseColor("#ff0000");
    private static final int progressOrange = Color.parseColor("#ffff8800");
    private static final int progressBlue = Color.parseColor("#ff33b5e5");
}
