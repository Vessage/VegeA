package cn.bahamut.vessage.main;

import android.content.Context;

import java.util.Date;

import cn.bahamut.common.DateHelper;
import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/5/14.
 */
public class AppUtil {
    public static String dateToFriendlyString(Context context, Date date) {
        String friendlyDateString = "";
        long miniutsBeforeNow = (new Date().getTime() - date.getTime()) / 60000;
        if(miniutsBeforeNow > 7 * 60 * 24){
            friendlyDateString = DateHelper.toLocalDateTimeSimpleString(date);
        }else if(miniutsBeforeNow > 60 * 24){
            friendlyDateString = String.format(context.getResources().getString(R.string.x_days_ago),miniutsBeforeNow / (60 * 24));
        }else if(miniutsBeforeNow > 60){
            friendlyDateString = String.format(context.getResources().getString(R.string.x_hours_ago),miniutsBeforeNow / 60);
        }else if(miniutsBeforeNow > 1){
            friendlyDateString = String.format(context.getResources().getString(R.string.x_minutes_ago),miniutsBeforeNow);
        }else {
            friendlyDateString = context.getResources().getString(R.string.just_now);
        }
        return friendlyDateString;
    }
}
