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
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.umeng.message.PushAgent;

import java.util.ArrayList;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.services.ConversationService;

public class ConversationListActivity extends AppCompatActivity {

    private static final int OPEN_CONTACT_REQUEST_ID = 1;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Test");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if(data == null){
            return;
        }
        switch (requestCode){
            case OPEN_CONTACT_REQUEST_ID:handleContactResult(data);
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
