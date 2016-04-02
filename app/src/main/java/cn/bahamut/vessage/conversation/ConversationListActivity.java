package cn.bahamut.vessage.conversation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.services.ConversationService;

public class ConversationListActivity extends AppCompatActivity {

    private ListView conversationListView;
    private ConversationListAdapter listAdapter;
    private ConversationListSearchAdapter searchAdapter;
    private SearchManager searchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        conversationListView = (ListView) findViewById(R.id.conversationListView);
        conversationListView.setOnItemClickListener(onListItemClick);
        listAdapter = new ConversationListAdapter(this);
        searchAdapter = new ConversationListSearchAdapter(this);
        searchManager = new SearchManager();
        searchManager.addObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
        ServicesProvider.getService(ConversationService.class).addObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
        listAdapter.reloadConversations();
        setAsConversationList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchManager.deleteObserver(SearchManager.NOTIFY_ON_SEARCH_RESULT_LIST_UPDATED, onSearchResultUpdated);
        ServicesProvider.getService(ConversationService.class).deleteObserver(ConversationService.NOTIFY_CONVERSATION_LIST_UPDATED, onConversationListUpdated);
    }

    private void setAsSearchList(){
        conversationListView.setAdapter(this.searchAdapter);
        conversationListView.deferNotifyDataSetChanged();
    }

    private void setAsConversationList(){
        conversationListView.setAdapter(this.listAdapter);
        if(conversationListView.getAdapter() instanceof ConversationListAdapter) {
            conversationListView.deferNotifyDataSetChanged();
        }
    }

    private Observer onConversationListUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            listAdapter.reloadConversations();
            if(conversationListView.getAdapter() instanceof ConversationListSearchAdapter) {
                conversationListView.deferNotifyDataSetChanged();
            }
        }
    };

    private Observer onSearchResultUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            searchAdapter.reloadResultList();
            conversationListView.deferNotifyDataSetChanged();
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
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByMobile(resultModel.mobile);
            openConversationView(conversation);
        }
    }

    private void openContactView(){

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
