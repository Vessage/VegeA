package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListAdapter extends ConversationListAdapterBase {


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

    public void reloadConversations() {
        UserService userService = ServicesProvider.getService(UserService.class);
        data = new ArrayList<>();
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        List<Conversation> list = ServicesProvider.getService(ConversationService.class).getAllConversations();
        for (Conversation conversation : list) {
            ItemModel model = new ItemModel();
            model.originModel = conversation;
            model.headLine = conversation.noteName;
            model.subLine = DateHelper.toDateTimeString(conversation.sLastMessageTime);
            if(!StringHelper.isStringNullOrEmpty(conversation.chatterId)){
                List notReadMsgs = vessageService.getNotReadVessage(conversation.chatterId);
                if(notReadMsgs.size() > 0){
                    model.badge = String.format("%d",notReadMsgs.size());
                }
                VessageUser user = null;
                user = userService.getUserById(conversation.chatterId);
                if(user != null){
                    model.avatar = user.avatar;
                }
            }
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
        return 1 + data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position == 0){
            convertView = mInflater.inflate(R.layout.conversation_list_view_contact_item,null);
            return convertView;
        }
        return super.getView(position - 1, convertView, parent);
    }
}
