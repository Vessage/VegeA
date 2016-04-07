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
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.services.ConversationService;

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
        data = new ArrayList<>();
        List<Conversation> list = ServicesProvider.getService(ConversationService.class).getAllConversations();
        for (Conversation conversation : list) {
            ItemModel model = new ItemModel();
            model.originModel = conversation;
            model.headLine = conversation.noteName;
            model.subLine = DateHelper.toDateTimeString(conversation.sLastMessageTime);
            data.add(model);
        }
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
