package cn.bahamut.vessage.conversation;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.umeng.message.PushAgent;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.ValidateMobileActivity;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.Vessage;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;

public class ConversationListActivity extends AppCompatActivity {

    private static final int OPEN_CONTACT_REQUEST_ID = 1;
    private static final int CHANGE_MOBILE_REQUEST_ID = 2;
    private static final int CHANGE_NICK_NAME_CODE_REQUEST_ID = 3;
    private ListView conversationListView;
    private SearchView searchView;

    private ConversationListAdapter listAdapter;
    private ConversationListSearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushAgent.getInstance(getApplicationContext()).onAppStart();
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
        ServicesProvider.getService(ConversationService.class).addObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
        listAdapter.reloadConversations();
        setAsConversationList();
        VessageService vessageService = ServicesProvider.getService(VessageService.class);
        vessageService.addObserver(VessageService.NOTIFY_NEW_VESSAGES_RECEIVED,onNewVessagesReceived);
        vessageService.newVessageFromServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        menu.add(0,0,1,String.format("%s:%s",getResources().getString(R.string.account),me.accountId));
        menu.add(0,1,1,R.string.change_avatar);
        menu.add(0,2,1,R.string.change_chat_bcg);
        menu.add(0,3,1,String.format("%s(%s)",getResources().getString(R.string.change_nick),me.nickName));
        menu.add(0,4,1,R.string.change_password);
        menu.add(0,5,1,String.format("%s(%s)",getResources().getString(R.string.change_mobile),me.mobile));
        menu.add(0,6,1,R.string.logout);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1:changeAvatar();break;
            case 2:changeChatBackground();break;
            case 3:changeNick();break;
            case 4:changePassword();break;
            case 5:changeMobile();break;
            case 6:logout();break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeAvatar() {

    }

    private void changeChatBackground() {

    }

    private void changeNick() {
        VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        EditPropertyActivity.showEditPropertyActivity(this, CHANGE_NICK_NAME_CODE_REQUEST_ID,R.string.change_nick,me.nickName);
    }

    private void changePassword() {

    }

    private void changeMobile() {
        ValidateMobileActivity.startRegistMobileActivity(this,CHANGE_MOBILE_REQUEST_ID);
    }

    private void logout() {
        release();
        UserSetting.setUserLogout();
        ServicesProvider.userLogout();
        AppMain.startSignActivity(this);
    }

    private void release() {
        searchAdapter.release();
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
        if(resultModel.conversation != null){
            openConversationView(resultModel.conversation);
        }else if(resultModel.user != null){
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUser(resultModel.user);
            openConversationView(conversation);
        }else if(resultModel.mobile != null){
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByMobile(resultModel.mobile,resultModel.mobile);
            openConversationView(conversation);
        }
        listAdapter.reloadConversations();
    }

    private void openContactView(){
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
            case CHANGE_MOBILE_REQUEST_ID:handleChangeMobile(resultCode);break;
            case CHANGE_NICK_NAME_CODE_REQUEST_ID:handleChangeNickName(data);break;
        }
    }

    private void handleChangeNickName(Intent data) {
        if(data == null){
            ProgressHUDHelper.showHud(ConversationListActivity.this,R.string.cancel,R.mipmap.cross_mark,true);
        }
        String newNick = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
        ServicesProvider.getService(UserService.class).changeMyNickName(newNick, new UserService.UserUpdatedCallback() {
            @Override
            public void updated(VessageUser user) {
                ProgressHUDHelper.showHud(ConversationListActivity.this,R.string.change_nick_suc,R.mipmap.check_mark,true);
            }
        });
    }

    private void handleChangeMobile(int resultCode) {
        if(resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS){
            ProgressHUDHelper.showHud(ConversationListActivity.this,R.string.change_mobile_suc,R.mipmap.check_mark,true);
        }else {
            ProgressHUDHelper.showHud(ConversationListActivity.this,R.string.change_mobile_cancel,R.mipmap.cross_mark,true);
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
                if(ContactHelper.isMobilePhoneNumber(phone)){
                    mobiles.add(phone);
                }
            }
            final CharSequence[] charSequences = mobiles.toArray(new String[0]);
            AlertDialog.Builder builder= new AlertDialog.Builder(this);

            builder.setTitle(contact)
                    .setIcon(R.mipmap.default_avatar)
                    .setItems(charSequences, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
        Intent intent = new Intent();
        intent.putExtra("conversationId",conversation.conversationId);
        intent.setClass(ConversationListActivity.this, ConversationViewActivity.class);
        startActivity(intent);
    }

    private void openConversationView(ConversationListAdapter adapter, int index){
        Conversation conversation = adapter.getConversationOfIndex(index);
        openConversationView(conversation);
    }

}
