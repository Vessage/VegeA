package cn.bahamut.vessage.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;

/**
 * Created by alexchow on 16/3/30.
 */
public class ConversationListSearchAdapter extends ConversationListAdapterBase {

    public void init(){
        searchManager = new SearchManager();
        searchManager.addObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
    }

    public void release(){
        searchManager.deleteObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
    }

    public void search(String keyword){
        searchManager.searchKeywork(keyword);
    }

    private SearchManager searchManager;
    public void reloadResultList(){
        notifyDataSetChanged();
    }

    public SearchManager.SearchResultModel getSearchResult(int index){
        if(data.size() > index){
            return (SearchManager.SearchResultModel)data.get(index).originModel;
        }else {
            return null;
        }
    }

    private Observer onSearchResultUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            reloadResultList();
        }
    };

    public ConversationListSearchAdapter(Context context){
        super(context);
    }

}
