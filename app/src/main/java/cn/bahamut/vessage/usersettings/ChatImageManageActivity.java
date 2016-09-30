package cn.bahamut.vessage.usersettings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.list.ConversationListActivity;

public class ChatImageManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_manage);
        getSupportActionBar().setTitle(R.string.my_face_chat_images);
    }

    public static void show(Context context, int openIndex) {
        Intent intent = new Intent(context,ChatImageManageActivity.class);
        intent.putExtra("openIndex",openIndex);
        context.startActivity(intent);
    }
}
