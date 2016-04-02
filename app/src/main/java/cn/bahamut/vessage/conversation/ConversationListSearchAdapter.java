package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListSearchAdapter extends ConversationListAdapterBase {

    public void reloadResultList(){

    }

    public SearchManager.SearchResultModel getSearchResult(int index){
        if(data.size() > index){
            return (SearchManager.SearchResultModel)data.get(index).originModel;
        }else {
            return null;
        }
    }

    public ConversationListSearchAdapter(Context context){
        super(context);
    }

}
