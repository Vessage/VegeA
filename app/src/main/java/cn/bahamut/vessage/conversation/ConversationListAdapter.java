package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;

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

    public void reloadConversations(){

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
