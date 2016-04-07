package cn.bahamut.vessage.conversation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.models.Conversation;
import cn.bahamut.vessage.models.Vessage;
import cn.bahamut.vessage.services.ConversationService;
import io.realm.RealmObject;

public class ConversationViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_view);
        ((Button)findViewById(R.id.recordVideoButton)).setOnClickListener(onClickRecordButton);
        String conversationId = savedInstanceState.getString("conversationId");
        if(conversationId == null){
            finish();
        }else{
            Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversation(conversationId);
            setActivityTitle(conversation.noteName);

        }
    }

    private void setActivityTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    private View.OnClickListener onClickRecordButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ConversationViewActivity.this,RecordVessageActivity.class);
            startActivity(intent);
        }
    };
}
