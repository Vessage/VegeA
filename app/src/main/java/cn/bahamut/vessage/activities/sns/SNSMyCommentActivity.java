package cn.bahamut.vessage.activities.sns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.bahamut.vessage.R;

public class SNSMyCommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_my_comment);
    }

    public static void showMyCommentActivity(Context context, int loadCount){
        Intent intent = new Intent(context,SNSMyCommentActivity.class);
        intent.putExtra("loadCount",loadCount);
        context.startActivity(intent);
    }
}
