package cn.bahamut.vessage.conversation;

import android.content.Context;

import java.util.LinkedList;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;

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
        data = new LinkedList<>();
        for (SearchManager.SearchResultModel model : searchManager.getSearchResultList()) {
            ItemModel itemModel = new ItemModel();
            itemModel.originModel = model;
            if(model.conversation != null){
                itemModel.headLine = model.conversation.noteName;
                itemModel.subLine = model.conversation.getLastMessageTime();
            }else if(model.user != null){
                if (StringHelper.isStringNullOrWhiteSpace(model.user.accountId)){
                    itemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.mobile_user);
                }else if(StringHelper.isStringNullOrWhiteSpace(model.user.nickName)){
                    itemModel.headLine = model.user.accountId;
                }else {
                    itemModel.headLine = model.user.nickName;
                }
                itemModel.subLine = "新建对话";
            }else if(model.mobile != null){
                itemModel.headLine = model.mobile;
                itemModel.subLine = "新建对话";
            }
            data.add(itemModel);
        }
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
