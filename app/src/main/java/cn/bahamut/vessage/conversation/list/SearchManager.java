package cn.bahamut.vessage.conversation.list;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/4/2.
 */
public class SearchManager extends Observable {

    private static final String LAST_ONLINE_SEARCH_TIME_KEY = "LAST_ONLINE_SEARCH_TIME";
    private static final String SEARCH_CNT_IN_INTERVAL_KEY = "SEARCH_CNT_IN_INTERVAL_KEY";
    private static final long SEARCH_LIMIT_INTERVAL = 1000 * 60;
    private int SEARCH_LIMIT_COUNT_IN_INTERVAL = 3;

    public interface SearchCallback{
        void onFinished(boolean isLimited);
    }

    public static class SearchResultModel{
        public Conversation conversation;
        public VessageUser user;
        public String mobile;
        public String keyword;
    }

    public static final String NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED = "NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED";

    List<SearchResultModel> searchResultModels = new LinkedList<>();
    private String searching = null;
    public void searchKeywordLocal(final String keyword){
        searching = keyword;
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
        }else if(keyword.matches("([0-9]){6,10}")){
            VessageUser user = ServicesProvider.getService(UserService.class).getCachedUserByAccountId(keyword);
            if(user != null && !VessageUser.isTheSameUser(me,user)){
                SearchResultModel model = new SearchResultModel();
                model.keyword = keyword;
                model.user = user;
                searchResultModels.add(model);
            }
        }
        postNotification(NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED);
    }

    public void searchKeywordOnline(final String keyword, final SearchCallback callback){

        String key = UserSetting.generateUserSettingKey(LAST_ONLINE_SEARCH_TIME_KEY);
        final String kt = UserSetting.generateUserSettingKey(SEARCH_CNT_IN_INTERVAL_KEY);
        long lastSearchTime = UserSetting.getUserSettingPreferences().getLong(key,0);
        int searchedCountInInterval = UserSetting.getUserSettingPreferences().getInt(kt,0);
        long now = new Date().getTime();
        if(now - lastSearchTime < SEARCH_LIMIT_INTERVAL && searchedCountInInterval >= SEARCH_LIMIT_COUNT_IN_INTERVAL){
            callback.onFinished(true);
            return;
        }else if(now - lastSearchTime >= SEARCH_LIMIT_INTERVAL){
            searchedCountInInterval = 0;
            UserSetting.getUserSettingPreferences().edit().putLong(key,now).commit();
            UserSetting.getUserSettingPreferences().edit().putInt(kt, 0).commit();
        }

        if (!searching.equals(keyword)){
            searchResultModels.clear();
        }
        final VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        final int finalSearchedCountInInterval = searchedCountInInterval + 1;
        String accountIdRegex = UserSetting.godMode ? "([0-9]){4,10}":"([0-9]){6,10}";
        if(ContactHelper.isMobilePhoneNumber(keyword)){
            ServicesProvider.getService(UserService.class).fetchUserByMobile(keyword, new UserService.UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    boolean addModel = true;
                    if(user != null && !VessageUser.isTheSameUser(me,user)){
                        for (int i = 0; i < searchResultModels.size(); i++) {
                            SearchResultModel model = searchResultModels.get(i);
                            if (model.user != null && model.user.userId.equals(user.userId)){
                                model.user = user;
                                addModel = false;
                                break;
                            }
                        }
                        if (addModel) {
                            SearchResultModel model = new SearchResultModel();
                            model.keyword = keyword;
                            model.user = user;
                            searchResultModels.add(model);
                        }
                        UserSetting.getUserSettingPreferences().edit().putInt(kt, finalSearchedCountInInterval).commit();
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
                    callback.onFinished(false);
                }
            });
        }else if(keyword.matches(accountIdRegex) && !me.accountId.equals(keyword)){
            ServicesProvider.getService(UserService.class).fetchUserByAccountId(keyword, new UserService.UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if(user != null && !VessageUser.isTheSameUser(me,user)){
                        boolean addModel = true;
                        for (int i = 0; i < searchResultModels.size(); i++) {
                            SearchResultModel model = searchResultModels.get(i);
                            if (model.user != null && model.user.userId.equals(user.userId)){
                                model.user = user;
                                addModel = false;
                                break;
                            }
                        }
                        if (addModel) {
                            SearchResultModel model = new SearchResultModel();
                            model.keyword = keyword;
                            model.user = user;
                            searchResultModels.add(model);
                        }
                        UserSetting.getUserSettingPreferences().edit().putInt(kt, finalSearchedCountInInterval).commit();
                        postNotification(NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED);
                    }
                    callback.onFinished(false);
                }
            });
        }else {
            callback.onFinished(false);
        }
    }

    public List<SearchResultModel> getSearchResultList(){
        return searchResultModels;
    }

}
