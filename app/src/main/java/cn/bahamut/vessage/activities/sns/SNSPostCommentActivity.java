package cn.bahamut.vessage.activities.sns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPost;

public class SNSPostCommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_comment);
    }

    public static void showPostCommentActivity(Context context, SNSPost post){
        Intent intent = new Intent(context,SNSPostCommentActivity.class);
        intent.putExtra("postId",post.pid);
        context.startActivity(intent);
    }
}
