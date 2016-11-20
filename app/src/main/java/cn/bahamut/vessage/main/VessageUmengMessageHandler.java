package cn.bahamut.vessage.main;

import android.app.Activity;
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
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/4/30.
 */
public class VessageUmengMessageHandler extends UmengMessageHandler {
    public static final int BUILDER_ID_DEFAULT = 0;
    public static final int BUILDER_ID_NEW_VESSAGE = 1;
    public static final int BUILDER_ID_ACTIVITY_UPDATED = 2;

    @Override
    public void dealWithCustomMessage(Context context, UMessage uMessage) {
        super.dealWithCustomMessage(context, uMessage);
    }

    @Override
    public Notification getNotification(Context context, UMessage umsg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.view_notification_view);
        Activity ca = AppMain.getCurrentActivity();
        switch (umsg.builder_id) {
            case BUILDER_ID_DEFAULT:
                myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(umsg.title));
                myNotificationView.setTextViewText(R.id.notification_text, LocalizedStringHelper.getLocalizedString(umsg.text));
                break;
            case BUILDER_ID_NEW_VESSAGE:
                if(ca != null){
                    ca.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                ServicesProvider.getService(VessageService.class).newVessageFromServer();
                            }catch (Exception e){

                            }
                        }
                    });
                }

                String msgText = LocalizedStringHelper.getLocalizedString(R.string.new_msg);
                String title = null;
                String msgTips = null;
                String nick = null;
                if (umsg.extra != null){
                    msgTips = umsg.extra.containsKey("tips") ? umsg.extra.get("tips") : null;
                    nick = umsg.extra.containsKey("nick") ? umsg.extra.get("nick") : null;
                    title = umsg.extra.containsKey("title") ? umsg.extra.get("title") : null;
                }
                try{
                    String noteName = ServicesProvider.getService(UserService.class).getUserNotedName(umsg.text);
                    if (!StringHelper.isStringNullOrWhiteSpace(noteName)){
                        nick = noteName;
                    }
                }catch (Exception e){
                }

                if (StringHelper.isStringNullOrWhiteSpace(title)){
                    myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(R.string.app_name));
                }else {
                    myNotificationView.setTextViewText(R.id.notification_title, title);
                }
                if (!StringHelper.isStringNullOrWhiteSpace(nick) && !StringHelper.isStringNullOrWhiteSpace(msgTips)){
                    msgText = String.format("%s:%s",nick,msgTips);
                }else if (!StringHelper.isStringNullOrWhiteSpace(nick)) {
                    msgText = String.format(LocalizedStringHelper.getLocalizedString(R.string.new_msg_from), nick);
                }

                myNotificationView.setTextViewText(R.id.notification_text,msgText );
                break;
            case BUILDER_ID_ACTIVITY_UPDATED:
                if(ca!=null){
                    ca.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                ServicesProvider.getService(ExtraActivitiesService.class).getActivitiesBoardData();
                            }catch (Exception e){

                            }
                        }
                    });
                }
                String activityName = umsg.extra != null ? umsg.extra.get("acName") : null;
                String activityMsg = umsg.extra != null ? umsg.extra.get("acMsg") : null;
                if(StringHelper.isStringNullOrWhiteSpace(activityName)) {
                    myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(R.string.app_name));
                }else {
                    myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(activityName));
                }
                if(StringHelper.isStringNullOrWhiteSpace(activityMsg)) {
                    myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(R.string.extra_activity_updated));
                }else {
                    myNotificationView.setTextViewText(R.id.notification_title, LocalizedStringHelper.getLocalizedString(activityMsg));
                }
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
