package cn.bahamut.vessage.conversation;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import cn.bahamut.common.MenuItemBadge;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import cn.bahamut.vessage.usersettings.UserSettingsActivity;

public class ConversationListActivity extends AppCompatActivity {

    private static final int OPEN_CONTACT_REQUEST_ID = 1;
    private static final String SHOW_WELCOME_ALERT = "SHOW_WELCOME_ALERT";
    private ListView conversationListView;
    private SearchView searchView;

    private ConversationListAdapter listAdapter;
    private ConversationListSearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        searchView = (SearchView)findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setOnCloseListener(onCloseSearchViewListener);
        searchView.setOnSearchClickListener(onSearchClickListener);
        conversationListView = (ListView) findViewById(R.id.conversation_lv);
        conversationListView.setOnItemClickListener(onListItemClick);
        conversationListView.setOnItemLongClickListener(onItemLongClick);
        listAdapter = new ConversationListAdapter(this);
        searchAdapter = new ConversationListSearchAdapter(this);
        searchAdapter.init();
        listAdapter.reloadConversations();
        setAsConversationList();

        ServicesProvider.getService(ConversationService.class).addObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        vessageService.addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED,onNewVessagesReceived);
        vessageService.newVessageFromServer();
        ServicesProvider.getService(ExtraActivitiesService.class).addObserver(ExtraActivitiesService.ON_ACTIVITIES_NEW_BADGES_UPDATED,onActivitiesBadgeUpdated);
        ServicesProvider.getService(ExtraActivitiesService.class).getActivitiesBoardData();

        String showWelcomeAlertKey = UserSetting.generateUserSettingKey(SHOW_WELCOME_ALERT);
        if(UserSetting.getUserSettingPreferences().getBoolean(showWelcomeAlertKey,true)){
            //UserSetting.getUserSettingPreferences().edit().putBoolean(showWelcomeAlertKey,false).commit();
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.welcome_alert_title)
                    .setMessage(R.string.welcome_alert_msg);
            builder.setPositiveButton(R.string.start_conversation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openContactView();
                }
            });
            builder.setCancelable(true);
            builder.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppMain.getInstance().tryRegistDeviceToken();
        AppMain.getInstance().checkAppLatestVersion(ConversationListActivity.this);
        ServicesProvider.getService(UserService.class).fetchActiveUsersFromServer(true);
        listAdapter.reloadConversations();
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
        MenuItemBadge.update(menu.getItem(0),R.mipmap.favorite,showBadge).getActionView().setOnClickListener(onClickMenuItemNewIntersting);
        MenuItemBadge.update(menu.getItem(1),R.mipmap.setting,false).getActionView().setOnClickListener(onClickMenuSetting);
        menu.add(1,2,1,R.string.tell_friends).setIcon(R.mipmap.share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 2){
            AppMain.getInstance().showTellVegeToFriendsAlert(LocalizedStringHelper.getLocalizedString(R.string.tell_friends_vege_msg));
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
        Intent intent = new Intent(ConversationListActivity.this, UserSettingsActivity.class);
        startActivity(intent);
    }

    private void showActivitiesList() {
        invalidateOptionsMenu();
        ServicesProvider.getService(ExtraActivitiesService.class).clearActivityBadgeNotify();
        Intent intent = new Intent(ConversationListActivity.this, ExtraActivitiesActivity.class);
        startActivity(intent);
    }

    private void release() {
        searchAdapter.release();
        ServicesProvider.getService(ExtraActivitiesService.class).deleteObserver(ExtraActivitiesService.ON_ACTIVITIES_NEW_BADGES_UPDATED,onActivitiesBadgeUpdated);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED,onNewVessagesReceived);
        ServicesProvider.getService(ConversationService.class).deleteObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
    }

    private void setAsSearchList(){
        conversationListView.setAdapter(this.searchAdapter);
        conversationListView.deferNotifyDataSetChanged();
    }

    private void setAsConversationList(){
        conversationListView.setAdapter(this.listAdapter);
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

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            searchAdapter.search(newText);
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
            for (Vessage vsg : vsgs) {
                boolean exists = false;
                for (int i = 0; i < loadedConversations.size(); i++) {
                    Conversation conversation = loadedConversations.get(i);
                    if(conversation.isInConversation(vsg)){
                        exists = true;
                    }
                }
                if(!exists){
                    Vessage.VessageExtraInfoModel infoModel = vsg.getExtraInfoModel();
                    conversationService.openConversationVessageInfo(vsg.sender,infoModel.getMobileHash(),infoModel.getNickName());
                }
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

    private ListView.OnItemLongClickListener onItemLongClick = new ListView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            if(conversationListView.getAdapter() == listAdapter){
                final int index = position - 1;
                PopupMenu popupMenu = new PopupMenu(ConversationListActivity.this,view);
                popupMenu.getMenu().add(R.string.remove);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ConversationListActivity.this)
                                .setTitle(R.string.ask_remove_conversation)
                                .setMessage(listAdapter.getConversationOfIndex(index).noteName)
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
                        return true;
                    }
                });
                popupMenu.show();
            }
            return true;
        }
    };

    private AdapterView.OnItemClickListener onListItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Adapter adapter = parent.getAdapter();
            if(adapter instanceof ConversationListAdapter){
                if(position > 0){
                    openConversationView((ConversationListAdapter)adapter,position - 1);
                }else{
                    openContactView();
                }
            }else if(adapter instanceof  ConversationListSearchAdapter) {
                openSearchResult((ConversationListSearchAdapter) adapter, position);
            }
        }
    };

    private void openSearchResult(ConversationListSearchAdapter adapter, int index){
        SearchManager.SearchResultModel resultModel = adapter.getSearchResult(index);
        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        setAsConversationList();
        if(resultModel.conversation != null){
            MobclickAgent.onEvent(ConversationListActivity.this,"Vege_OpenSearchResultConversation");
            openConversationView(resultModel.conversation);
        }else if(resultModel.user != null){
            String noteName = resultModel.user.nickName;
            if (StringHelper.isStringNullOrWhiteSpace(noteName)){
                noteName = resultModel.keyword;
            }
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(resultModel.user.userId,noteName);
            openConversationView(conversation);
        }else if(resultModel.mobile != null){
            MobclickAgent.onEvent(ConversationListActivity.this,"Vege_OpenSearchResultMobileConversation");
            openMobileConversation(resultModel.mobile,resultModel.mobile);
        }
    }

    private void openContactView(){
        MobclickAgent.onEvent(ConversationListActivity.this,"Vege_OpenContactView");
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
        }
    }


    private void handleContactResult(Intent data) {
        Uri uri = data.getData();
        AppUtil.selectContactPerson(this, uri, new AppUtil.OnSelectContactPerson() {
            @Override
            public void onSelectContactPerson(String mobile,String contact) {
                MobclickAgent.onEvent(ConversationListActivity.this,"Vege_SelectContactMobile");
                openMobileConversation(mobile,contact);
            }
        });
    }

    private void openMobileConversation(String mobile, final String noteName){
        VessageUser user = ServicesProvider.getService(UserService.class).getUserByMobile(mobile);
        if(user != null){
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(user.userId,noteName);
            openConversationView(conversation);
        }else {
            final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ConversationListActivity.this);
            ServicesProvider.getService(UserService.class).registNewUserByMobile(mobile, noteName, new UserService.UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    hud.dismiss();
                    if(user != null){
                        Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(user.userId,noteName);
                        ServicesProvider.getService(UserService.class).setUserNoteName(user.userId,noteName);
                        openConversationView(conversation);
                    }else {
                        Toast.makeText(ConversationListActivity.this,R.string.no_such_user,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        listAdapter.reloadConversations();
    }

    private void openConversationView(Conversation conversation){
        ConversationViewActivity.openConversationView(this,conversation);
    }

    private void openConversationView(ConversationListAdapter adapter, int index){
        Conversation conversation = adapter.getConversationOfIndex(index);
        openConversationView(conversation);
    }

}
