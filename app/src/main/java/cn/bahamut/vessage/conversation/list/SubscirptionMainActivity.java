package cn.bahamut.vessage.conversation.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cn.bahamut.vessage.activities.sns.SNSMainActivity;
import cn.bahamut.vessage.activities.sns.SNSPostManager;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
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
        setTitle(R.string.nav_subscription_account_title);
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
        } else {
            adapter.notifyDataSetChanged();
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
        TextView subscriptionButton;

        public ViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_img_view);
            headline = (TextView) itemView.findViewById(R.id.headline_text);
            subline = (TextView) itemView.findViewById(R.id.subline_text);
            subscriptionButton = (TextView) itemView.findViewById(R.id.subscription_btn);
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
            boolean subscripted = conversation != null && StringHelper.isStringNullOrWhiteSpace(conversation.activityId);
            updateSubscriptButton(holder, subscripted, listener);
        }

        @Override
        public int getItemCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }
    }

    private void updateSubscriptButton(ViewHolder holder, boolean subscripted, View.OnClickListener listener) {
        if (subscripted) {
            holder.subscriptionButton.setOnClickListener(null);
            holder.subscriptionButton.setBackgroundResource(R.drawable.check_blue);
        } else {
            holder.subscriptionButton.setOnClickListener(listener);
            holder.subscriptionButton.setBackgroundResource(R.drawable.add_gray);

        }
    }

    private void subscriptAccount(SubscriptionAccount account, ViewHolder holder) {
        Map<String, Object> info = new HashMap<>();
        info.put("userType", VessageUser.TYPE_SUBSCRIPTION);
        conversationService.openConversationByUserInfo(account.id, info);
        ExtraActivitiesService acService = ServicesProvider.getService(ExtraActivitiesService.class);
        acService.setActivityMiniBadgeShow(SNSPostManager.ACTIVITY_ID);
        acService.setActivityBadgeNotified();
        updateSubscriptButton(holder, true, null);
    }

    private void showSAccountSNS(SubscriptionAccount account) {
        Intent intent = new Intent();
        intent.putExtra(SNSMainActivity.SPEC_USER_ID_KEY, account.id);
        intent.putExtra(SNSMainActivity.SPEC_USER_NICK_KEY, account.title);
        intent.putExtra(SNSMainActivity.USER_PAGE_MODE_KEY, true);
        intent.putExtra(SNSMainActivity.CAN_SUBSCRIPTION_KEY, true);
        ExtraActivitiesActivity.startExtraActivity(this, SNSPostManager.ACTIVITY_ID, intent);
    }
}
