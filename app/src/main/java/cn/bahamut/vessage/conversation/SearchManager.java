package cn.bahamut.vessage.conversation;

import java.security.Provider;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;

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

    List<SearchResultModel> searchResultModels = new LinkedList<>();
    public void searchKeywork(final String keywork){
        searchResultModels.clear();
        final ObserverState state = new ObserverState();
        state.setNotifyType(NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED);

        if(ContactHelper.isMobilePhoneNumber(keywork)){
            List<Conversation> result = ServicesProvider.getService(ConversationService.class).searchConversations(keywork);
            for (Conversation conversation : result) {
                SearchResultModel model = new SearchResultModel();
                model.conversation = conversation;
                searchResultModels.add(model);
                postNotification(state);
            }
            if(result.size() == 0){
                ServicesProvider.getService(UserService.class).fetchUserByMobile(keywork, new UserService.UserUpdatedCallback() {
                    @Override
                    public void updated(VessageUser user) {
                        if(user != null){
                            SearchResultModel model = new SearchResultModel();
                            model.user = user;
                            searchResultModels.add(model);
                            postNotification(state);
                        }else {
                            SearchResultModel model = new SearchResultModel();
                            model.mobile = keywork;
                            searchResultModels.add(model);
                            postNotification(state);
                        }
                    }
                });
            }

        }else if(keywork.matches("[\\D](6,10)")){
            VessageUser user = ServicesProvider.getService(UserService.class).getCachedUserByAccountId(keywork);
            if(user != null){
                SearchResultModel model = new SearchResultModel();
                model.user = user;
                searchResultModels.add(model);
                postNotification(state);
            }else {
                ServicesProvider.getService(UserService.class).fetchUserByAccountId(keywork, new UserService.UserUpdatedCallback() {
                    @Override
                    public void updated(VessageUser user) {
                        if(user != null){
                            SearchResultModel model = new SearchResultModel();
                            model.user = user;
                            searchResultModels.add(model);
                            postNotification(state);
                        }
                    }
                });
            }
        }
    }

    public List<SearchResultModel> getSearchResultList(){
        return searchResultModels;
    }

}
