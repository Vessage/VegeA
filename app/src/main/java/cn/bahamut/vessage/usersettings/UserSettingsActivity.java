package cn.bahamut.vessage.usersettings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.services.UserService;

/**
 * Created by alexchow on 16/5/14.
 */
public class UserSettingsActivity extends AppCompatActivity {


    private static final int CHANGE_MOBILE_REQUEST_ID = 2;
    private static final int CHANGE_NICK_NAME_CODE_REQUEST_ID = 3;

    private ListView listView;

    protected static class SettingItemModel {
        public int iconResId;
        public String headLine;
        public boolean showNextIcon;
    }

    //ViewHolder静态类
    protected static class ViewHolder
    {
        public ImageView avatar;
        public TextView headline;
        public ImageView nextIcon;
    }

    class UserSettingsAdapter extends BaseAdapter{
        private final Context context;
        protected List<SettingItemModel> settings;
        protected LayoutInflater mInflater = null;
        protected List<View> convertViews = new ArrayList<>();

        UserSettingsAdapter(Context context,List<SettingItemModel> settings){
            super();
            this.settings = settings;
            this.context = context;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return settings.size();
        }

        @Override
        public Object getItem(int position) {
            return settings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertViews.size() > position){
                convertViews.get(position);
                holder = (ViewHolder) convertView.getTag();
            }else{
                convertView = mInflater.inflate(R.layout.user_settings_item, null);
                holder = new ViewHolder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatarImageView);
                holder.headline = (TextView) convertView.findViewById(R.id.headlineTextView);
                holder.nextIcon = (ImageView)convertView.findViewById(R.id.nextMark);
                convertView.setTag(holder);
            }
            SettingItemModel model = settings.get(position);
            holder.nextIcon.setVisibility(model.showNextIcon ? View.VISIBLE : View.INVISIBLE);
            holder.headline.setText(model.headLine);
            holder.avatar.setImageResource(model.iconResId);
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init(){
        listView = (ListView) findViewById(R.id.setting_list_view);
        listView.setOnItemClickListener(onClickSettingItem);
        VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        setTitle(String.format("%s:%s",getResources().getString(R.string.account),me.accountId));
        String mobileText = LocalizedStringHelper.getLocalizedString(R.string.not_bind_mobile);
        if(!StringHelper.isStringNullOrWhiteSpace(me.mobile)){
            if(StringHelper.isMobileNumber(me.mobile)){
                mobileText = String.format("%s***%s",me.mobile.substring(0,3),me.mobile.substring(7));
            }
        }
        List<SettingItemModel> settings = new LinkedList<>();
        SettingItemModel settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.avatar);
        settingItemModel.iconResId = R.mipmap.camera;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.nick) + ": " + me.nickName;
        settingItemModel.iconResId = R.mipmap.setting_nick;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.change_chat_bcg);
        settingItemModel.iconResId = R.mipmap.setting_chat_bcg;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.change_password);
        settingItemModel.iconResId = R.mipmap.setting_lock;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.mobile) + ": " + mobileText;
        settingItemModel.iconResId = R.mipmap.setting_mobile;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.logout);
        settingItemModel.iconResId = R.mipmap.setting_logout;
        settingItemModel.showNextIcon = false;
        settings.add(settingItemModel);

        UserSettingsAdapter adapter = new UserSettingsAdapter(this,settings);

        listView.setAdapter(adapter);
    }

    private AdapterView.OnItemClickListener onClickSettingItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
                case 0:changeAvatar();break;
                case 1:changeNick();break;
                case 2:changeChatBackground();break;
                case 3:changePassword();break;
                case 4:changeMobile();break;
                case 5:logout();break;
            }
        }
    };

    private void changeAvatar() {
        Intent intent = new Intent(UserSettingsActivity.this, ChangeAvatarActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            return;
        }
        switch (requestCode){
            case CHANGE_MOBILE_REQUEST_ID:handleChangeMobile(resultCode);break;
            case CHANGE_NICK_NAME_CODE_REQUEST_ID:handleChangeNickName(data);break;
        }
    }

    private void handleChangeNickName(Intent data) {
        if(data == null){
            return;
        }
        UserService userService = ServicesProvider.getService(UserService.class);
        String newNick = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
        if(StringHelper.isStringNullOrEmpty(newNick)){
            Toast.makeText(this, R.string.nick_cant_null,Toast.LENGTH_SHORT).show();
            return;
        }
        if(newNick.equals(userService.getMyProfile().nickName)){
            Toast.makeText(this, R.string.same_nick,Toast.LENGTH_SHORT).show();
            return;
        }
        ServicesProvider.getService(UserService.class).changeMyNickName(newNick, new UserService.ChangeNickCallback() {
            @Override
            public void onChangeNick(boolean isDone) {
                if(isDone){
                    init();
                    ProgressHUDHelper.showHud(UserSettingsActivity.this,R.string.change_nick_suc,R.mipmap.check_mark,true);
                }else {
                    ProgressHUDHelper.showHud(UserSettingsActivity.this,R.string.change_nick_fail,R.mipmap.cross_mark,true);
                }
            }
        });
    }

    private void handleChangeMobile(int resultCode) {
        if(resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS){
            init();
            ProgressHUDHelper.showHud(UserSettingsActivity.this,R.string.change_mobile_suc,R.mipmap.check_mark,true);
        }else {
            ProgressHUDHelper.showHud(UserSettingsActivity.this,R.string.change_mobile_cancel,R.mipmap.cross_mark,true);
        }
    }


    private void changeChatBackground() {
        Intent intent = new Intent(UserSettingsActivity.this, ChangeChatBackgroundActivity.class);
        startActivity(intent);
    }

    private void changeNick() {
        VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        EditPropertyActivity.showEditPropertyActivity(this, CHANGE_NICK_NAME_CODE_REQUEST_ID, R.string.change_nick,me.nickName);
    }

    private void changePassword() {
        Intent intent = new Intent(UserSettingsActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void changeMobile() {
        ValidateMobileActivity.startRegistMobileActivity(this,CHANGE_MOBILE_REQUEST_ID);
    }

    private void logout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingsActivity.this);
        builder.setTitle(R.string.logout_hint);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserSetting.setUserLogout();
                ServicesProvider.userLogout();
                AppMain.startSignActivity(UserSettingsActivity.this);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        builder.show();


    }

}
