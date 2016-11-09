package cn.bahamut.vessage.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.activities.ExtraActivitiesActivity;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;

/**
 * Created by alexchow on 16/6/5.
 */
public class VessageUmengNotificationClickHandler extends UmengNotificationClickHandler {
    @Override
    public void dealWithCustomAction(Context context, UMessage msg) {
        if(msg.custom.equals("OtherDeviceLogin")){
            onOtherDeviceLogin();
        }else if(msg.builder_id == VessageUmengMessageHandler.BUILDER_ID_NEW_VESSAGE){
            ConversationService conversationService = ServicesProvider.getService(ConversationService.class);
            if(conversationService != null && StringHelper.isNullOrEmpty(msg.text) == false) {
                Conversation conversation = conversationService.getConversationByChatterId(msg.text);
                if (conversation != null && AppMain.getCurrentActivity() != null) {
                    ConversationViewActivity.openConversationView(AppMain.getCurrentActivity(), conversation.conversationId, Intent.FLAG_ACTIVITY_NEW_TASK);
                    return;
                }
            }
            launchApp(AppMain.getInstance(),msg);
        }else if(msg.builder_id == VessageUmengMessageHandler.BUILDER_ID_ACTIVITY_UPDATED){
            if(AppMain.getCurrentActivity() != null){
                Intent intent = new Intent(AppMain.getCurrentActivity(), ExtraActivitiesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppMain.getInstance().startActivity(intent);
                return;
            }
            launchApp(AppMain.getInstance(),msg);
        }
    }

    private void onOtherDeviceLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppMain.getInstance().getApplicationContext());
        builder.setTitle(R.string.other_device_logon);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.setUserLogout();
                ServicesProvider.userLogout();
                Intent intent = new Intent(AppMain.getInstance().getApplicationContext(),EntryActivity.class);
                AppMain.getInstance().getApplicationContext().startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
