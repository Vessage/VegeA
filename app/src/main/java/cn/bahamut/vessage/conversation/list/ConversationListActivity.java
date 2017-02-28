package cn.bahamut.vessage.conversation.list;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.MenuItemBadge;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.UsersListActivity;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.main.VGCoreConstants;
import cn.bahamut.vessage.services.AppService;
import cn.bahamut.vessage.services.LocationService;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import cn.bahamut.vessage.userprofile.OpenConversationDelegate;
import cn.bahamut.vessage.userprofile.UserProfileView;
import cn.bahamut.vessage.usersettings.UserSettingsActivity;
import io.realm.Realm;

public class ConversationListActivity extends AppCompatActivity {

    private static final int OPEN_CONTACT_REQUEST_ID = 1;
    private static final int SELECT_GROUP_USERS_REQUEST_ID = 2;

    private static final long DEFAULT_NEAR_ACTIVE_AC_BEFORE_RM_TS = Conversation.MAX_LEFT_TIME_MS; //1000 * 60 * 60;

    private RecyclerView conversationListView;
    private SearchView searchView;

    private ConversationListAdapter listAdapter;
    private ConversationListSearchAdapter searchAdapter;
    private boolean isGoAhead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_conversation_list);
        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setOnCloseListener(onCloseSearchViewListener);
        searchView.setOnSearchClickListener(onSearchClickListener);
        conversationListView = (RecyclerView) findViewById(R.id.conversation_lv);
        conversationListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listAdapter = new ConversationListAdapter(this);
        searchAdapter = new ConversationListSearchAdapter(this);
        searchAdapter.init();
        listAdapter.reloadConversations();
        setAsConversationList();

        searchAdapter.setItemListener(onClickItemListener);
        listAdapter.setItemListener(onClickItemListener);

        findViewById(R.id.search_view_hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconifiedByDefault(true);
                searchView.setFocusable(true);
                searchView.setIconified(false);
                searchView.requestFocusFromTouch();
            }
        });

        ServicesProvider.getService(ConversationService.class).addObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        vessageService.addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED, onNewVessagesReceived);
        ServicesProvider.getService(ExtraActivitiesService.class).addObserver(ExtraActivitiesService.ON_ACTIVITIES_NEW_BADGES_UPDATED, onActivitiesBadgeUpdated);
        ServicesProvider.getService(ExtraActivitiesService.class).getActivitiesBoardData();

        LocationService locationService = ServicesProvider.getService(LocationService.class);
        locationService.addObserver(LocationService.LOCATION_UPDATED, onLocationUpdated);

    }

    private Observer onLocationUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            fetchNearUsers(true);
        }
    };

    private void fetchNearUsers(boolean checkTime) {
        String locationString = ServicesProvider.getService(LocationService.class).getHereString();
        if(!StringHelper.isNullOrEmpty(locationString)){
            ServicesProvider.getService(UserService.class).fetchNearUsers(locationString,checkTime);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppMain.getInstance().tryRegistDeviceToken();
        ServicesProvider.getService(AppService.class).checkAppLatestVersion(ConversationListActivity.this);
        ServicesProvider.getService(UserService.class).fetchActiveUsersFromServer(true);
        fetchNearUsers(true);
        listAdapter.reloadConversations();
        if (!isGoAhead){
            ServicesProvider.getService(VessageService.class).newVessageFromServer();
            ServicesProvider.getService(ExtraActivitiesService.class).getActivitiesBoardData();
        }
        isGoAhead = false;
        int timeupCnt = listAdapter.clearTimeUpConversations();
        if(timeupCnt > 0){
            Toast.makeText(this,String.format(LocalizedStringHelper.getLocalizedString(R.string.x_conversation_timeup),timeupCnt),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        release();
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        boolean showBadge = ServicesProvider.getService(ExtraActivitiesService.class).isActivityBadgeNotified();
        MenuItemBadge.update(menu.getItem(0), R.drawable.favorite, showBadge, R.drawable.red_dot).getActionView().setOnClickListener(onClickMenuItemNewIntersting);
        MenuItemBadge.update(menu.getItem(1), R.drawable.setting, false).getActionView().setOnClickListener(onClickMenuSetting);
        menu.add(1, 2, 1, R.string.tell_friends).setIcon(R.drawable.share).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(1, 3, 1, R.string.fqa).setIcon(R.drawable.question).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(1, 4, 1, R.string.check_app_update).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 2) {
            String title = String.format(LocalizedStringHelper.getLocalizedString(R.string.my_vg_id_x), UserSetting.getLastUserLoginedAccount());
            String msg = LocalizedStringHelper.getLocalizedString(R.string.tell_friends_vege_msg);
            AppMain.getInstance().showTellVegeToFriendsAlert(title, msg, R.string.tell_friends_alert_msg);

        } else if (item.getItemId() == 3) {
            Uri uri = Uri.parse("http://bahamut.cn/VGQA.html");
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(it);
        } else if (item.getItemId() == 4) {
            ServicesProvider.getService(AppService.class).checkAppLatestVersion(ConversationListActivity.this, true);
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener onClickMenuSetting = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showUserSetting();
        }
    };

    private View.OnClickListener onClickMenuItemNewIntersting = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showActivitiesList();
        }
    };

    private void showUserSetting() {
        isGoAhead = true;
        Intent intent = new Intent(ConversationListActivity.this, UserSettingsActivity.class);
        startActivity(intent);
    }

    private void showActivitiesList() {
        isGoAhead = true;
        invalidateOptionsMenu();
        ServicesProvider.getService(ExtraActivitiesService.class).clearActivityBadgeNotify();
        Intent intent = new Intent(ConversationListActivity.this, ExtraActivitiesActivity.class);
        startActivity(intent);
    }

    private void release() {
        searchAdapter.release();
        ServicesProvider.getService(LocationService.class).deleteObserver(LocationService.LOCATION_UPDATED,onLocationUpdated);
        ServicesProvider.getService(ExtraActivitiesService.class).deleteObserver(ExtraActivitiesService.ON_ACTIVITIES_NEW_BADGES_UPDATED,onActivitiesBadgeUpdated);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED,onNewVessagesReceived);
        ServicesProvider.getService(ConversationService.class).deleteObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
    }

    private void setAsSearchList() {
        conversationListView.setAdapter(this.searchAdapter);
        this.searchAdapter.notifyDataSetChanged();
        this.searchAdapter.searchLocal(null);
        findViewById(R.id.search_view_hint).setVisibility(View.INVISIBLE);
    }

    private void setAsConversationList(){
        conversationListView.setAdapter(this.listAdapter);
        conversationListView.getAdapter().notifyDataSetChanged();
        findViewById(R.id.search_view_hint).setVisibility(View.VISIBLE);
    }
    private SearchView.OnCloseListener onCloseSearchViewListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {
            setAsConversationList();
            return false;
        }
    };
    private View.OnClickListener onSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setAsSearchList();
        }
    };
    private KProgressHUD hud;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (conversationListView.getAdapter() != searchAdapter) {
                return false;
            }
            searchAdapter.searchOnline(query, new SearchManager.SearchCallback() {
                @Override
                public void onFinished(boolean isLimited) {
                    if (isLimited) {
                        Toast.makeText(ConversationListActivity.this, R.string.search_limited, Toast.LENGTH_LONG).show();
                    }
                }
            });
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (conversationListView.getAdapter() == searchAdapter){
                searchAdapter.searchLocal(newText);
            }
            return false;
        }
    };

    private Observer onActivitiesBadgeUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            invalidateOptionsMenu();
        }
    };

    private Observer onNewVessagesReceived = new Observer(){

        @Override
        public void update(ObserverState state) {
            ConversationService conversationService = ServicesProvider.getService(ConversationService.class);
            List<Conversation> loadedConversations = conversationService.getAllConversations();
            List<Vessage> vsgs = (List<Vessage>)state.getInfo();
            Map<String,Long> updateConversationLastDateMap = new HashMap<>();
            for (Vessage vsg : vsgs) {
                boolean exists = false;
                for (int i = 0; i < loadedConversations.size(); i++) {
                    Conversation conversation = loadedConversations.get(i);
                    if (conversation.isInConversation(vsg)) {
                        updateConversationLastDateMap.put(conversation.conversationId, vsg.ts);
                        exists = true;
                    }
                }
                if (!exists) {
                    Conversation conversation = conversationService.openConversationVessageInfo(vsg.sender, vsg.isGroup);
                    loadedConversations.add(conversation);
                    if (conversation.type == Conversation.TYPE_SINGLE_CHAT) {
                        ServicesProvider.getService(UserService.class).fetchUserByUserId(conversation.chatterId);
                    } else if (conversation.type == Conversation.TYPE_GROUP_CHAT) {
                        ServicesProvider.getService(ChatGroupService.class).fetchChatGroup(conversation.chatterId);
                    }
                }
            }

            try (Realm realm = Realm.getDefaultInstance()) {
                realm.beginTransaction();
                for (Conversation conversation : loadedConversations) {
                    Long date = updateConversationLastDateMap.get(conversation.conversationId);
                    if (date != null) {
                        Conversation con = realm.where(Conversation.class).equalTo("conversationId", conversation.conversationId).findFirst();
                        if (con != null && con.lstTs < date) {
                            con.lstTs = date;
                            conversation.lstTs = date;
                            conversation.activityId = null;
                        }
                    }
                }
                realm.commitTransaction();
            }
            listAdapter.reloadConversations();
        }
    };

    private Observer onConversationListUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            listAdapter.reloadConversations();
        }
    };

    private void pinConversation(int index,ConversationListAdapterBase.ViewHolder viewHolder) {
        if (!listAdapter.canPinConversation()) {
            Toast.makeText(this,String.format(LocalizedStringHelper.getLocalizedString(R.string.x_pin_limit),ConversationService.MAX_PIN_CONVERSATION_LIMIT),Toast.LENGTH_SHORT).show();
        } else if (!listAdapter.pinConversation(index)) {
            Toast.makeText(this,R.string.pin_error,Toast.LENGTH_SHORT).show();
        }
    }

    private void unpinConversation(int index,ConversationListAdapterBase.ViewHolder viewHolder) {
        listAdapter.unpinConversation(index);
    }

    private void removeConversation(final int index,ConversationListAdapterBase.ViewHolder viewHolder) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ConversationListActivity.this)
                .setTitle(R.string.ask_remove_conversation)
                .setMessage(viewHolder.headline.getText())
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!listAdapter.removeConversation(index)){
                            Toast.makeText(ConversationListActivity.this,R.string.remove_conversation_fail,Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    ConversationListAdapterBase.ItemListener onClickItemListener = new ConversationListAdapterBase.ItemListener() {

        @Override
        public void onClickItem(ConversationListAdapterBase adapter, ConversationListAdapterBase.ViewHolder viewHolder, int position) {
            if(adapter instanceof ConversationListAdapter){
                if(position == ConversationListAdapter.OPEN_CONTACT_INDEX){
                    openContactView();
                }else if(!ConversationListAdapter.CREATE_GROUP_CHAT_FEATURE_LOCKED && position == ConversationListAdapter.START_GROUP_CHAT_INDEX){
                    openSelectUserForChatGroup();
                }else if(!ConversationListAdapter.positionIsDevider(position))
                {
                    openConversationView((ConversationListAdapter)adapter,position - ConversationListAdapter.EXTRA_ITEM_COUNT);
                }
            }else if(adapter instanceof  ConversationListSearchAdapter) {
                openSearchResult((ConversationListSearchAdapter) adapter, position);
            }
        }

        @Override
        public void onLongClickItem(ConversationListAdapterBase adapter, final ConversationListAdapterBase.ViewHolder viewHolder, int position) {
            if (conversationListView.getAdapter() == listAdapter) {

                if (position < ConversationListAdapter.EXTRA_ITEM_COUNT) {
                    return;
                }

                final int index = position - ConversationListAdapter.EXTRA_ITEM_COUNT;
                PopupMenu popupMenu = new PopupMenu(ConversationListActivity.this, viewHolder.itemView);
                popupMenu.getMenu().add(0, 0, 1, R.string.remove);
                if (listAdapter.getConversationOfIndex(index).isPinned) {
                    popupMenu.getMenu().add(0, 0, 2, R.string.unpin);
                } else {
                    popupMenu.getMenu().add(0, 0, 3, R.string.pin);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getOrder()) {
                            case 1:
                                removeConversation(index, viewHolder);
                                break;
                            case 2:
                                unpinConversation(index, viewHolder);
                                break;
                            case 3:
                                pinConversation(index, viewHolder);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        }
    };

    private void openSelectUserForChatGroup() {
        isGoAhead = true;
        new UsersListActivity.ShowSelectUserActivityBuilder(ConversationListActivity.this)
                .setAllowMultiselection(true)
                .setCanSelectNearUser(false)
                .setCanSelectMobile(false)
                .setCanSelectActiveUser(false)
                .setMultiselectionMaxCount(ChatGroup.MAX_USERS_COUNT - 1)
                .setMultiselectionMinCount(2)
                .setRemoveMyProfile(true)
                .setConversationUserIdList()
                .setTitle(LocalizedStringHelper.getLocalizedString(R.string.start_group_conversation))
                .showActivity(SELECT_GROUP_USERS_REQUEST_ID);
    }

    private void openSearchResult(ConversationListSearchAdapter adapter, int index){
        SearchManager.SearchResultModel resultModel = adapter.getSearchResult(index);
        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        setAsConversationList();
        if(resultModel.conversation != null){
            MobclickAgent.onEvent(ConversationListActivity.this,"Vege_OpenSearchResultConversation");
            openConversationView(resultModel.conversation);
        }else if(resultModel.user != null){
            HashMap<String, Object> info = new HashMap<>();
            if (resultModel.userType == SearchManager.SearchResultModel.USER_TYPE_ACTIVE ||
                    resultModel.userType == SearchManager.SearchResultModel.USER_TYPE_NEAR ||
                    resultModel.userType == SearchManager.SearchResultModel.USER_TYPE_NEAR_ACTIVE) {
                info.put("activityId", VGCoreConstants.NEAR_ACTIVE_ACTIVITY_ID);
                info.put("beforeRemoveMS", DEFAULT_NEAR_ACTIVE_AC_BEFORE_RM_TS);
            }
            openUserProfileView(resultModel.user, info);
        }else if(resultModel.mobile != null){
            MobclickAgent.onEvent(ConversationListActivity.this,"Vege_OpenSearchResultMobileConversation");
            openMobileConversation(resultModel.mobile,resultModel.mobile);
        }
    }

    private void openUserProfileView(VessageUser user, Map<String, Object> extraInfo) {
        UserProfileView userProfileView = new UserProfileView(ConversationListActivity.this, user);
        OpenConversationDelegate delegate = new OpenConversationDelegate();
        delegate.showAccountId = extraInfo != null && extraInfo.containsKey("activityId") == false;
        delegate.snsPreviewEnabled = delegate.showAccountId;
        delegate.conversationExtraInfo = extraInfo;
        userProfileView.delegate = delegate;
        userProfileView.show();
    }

    private void openContactView(){
        MobclickAgent.onEvent(ConversationListActivity.this,"Vege_OpenContactView");
        isGoAhead = true;
        Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, OPEN_CONTACT_REQUEST_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            return;
        }
        switch (requestCode){
            case OPEN_CONTACT_REQUEST_ID:handleContactResult(data);break;
            case SELECT_GROUP_USERS_REQUEST_ID:handleOpenGroupChat(data);break;
        }
    }

    private void handleOpenGroupChat(Intent data) {
        List<String> userIds = data.getStringArrayListExtra(UsersListActivity.SELECTED_USER_IDS_ARRAY_KEY);
        if (userIds.size() < 1){
            Toast.makeText(ConversationListActivity.this,R.string.please_at_list_two_user,Toast.LENGTH_SHORT).show();
        }else {
            hud = ProgressHUDHelper.showSpinHUD(this);
            ChatGroupService.OnCreatChatGroupHandler handler = new ChatGroupService.OnCreatChatGroupHandler() {
                @Override
                public void onCreateChatGroupError() {
                    hud.dismiss();
                    Toast.makeText(ConversationListActivity.this,R.string.create_chat_group_fail,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCreatedChatGroup(ChatGroup chatGroup) {
                    hud.dismiss();
                    Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByGroup(chatGroup);
                    ConversationViewActivity.openConversationView(ConversationListActivity.this,conversation);
                }
            };
            UserService userService = ServicesProvider.getService(UserService.class);
            String groupName = LocalizedStringHelper.getLocalizedString(R.string.new_group);
            for (String userId : userIds) {
                VessageUser user = userService.getUserById(userId);
                if(user != null && !StringHelper.isStringNullOrWhiteSpace(user.nickName)){
                    groupName = String.format(LocalizedStringHelper.getLocalizedString(R.string.new_group_name_format),user.nickName,userIds.size());
                    break;
                }
            }
            ServicesProvider.getService(ChatGroupService.class).createNewChatGroup(groupName,userIds.toArray(new String[0]),handler);
        }
    }

    private void handleContactResult(Intent data) {
        Uri uri = data.getData();
        AppUtil.selectContactPerson(this, uri, new AppUtil.OnSelectContactPerson() {
            @Override
            public void onSelectContactPerson(String mobile,String contact) {
                openMobileConversation(mobile,contact);
            }
        });
    }

    private void openMobileConversation(String mobile, final String noteName) {
        VessageUser user = ServicesProvider.getService(UserService.class).getUserByMobile(mobile);
        if (user != null) {
            openUserProfileView(user, new HashMap<String, Object>());
        } else {
            hud = ProgressHUDHelper.showSpinHUD(ConversationListActivity.this);
            ServicesProvider.getService(UserService.class).fetchUserByMobile(mobile, new UserService.UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    hud.dismiss();
                    if (user != null) {
                        openUserProfileView(user, null);
                    } else {
                        String title = String.format(LocalizedStringHelper.getLocalizedString(R.string.my_vg_id_x), UserSetting.getLastUserLoginedAccount());
                        String msg = LocalizedStringHelper.getLocalizedString(R.string.tell_friends_vege_msg);
                        AppMain.getInstance().showTellVegeToFriendsAlert(title, msg, R.string.invite_ta, R.string.no_such_mobile_user);
                    }
                }
            });
        }

        listAdapter.reloadConversations();
    }

    private void openConversationView(Conversation conversation){
        isGoAhead = true;
        ConversationViewActivity.openConversationView(this,conversation);
    }

    private void openConversationView(ConversationListAdapter adapter, int index){
        Conversation conversation = adapter.getConversationOfIndex(index);
        openConversationView(conversation);
    }

}
