package cn.bahamut.vessage.conversation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.bahamut.vessage.R;

public class ConversationViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_view);
        ((Button)findViewById(R.id.recordVideoButton)).setOnClickListener(onClickRecordButton);
        getSupportActionBar().setTitle("");
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
