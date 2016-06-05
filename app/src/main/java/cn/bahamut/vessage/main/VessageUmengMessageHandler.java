package cn.bahamut.vessage.main;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.activities.ExtraActivitiesService;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/4/30.
 */
public class VessageUmengMessageHandler extends UmengMessageHandler {
    public static final int BUILDER_ID_DEFAULT = 0;
    public static final int BUILDER_ID_NEW_VESSAGE = 1;
    public static final int BUILDER_ID_ACTIVITY_UPDATED = 2;

    @Override
    public Notification getNotification(Context context, UMessage umsg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
        switch (umsg.builder_id) {
            case BUILDER_ID_DEFAULT:
                myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(umsg.title));
                myNotificationView.setTextViewText(R.id.notification_text, LocalizedStringHelper.getLocalizedString(umsg.text));
                break;
            case BUILDER_ID_NEW_VESSAGE:
                VessageService vessageService = ServicesProvider.getService(VessageService.class);
                if(vessageService != null){
                    vessageService.newVessageFromServer();
                }
                myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(R.string.app_name));
                String msgText = LocalizedStringHelper.getLocalizedString(R.string.new_msg);
                ConversationService conversationService = ServicesProvider.getService(ConversationService.class);
                if(conversationService != null&& StringHelper.isStringNullOrEmpty(umsg.text) == false){
                    Conversation conversation = conversationService.getConversationByChatterId(umsg.text);
                    if(conversation != null){
                        msgText = String.format(LocalizedStringHelper.getLocalizedString(R.string.new_msg_from),conversation.noteName);
                    }else {
                        UserService userService = ServicesProvider.getService(UserService.class);
                        if(userService != null){
                            VessageUser user = userService.getUserById(umsg.text);
                            if(user != null){
                                msgText = String.format(LocalizedStringHelper.getLocalizedString(R.string.new_msg_from),user.nickName);
                            }
                        }
                    }
                }
                myNotificationView.setTextViewText(R.id.notification_text,msgText );
                break;
            case BUILDER_ID_ACTIVITY_UPDATED:
                ExtraActivitiesService extraActivitiesService = ServicesProvider.getService(ExtraActivitiesService.class);
                extraActivitiesService.getActivitiesBoardData();
                myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(R.string.app_name));
                myNotificationView.setTextViewText(R.id.notification_text,LocalizedStringHelper.getLocalizedString(R.string.extra_activity_updated));
                break;
            default:
                return super.getNotification(context, umsg);
        }
        builder.setContent(myNotificationView)
                .setSmallIcon(getSmallIconId(context, umsg))
                .setTicker(LocalizedStringHelper.getLocalizedString(umsg.ticker))
                .setAutoCancel(true);
        return builder.build();

    }
}
