package cn.bahamut.vessage.main;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Date;

import cn.bahamut.common.ContactHelper;
import cn.bahamut.common.DateHelper;
import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/5/14.
 */
public class AppUtil {
    public static String dateToFriendlyString(Context context, Date date) {
        if (date == null) {
            return LocalizedStringHelper.getLocalizedString(R.string.unknow_time);
        }
        String friendlyDateString = "";
        long miniutsBeforeNow = (new Date().getTime() - date.getTime()) / 60000;
        if(miniutsBeforeNow > 7 * 60 * 24){
            friendlyDateString = DateHelper.toLocalDateTimeSimpleString(date);
        }else if(miniutsBeforeNow > 60 * 24){
            friendlyDateString = String.format(context.getResources().getString(R.string.x_days_ago),String.valueOf(miniutsBeforeNow / (60 * 24)));
        }else if(miniutsBeforeNow > 60){
            friendlyDateString = String.format(context.getResources().getString(R.string.x_hours_ago),String.valueOf(miniutsBeforeNow / 60));
        }else if(miniutsBeforeNow > 1){
            friendlyDateString = String.format(context.getResources().getString(R.string.x_minutes_ago),String.valueOf(miniutsBeforeNow));
        }else {
            friendlyDateString = context.getResources().getString(R.string.just_now);
        }
        return friendlyDateString;
    }

    public static interface OnSelectContactPerson{
        void onSelectContactPerson(String mobile,String contact);
    }

    public static void selectContactPerson(final Context context, Uri uri, final OnSelectContactPerson onSelectContactPerson){
        // 得到ContentResolver对象
        ContentResolver cr = context.getContentResolver();
        // 取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        // 向下移动光标
        while (cursor.moveToNext()) {
            // 取得联系人名字
            int nameFieldColumnIndex = cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final String contact = cursor.getString(nameFieldColumnIndex);
            String[] phones = ContactHelper.getContactPhone(cr,cursor);
            final ArrayList<String> mobiles = new ArrayList<>();
            for (String phone : phones) {
                String phoneNumber = phone.replaceAll(" |-|\\+86","");
                if(phoneNumber.startsWith("86")){
                    phoneNumber = phoneNumber.substring(2);
                }
                if(ContactHelper.isMobilePhoneNumber(phoneNumber)){
                    mobiles.add(phoneNumber);
                }
            }
            if(mobiles.size() == 0){
                Toast.makeText(context,R.string.no_mobile_found,Toast.LENGTH_SHORT).show();
            }else {
                final CharSequence[] charSequences = mobiles.toArray(new String[0]);
                AlertDialog.Builder builder= new AlertDialog.Builder(context);

                builder.setTitle(contact)
                        .setItems(charSequences, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MobclickAgent.onEvent(context,"Vege_SelectContactMobile");
                                onSelectContactPerson.onSelectContactPerson(mobiles.get(which),contact);
                            }
                        }).show();

                for (String phone : phones) {
                    Log.i(contact,phone);
                }
            }


        }
    }
}
