package cn.bahamut.vessage.conversation;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.Vessage;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;
import cn.bahamut.vessage.usersettings.UserSettingsActivity;

public class ConversationListActivity extends AppCompatActivity {

    private static final int OPEN_CONTACT_REQUEST_ID = 1;
    private ListView conversationListView;
    private SearchView searchView;

    private ConversationListAdapter listAdapter;
    private ConversationListSearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        searchView = (SearchView)findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setOnCloseListener(onCloseSearchViewListener);
        searchView.setOnSearchClickListener(onSearchClickListener);
        conversationListView = (ListView) findViewById(R.id.conversationListView);
        conversationListView.setOnItemClickListener(onListItemClick);
        listAdapter = new ConversationListAdapter(this);
        searchAdapter = new ConversationListSearchAdapter(this);
        searchAdapter.init();
        listAdapter.reloadConversations();
        setAsConversationList();

        ServicesProvider.getService(ConversationService.class).addObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        vessageService.addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED,onNewVessagesReceived);
        vessageService.newVessageFromServer();
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
        menu.add(0,1,0,R.string.new_intersting)
                .setIcon(R.mipmap.favorite)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0,2,0,R.string.user_setting)
                .setIcon(R.mipmap.setting)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1:showActivitiesList();break;
            case 2:showUserSetting();break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserSetting() {
        Intent intent = new Intent(ConversationListActivity.this, UserSettingsActivity.class);
        startActivity(intent);
    }

    private void showActivitiesList() {
        Intent intent = new Intent(ConversationListActivity.this, ExtraActivitiesActivity.class);
        startActivity(intent);
    }

    private void release() {
        searchAdapter.release();
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
            MobclickAgent.onEvent(ConversationListActivity.this,"OpenSearchResultConversation");
            openConversationView(resultModel.conversation);
        }else if(resultModel.user != null){
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUser(resultModel.user);
            openConversationView(conversation);
        }else if(resultModel.mobile != null){
            MobclickAgent.onEvent(ConversationListActivity.this,"OpenSearchResultMobileConversation");
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByMobile(resultModel.mobile,resultModel.mobile);
            openConversationView(conversation);
        }
    }

    private void openContactView(){
        MobclickAgent.onEvent(ConversationListActivity.this,"OpenContactView");
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
        // 得到ContentResolver对象
        ContentResolver cr = getContentResolver();
        // 取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        // 向下移动光标
        while (cursor.moveToNext()) {
            // 取得联系人名字
            int nameFieldColumnIndex = cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final String contact = cursor.getString(nameFieldColumnIndex);
            String[] phones = ContactHelper.getContactPhone(getContentResolver(),cursor);
            final ArrayList<String> mobiles = new ArrayList<>();
            for (String phone : phones) {
                String phoneNumber = phone.replaceAll(" |-|\\+86","");
                if(phoneNumber.startsWith("86")){
                    phoneNumber = phoneNumber.substring(2);
                }
                if(ContactHelper.isMobilePhoneNumber(phoneNumber)){
                    mobiles.add(phoneNumber);
                }
            }
            final CharSequence[] charSequences = mobiles.toArray(new String[0]);
            AlertDialog.Builder builder= new AlertDialog.Builder(this);

            builder.setTitle(contact)
                    .setIcon(R.mipmap.default_avatar)
                    .setItems(charSequences, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobclickAgent.onEvent(ConversationListActivity.this,"SelectContactMobile");
                            String mobilePhone = mobiles.get(which);
                            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByMobile(mobilePhone,contact);
                            openConversationView(conversation);
                            listAdapter.reloadConversations();
                        }
                    }).show();

            for (String phone : phones) {
                Log.i(contact,phone);
            }

        }
    }

    private void openConversationView(Conversation conversation){
        ConversationViewActivity.openConversationView(this,conversation);
    }

    private void openConversationView(ConversationListAdapter adapter, int index){
        Conversation conversation = adapter.getConversationOfIndex(index);
        openConversationView(conversation);
    }

}
