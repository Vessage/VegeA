package cn.bahamut.vessage.conversation.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.activities.sns.SNSPostManager;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.subsciption.SubscriptionAccount;
import cn.bahamut.vessage.services.subsciption.SubscriptionService;
import cn.bahamut.vessage.services.user.VessageUser;

public class SubscirptionMainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adapter adapter;

    private ConversationService conversationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscirption_main);
        setTitle(R.string.subscription_account);
        recyclerView = (RecyclerView) findViewById(R.id.subaccount_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        conversationService = ServicesProvider.getService(ConversationService.class);
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.data == null) {
            refreshData();
        }
    }

    private void refreshData() {
        ServicesProvider.getService(SubscriptionService.class).getOnlineSubscriptionAccounts(new SubscriptionService.GetSubscriptionAccountsCallback() {
            @Override
            public void onGetSubscriptionAccounts(SubscriptionAccount[] accounts) {
                if (accounts != null) {
                    adapter.data = new ArrayList<>();
                    for (SubscriptionAccount account : accounts) {
                        adapter.data.add(account);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView headline;
        TextView subline;
        Button subscriptionButton;

        public ViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_img_view);
            headline = (TextView) itemView.findViewById(R.id.headline_text);
            subline = (TextView) itemView.findViewById(R.id.subline_text);
            subscriptionButton = (Button) itemView.findViewById(R.id.subscription_btn);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        List<SubscriptionAccount> data;
        LayoutInflater layoutInflater = LayoutInflater.from(SubscirptionMainActivity.this);

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.subaccount_list_item, null);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final SubscriptionAccount item = data.get(position);
            ImageHelper.setImageByFileIdOnView(holder.avatar, item.avatar, R.drawable.nav_subaccount_icon);
            holder.headline.setText(item.title);
            holder.subline.setText(item.desc);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.subscription_btn:
                            subscriptAccount(item, holder);
                            break;
                        default:
                            showSAccountSNS(item);
                            break;
                    }
                }
            };
            holder.itemView.setOnClickListener(listener);
            Conversation conversation = conversationService.getConversationOfChatterId(item.id);
            if (conversation == null || StringHelper.isStringNullOrWhiteSpace(conversation.activityId) == false) {
                holder.subscriptionButton.setEnabled(true);
                holder.subscriptionButton.setOnClickListener(listener);
                holder.subscriptionButton.setText(R.string.subscript);
            } else {
                holder.subscriptionButton.setEnabled(false);
                holder.subscriptionButton.setOnClickListener(null);
                holder.subscriptionButton.setText(R.string.subscripted);
            }
        }

        @Override
        public int getItemCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }
    }

    private void subscriptAccount(SubscriptionAccount account, ViewHolder holder) {
        Map<String, Object> info = new HashMap<>();
        info.put("userType", VessageUser.TYPE_SUBSCRIPTION);
        conversationService.openConversationByUserInfo(account.id, info);
        holder.subscriptionButton.setOnClickListener(null);
        holder.subscriptionButton.setEnabled(false);
        holder.subscriptionButton.setText(R.string.subscripted);
    }

    private void showSAccountSNS(SubscriptionAccount account) {
        Intent intent = new Intent();
        intent.putExtra("specificUserId", account.id);
        intent.putExtra("specificUserNick", account.title);
        intent.putExtra("userPageMode", true);
        ExtraActivitiesActivity.startExtraActivity(this, SNSPostManager.ACTIVITY_ID, intent);
    }
}
