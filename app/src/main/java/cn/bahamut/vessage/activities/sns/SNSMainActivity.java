package cn.bahamut.vessage.activities.sns;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.sns.model.SNSPost;

public class SNSMainActivity extends AppCompatActivity {

    private RecyclerView postListView;
    private SNSPostAdapter adapter;
    private TextView homeButton;
    private TextView myPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_activity_snsmain);
        SNSPostManager.getInstance().initManager();
        getSupportActionBar().setTitle(R.string.sns);
        postListView = (RecyclerView) findViewById(R.id.post_list_view);
        adapter = new SNSPostAdapter(this);
        postListView.setAdapter(adapter);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        postListView.setLayoutManager(lm);
        findViewById(R.id.new_post_btn).setOnClickListener(onClickBottomView);
        homeButton = (TextView) findViewById(R.id.home_btn);
        homeButton.setOnClickListener(onClickBottomView);
        myPostButton = (TextView) findViewById(R.id.my_post_btn);
        myPostButton.setOnClickListener(onClickBottomView);

        postListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0){
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int totalItemCount = layoutManager.getItemCount();

                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!adapter.isLoadingMore() && totalItemCount < (lastVisibleItem + 3)) {
                    adapter.loadMorePosts();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.isInited() == false){
            refreshPost();
        }
    }

    private void refreshPost() {
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(this);
        adapter.refreshPosts(new SNSPostAdapter.RefreshPostCallback() {
            @Override
            public void onRefreshCompleted(int received) {
                hud.dismiss();
                if (received < 0){
                    Toast.makeText(SNSMainActivity.this,R.string.get_sns_data_error,Toast.LENGTH_SHORT).show();
                }else if (received == 0){
                    Toast.makeText(SNSMainActivity.this,R.string.no_sns_posts,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SNSPostManager.getInstance().releaseManager();
    }

    private View.OnClickListener onClickBottomView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.new_post_btn:
                    postNewSNSPost();
                    break;
                case R.id.home_btn:
                    switchHomePage();
                    break;
                case R.id.my_post_btn:
                    switchMyPostsPage();
                    break;
            }
        }
    };

    private void refreshBottomButton() {
        ColorStateList tmp = myPostButton.getTextColors();
        myPostButton.setTextColor(homeButton.getTextColors());
        homeButton.setTextColor(tmp);
    }

    private void switchMyPostsPage() {
        switchPostTypePage(SNSPost.TYPE_MY_POST);
    }

    private void switchPostTypePage(int type) {
        if (type != adapter.getPostType()){
            adapter.setPostType(type);
            refreshBottomButton();
        }else {
            if (adapter.getItemCount() <= 1 ){
                refreshPost();
            }
        }
        if (type == SNSPost.TYPE_MY_POST){
            getSupportActionBar().setTitle(R.string.my_post);
        }else {
            getSupportActionBar().setTitle(R.string.sns);
        }
    }

    private void switchHomePage() {
        switchPostTypePage(SNSPost.TYPE_NORMAL_POST);
    }

    private void postNewSNSPost() {
        Toast.makeText(this,"Building",Toast.LENGTH_SHORT).show();
    }
}
