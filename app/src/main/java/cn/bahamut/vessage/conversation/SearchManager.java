package cn.bahamut.vessage.conversation;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/4/2.
 */
public class SearchManager extends Observable {

    public static class SearchResultModel{
        public Conversation conversation;
        public VessageUser user;
        public String mobile;
        public String keyword;
    }

    public static final String NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED = "NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED";

    List<SearchResultModel> searchResultModels = new LinkedList<>();
    public void searchKeywork(final String keyword){
        searchResultModels.clear();
        final VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        if(ContactHelper.isMobilePhoneNumber(keyword)){
            List<Conversation> result = ServicesProvider.getService(ConversationService.class).searchConversations(keyword);
            for (Conversation conversation : result) {
                SearchResultModel model = new SearchResultModel();
                model.keyword = keyword;
                model.conversation = conversation;
                searchResultModels.add(model);
            }
            if(result.size() == 0){
                ServicesProvider.getService(UserService.class).fetchUserByMobile(keyword, new UserService.UserUpdatedCallback() {
                    @Override
                    public void updated(VessageUser user) {
                        if(user != null && !VessageUser.isTheSameUser(me,user)){
                            SearchResultModel model = new SearchResultModel();
                            model.keyword = keyword;
                            model.user = user;
                            searchResultModels.add(model);
                            postNotification(NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED);
                        }else {
                            VessageUser tmpUser = new VessageUser();
                            tmpUser.mobile = keyword;
                            if(!VessageUser.isTheSameUser(me,tmpUser)){
                                SearchResultModel model = new SearchResultModel();
                                model.mobile = keyword;
                                model.keyword = keyword;
                                searchResultModels.add(model);
                            }
                        }
                    }
                });
            }

        }else if(keyword.matches("([0-9]){6,10}")){
            VessageUser user = ServicesProvider.getService(UserService.class).getCachedUserByAccountId(keyword);
            if(user != null && !VessageUser.isTheSameUser(me,user)){
                SearchResultModel model = new SearchResultModel();
                model.keyword = keyword;
                model.user = user;
                searchResultModels.add(model);
            }else {
                ServicesProvider.getService(UserService.class).fetchUserByAccountId(keyword, new UserService.UserUpdatedCallback() {
                    @Override
                    public void updated(VessageUser user) {
                        if(user != null && !VessageUser.isTheSameUser(me,user)){
                            SearchResultModel model = new SearchResultModel();
                            model.keyword = keyword;
                            model.user = user;
                            searchResultModels.add(model);
                            postNotification(NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED);
                        }
                    }
                });
            }
        }
        postNotification(NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED);
    }

    public List<SearchResultModel> getSearchResultList(){
        return searchResultModels;
    }

}
