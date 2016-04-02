package cn.bahamut.vessage.conversation;

import java.util.List;

import cn.bahamut.observer.Observable;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;

/**
 * Created by alexchow on 16/4/2.
 */
public class SearchManager extends Observable {

    public static class SearchResultModel{
        public Conversation conversation;
        public VessageUser user;
        public String mobile;
    }

    public static final String NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED = "NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED";

    public void searchKeywork(String keywork){

    }

    public List<SearchResultModel> getSearchResultList(){
        return null;
    }

}
