package cn.bahamut.vessage.usersettings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.AndroidHelper;
import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.AppService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 16/5/14.
 */
public class UserSettingsActivity extends AppCompatActivity {


    private static final int CHANGE_MOBILE_REQUEST_ID = 2;
    private static final int CHANGE_NICK_NAME_CODE_REQUEST_ID = 3;
    private static final int CHANGE_MOTTO_CODE_REQUEST_ID = 4;

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
                convertView = mInflater.inflate(R.layout.usersetting_setting_item, null);
                holder = new ViewHolder();
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar_img_view);
                holder.headline = (TextView) convertView.findViewById(R.id.headline_text);
                holder.nextIcon = (ImageView)convertView.findViewById(R.id.next_mark);
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
        setContentView(R.layout.usersetting_activity_user_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1,1,1, String.format("%s(%s)",LocalizedStringHelper.getLocalizedString(R.string.check_app_update),AndroidHelper.getVersion(this)));
        menu.add(1,2,1,R.string.feedback);
        //menu.add(1,3,1,R.string.vote_me_up);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1:checkUpdate();break;
            case 2:sendFeedbackMail();break;
            case 3:voteApp();break;
        }
        return super.onContextItemSelected(item);
    }

    private void checkUpdate() {
        ServicesProvider.getService(AppService.class).checkAppLatestVersion(UserSettingsActivity.this,true);
    }

    private void voteApp() {

    }

    private void sendFeedbackMail() {
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse("mailto:bahamut-sharelink@outlook.com"));
        data.putExtra(Intent.EXTRA_SUBJECT, LocalizedStringHelper.getLocalizedString(R.string.feedback_mail_subject));
        startActivity(data);
    }

    private void init(){
        listView = (ListView) findViewById(R.id.setting_list_view);
        listView.setOnItemClickListener(onClickSettingItem);
        UserService userService = ServicesProvider.getService(UserService.class);
        VessageUser me = userService.getMyProfile();
        setTitle(String.format("%s:%s",getResources().getString(R.string.account),me.accountId));
        String mobileText = LocalizedStringHelper.getLocalizedString(R.string.not_bind_mobile);
        if (userService.isMyMobileValidated() && !userService.isUsingTempMobile()){
            mobileText = String.format("%s***%s",me.mobile.substring(0,3),me.mobile.substring(7));
        }

        List<SettingItemModel> settings = new ArrayList<>();
        SettingItemModel settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.avatar);
        settingItemModel.iconResId = R.drawable.camera;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.nick) + ": " + me.nickName;
        settingItemModel.iconResId = R.drawable.setting_nick;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();

        if (me.sex > 0) {
            settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.sex_male);
        } else if (me.sex < 0) {
            settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.sex_female);
        } else {
            settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.sex_middle);
        }
        settingItemModel.iconResId = R.drawable.setting_sex;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.motto) + ": " +
                (StringHelper.isStringNullOrWhiteSpace(me.motto) ? LocalizedStringHelper.getLocalizedString(R.string.default_motto) : me.motto);
        settingItemModel.iconResId = R.drawable.setting_motto;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.change_password);
        settingItemModel.iconResId = R.drawable.setting_lock;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.mobile) + ": " + mobileText;
        settingItemModel.iconResId = R.drawable.setting_mobile;
        settingItemModel.showNextIcon = true;
        settings.add(settingItemModel);

        settingItemModel = new SettingItemModel();
        settingItemModel.headLine = LocalizedStringHelper.getLocalizedString(R.string.logout);
        settingItemModel.iconResId = R.drawable.setting_logout;
        settingItemModel.showNextIcon = false;
        settings.add(settingItemModel);

        UserSettingsAdapter adapter = (UserSettingsAdapter) listView.getAdapter();

        if (adapter == null) {
            adapter = new UserSettingsAdapter(this, settings);
            listView.setAdapter(adapter);
        } else {
            adapter.settings = settings;
            adapter.notifyDataSetChanged();
        }
    }

    private AdapterView.OnItemClickListener onClickSettingItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
                case 0:changeAvatar();break;
                case 1:changeNick();break;
                case 2:
                    changeSex();
                    break;
                case 3:
                    changeMotto();
                    break;
                case 4:
                    changePassword();
                    break;
                case 5:
                    changeMobile();
                    break;
                case 6:
                    logout();
                    break;
            }
        }
    };

    private void changeMotto() {
        VessageUser me = ServicesProvider.getService(UserService.class).getMyProfile();
        EditPropertyActivity.showEditPropertyActivity(this, CHANGE_MOTTO_CODE_REQUEST_ID, R.string.change_motto, me.motto);
    }

    private void changeSex() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingsActivity.this);
        String[] genderList = new String[]{
                LocalizedStringHelper.getLocalizedString(R.string.female),
                LocalizedStringHelper.getLocalizedString(R.string.male),
                LocalizedStringHelper.getLocalizedString(R.string.no_male),
        };

        builder.setTitle(R.string.sel_your_sex);
        builder.setItems(genderList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        setSex(-80);
                        break;
                    case 1:
                        setSex(80);
                        break;
                    case 2:
                        setSex(0);
                        break;
                }
            }
        });
        builder.show();
    }

    private void setSex(int sex) {
        ServicesProvider.getService(UserService.class).changeMySex(sex, new UserService.ChangeValueReturnBooleanCallback() {
            @Override
            public void onChanged(boolean isChanged) {
                if (isChanged) {
                    init();
                    ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_sex_suc, R.drawable.check_mark, true);
                } else {
                    ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_sex_err, R.drawable.cross_mark, true);
                }
            }
        });
    }

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
            case CHANGE_MOTTO_CODE_REQUEST_ID:
                handleChangeMotto(data);
                break;
            default:
                break;
        }
    }

    private void handleChangeMotto(Intent data) {
        String newMotto = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
        ServicesProvider.getService(UserService.class).changeMyMotto(newMotto, new UserService.ChangeValueReturnBooleanCallback() {
            @Override
            public void onChanged(boolean isChanged) {
                if (isChanged) {
                    init();
                    ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_motto_suc, R.drawable.check_mark, true);
                } else {
                    ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_motto_err, R.drawable.cross_mark, true);
                }
            }
        });
    }

    private void handleChangeNickName(Intent data) {
        if(data == null){
            return;
        }
        UserService userService = ServicesProvider.getService(UserService.class);
        String newNick = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
        if(StringHelper.isNullOrEmpty(newNick)){
            Toast.makeText(this, R.string.nick_cant_null,Toast.LENGTH_SHORT).show();
            return;
        }
        if(newNick.equals(userService.getMyProfile().nickName)){
            Toast.makeText(this, R.string.same_nick,Toast.LENGTH_SHORT).show();
            return;
        }
        ServicesProvider.getService(UserService.class).changeMyNickName(newNick, new UserService.ChangeValueReturnBooleanCallback() {
            @Override
            public void onChanged(boolean isDone) {
                if(isDone){
                    init();
                    ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_nick_suc, R.drawable.check_mark, true);
                }else {
                    ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_nick_fail, R.drawable.cross_mark, true);
                }
            }
        });
    }

    private void handleChangeMobile(int resultCode) {
        if(resultCode == ValidateMobileActivity.RESULT_CODE_VALIDATE_SUCCESS){
            init();
            ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_mobile_suc, R.drawable.check_mark, true);
        }else {
            ProgressHUDHelper.showHud(UserSettingsActivity.this, R.string.change_mobile_cancel, R.drawable.cross_mark, true);
        }
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
        ValidateMobileActivity.startRegistMobileActivity(this, CHANGE_MOBILE_REQUEST_ID, false);
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
