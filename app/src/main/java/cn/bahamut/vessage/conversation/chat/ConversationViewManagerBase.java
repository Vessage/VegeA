package cn.bahamut.vessage.conversation.chat;

import android.content.Intent;
import android.view.View;

import java.util.Collection;

import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/9.
 */


public class ConversationViewManagerBase{

    private ConversationViewActivity conversationViewActivity;

    public void initManager(ConversationViewActivity activity) {
        this.conversationViewActivity = activity;
    }

    public ConversationViewActivity getConversationViewActivity() {
        return conversationViewActivity;
    }

    protected void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    protected void showView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public View findViewById(int resId) {
        return conversationViewActivity.findViewById(resId);
    }

    public boolean isGroupChat() {
        return getConversationViewActivity().isGroupChat();
    }

    public ChatGroup getChatGroup() {
        return conversationViewActivity.getChatGroup();
    }

    protected String getConversationTitle() {
        return conversationViewActivity.getConversationTitle();
    }

    public Conversation getConversation() {
        return conversationViewActivity.getConversation();
    }

    public void onChatGroupUpdated() {
    }

    public void onGroupedChatterUpdated(VessageUser chatter) {
    }

    public void onVessagesReceived(Collection<Vessage> vessages) {
    }

    public void onDestroy() {
    }

    public void onPause() {
    }

    public void onResume() {
    }
/*
    public void onSwitchToManager() {
        getConversationViewActivity().currentManager = this;
    }
*/
    public void onSwitchOut() {
    }

    public void onConfigurationChanged() {
    }

    public void onBackKeyPressed() {
    }

    public void sending(int progress) {
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
    }
}

