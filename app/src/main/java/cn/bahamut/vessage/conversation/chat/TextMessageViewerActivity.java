package cn.bahamut.vessage.conversation.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppUtil;

public class TextMessageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_text_message_viewer);
        String content = getIntent().getStringExtra("content");
        ((TextView) findViewById(R.id.content_text_view)).setText(content);
        long time = getIntent().getLongExtra("date", 0);
        Date date = time == 0 ? null : new Date(time);
        ((TextView) findViewById(R.id.date_tv)).setText(AppUtil.dateToFriendlyString(this, date));
        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static void showTextMessageViewerActivity(Context context, String content, Date date) {
        Intent intent = new Intent(context, TextMessageViewerActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("date", date == null ? 0 : date.getTime());
        context.startActivity(intent);
    }
}
