package cn.bahamut.common;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by alexchow on 16/4/18.
 */
public class ContactHelper {

    static public boolean isMobilePhoneNumber(String number){
        return number.matches("^((13[0-9])|(15[^4,\\D])|(18[0,2,5-9]))\\d{8}$");
    }

    //获取联系人电话
    static public String[] getContactPhone(ContentResolver contentResolver,Cursor cursor)
    {

        int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int phoneNum = cursor.getInt(phoneColumn);
        if (phoneNum > 0)
        {
            // 获得联系人的ID号
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idColumn);
            // 获得联系人的电话号码的cursor;
            Cursor phones = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + contactId,
                    null, null);
            int phoneCount = phones.getCount();
            ArrayList<String> allPhoneNum = new ArrayList<>(phoneCount);
            if (phones.moveToFirst())
            {
                // 遍历所有的电话号码
                for (;!phones.isAfterLast();phones.moveToNext())
                {
                    int index = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int typeindex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phone_type = phones.getInt(typeindex);
                    String phoneNumber = phones.getString(index).replaceAll(" |-|\\+86","");
                    if(phoneNumber.startsWith("86")){
                        phoneNumber = phoneNumber.substring(2);
                    }
                    allPhoneNum.add(phoneNumber);
                }
                if (!phones.isClosed())
                {
                    phones.close();
                }
                return allPhoneNum.toArray(new String[0]);
            }
        }
        return new String[0];
    }
}
