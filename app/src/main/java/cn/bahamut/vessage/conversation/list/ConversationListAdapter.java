package cn.bahamut.vessage.conversation.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
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
    static public final int EXTRA_ITEM_COUNT = 1 + (CREATE_GROUP_CHAT_FEATURE_LOCKED ? 0 : 1);

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

    private UserService userService = ServicesProvider.getService(UserService.class);
    private ChatGroupService chatGroupService = ServicesProvider.getService(ChatGroupService.class);
    private VessageService vessageService = ServicesProvider.getService(VessageService.class);
    public void reloadConversations() {
        data = new ArrayList<>();
        List<Conversation> list = ServicesProvider.getService(ConversationService.class).getAllConversations();
        for (Conversation conversation : list) {
            ItemModel model = new ItemModel();
            model.originModel = conversation;
            model.subLine = AppUtil.dateToFriendlyString(getContext(),conversation.sLastMessageTime);
            int count = vessageService.getNotReadVessageCount(conversation.chatterId);
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
        if(position == 0){
            convertView = mInflater.inflate(R.layout.conversation_list_extra_item,null);
            ((TextView)convertView.findViewById(R.id.title)).setText(R.string.open_mobile_conversation);
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.contacts));
            ((ImageView)convertView.findViewById(R.id.icon)).setImageBitmap(bitmap);
            return convertView;
        }
        else if(position == 1 && !CREATE_GROUP_CHAT_FEATURE_LOCKED){ // Feature locked
            convertView = mInflater.inflate(R.layout.conversation_list_extra_item,null);
            ((TextView)convertView.findViewById(R.id.title)).setText(R.string.start_group_conversation);
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
            ((ImageView)convertView.findViewById(R.id.icon)).setImageBitmap(bitmap);
            return convertView;
        }
        int realPos = position - EXTRA_ITEM_COUNT;
        convertView = super.getView(realPos, convertView, parent);
        ItemModel model = data.get(realPos);
        ConversationListAdapterBase.ViewHolder holder = (ViewHolder) convertView.getTag();
        Conversation c = (Conversation)model.originModel;

        if (c.isGroup){
            Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().openRawResource(R.raw.group_chat));
            holder.avatar.setImageBitmap(bitmap);
            ChatGroup chatCroup = chatGroupService.getCachedChatGroup(c.chatterId);
            if(chatCroup!=null){
                holder.headline.setText(chatCroup.groupName);
            }else {
                chatGroupService.fetchChatGroup(c.chatterId);
            }
        }else {
            VessageUser user = userService.getUserById(c.chatterId);
            if(user != null){
                holder.headline.setText(userService.getUserNoteName(c.chatterId));
                ImageHelper.setImageByFileId(holder.avatar, model.avatar, AssetsDefaultConstants.getDefaultFace(c.chatterId.hashCode()));
            }else {
                userService.fetchUserByUserId(c.chatterId);
            }
        }
        return convertView;
    }
}
