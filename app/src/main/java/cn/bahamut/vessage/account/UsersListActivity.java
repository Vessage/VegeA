package cn.bahamut.vessage.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.ConversationViewActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppUtil;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class UsersListActivity extends AppCompatActivity {

    public static final int USERS_LIST_ACTIVITY_MODE_SELECTION = 1;
    public static final int USERS_LIST_ACTIVITY_MODE_LIST = 2;
    public static final String SELECTED_USER_IDS_ARRAY_KEY = "SELECTED_USER_IDS_ARRAY_KEY";
    private static final int OPEN_CONTACT_REQUEST_ID = 3;
    private static final int OPEN_SELECT_NEAR_USER_REQUEST_ID = 4;
    private String myUserId;

    private boolean isAllowSelectSelf() {
        return getIntent().getBooleanExtra("allowSelectSelf",false);
    }

    private boolean canSelectNearUser() {
        return getIntent().getBooleanExtra("canSelectNearUser",false);
    }

    private boolean canSelectMobileUser() {
        return getIntent().getBooleanExtra("canSelectMobile",false);
    }

    private boolean isAllowMultiselection() {
        return getIntent().getBooleanExtra("allowMultiselection",false);
    }

    private boolean canSelectActiveUser() {
        return getIntent().getBooleanExtra("canSelectActiveUser",false);
    }

    private class UsersListAdapter extends BaseAdapter{
        private Context context;
        private boolean allowSelection;
        private boolean allowMutilSelection;
        private boolean canSelectNearUsers;
        private boolean canSelectMobileUser;
        private boolean canSelectActiveUsers;

        private int getUserStartIndex(){
            int index = 0;
            if(canSelectMobileUser) index++;
            if(canSelectActiveUsers) index++;
            if(canSelectNearUsers) index++;
            return index;
        }

        private Set<Integer> selectedIndexSet = new LinkedHashSet<>();
        private ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(allowSelection){
                    if(position == getMobileItemIndex()){
                        showContactActivity();
                    }else if(position == getActiveItemIndex()){
                        showActiveUserActivity();
                    } else if(position == getNearItemIndex()){
                        showNearUserActivity();
                    }else{
                        selectItem(position);
                    }
                }else {
                    VessageUser user = data.get(position - getUserStartIndex());
                    if(user != null){
                        Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUserInfo(user.userId,user.nickName);
                        ConversationViewActivity.openConversationView(context,conversation);
                    }else {
                        Toast.makeText(context,R.string.user_data_not_ready,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        public boolean selectItem(int position){
            if(allowSelection){
                int dataIndex = position - getUserStartIndex();
                boolean containIndex = selectedIndexSet.contains(dataIndex);
                if(allowMutilSelection){
                    if(containIndex){
                        selectedIndexSet.remove(dataIndex);
                    }
                }else {
                    selectedIndexSet.clear();
                }
                if(!containIndex){
                    selectedIndexSet.add(dataIndex);
                }
                notifyDataSetChanged();
                return true;
            }
            return false;
        }

        protected Context getContext() {
            return context;
        }

        public boolean isAllowSelection() {
            return allowSelection;
        }

        public void setAllowSelection(boolean allowSelection) {
            this.allowSelection = allowSelection;
        }

        public boolean isAllowMutilSelection() {
            return allowMutilSelection;
        }

        public void setAllowMutilSelection(boolean allowMutilSelection) {
            this.allowMutilSelection = allowMutilSelection;
        }

        public ListView.OnItemClickListener getOnItemClickListener() {
            return onItemClickListener;
        }

        //ViewHolder静态类
        protected class ViewHolder
        {
            public TextView title;
            public ImageView avatar;
            public TextView headline;
            public ImageView statusImage;
        }

        protected ArrayList<VessageUser> data;
        protected LayoutInflater mInflater = null;

        public void setData(List<String> userIds){
            UserService userService = ServicesProvider.getService(UserService.class);
            List<String> notLoadedId = new LinkedList<>();
            data = new ArrayList<>(userIds.size());
            for (String userId : userIds) {
                VessageUser user = userService.getUserById(userId);
                if(user != null){
                    data.add(user);
                }else {
                    user = new VessageUser();
                    user.userId = userId;
                    data.add(user);
                    notLoadedId.add(userId);
                }
            }
            notifyDataSetChanged();
            for (String uid : notLoadedId) {
                userService.fetchUserByUserId(uid);
            }
        }

        public UsersListAdapter(Context context){
            this.context = context;

            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if(data == null){
                return getUserStartIndex();
            }
            return data.size() + getUserStartIndex();
        }

        private int getMobileItemIndex(){
            if(canSelectMobileUser){
                return 0;
            }
            return -1;
        }

        private int getActiveItemIndex() {
            if(canSelectActiveUsers){
                return 1 + getMobileItemIndex();
            }
            return -1;
        }

        private int getNearItemIndex(){
            if (canSelectNearUsers){
                int defaultIndex = 0;
                if(canSelectMobileUser) defaultIndex++;
                if(canSelectActiveUsers) defaultIndex++;
                return defaultIndex;
            }
            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(position == getMobileItemIndex()){
                convertView = mInflater.inflate(R.layout.next_item,null);
                ((TextView)convertView.findViewById(R.id.title)).setText(R.string.select_mobile);
                return convertView;
            }else if(position == getActiveItemIndex()){
                convertView = mInflater.inflate(R.layout.next_item,null);
                ((TextView)convertView.findViewById(R.id.title)).setText(R.string.select_active_users);
                return convertView;
            }
            else if(position == getNearItemIndex()){
                convertView = mInflater.inflate(R.layout.next_item,null);
                ((TextView)convertView.findViewById(R.id.title)).setText(R.string.select_near);
                return convertView;
            }else {
                ViewHolder holder = null;
                //如果缓存convertView为空，则需要创建View
                if (convertView == null || ((ViewHolder) convertView.getTag()) == null) {
                    holder = new ViewHolder();
                    //根据自定义的Item布局加载布局
                    convertView = mInflater.inflate(R.layout.users_list_view_item, null);
                    holder.avatar = (ImageView) convertView.findViewById(R.id.avatar_img_view);
                    holder.headline = (TextView) convertView.findViewById(R.id.headline_text);
                    holder.statusImage = (ImageView) convertView.findViewById(R.id.status_img_view);
                    //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                VessageUser user = data.get(position - getUserStartIndex());
                ImageHelper.setImageByFileId(holder.avatar, user.avatar, R.mipmap.default_avatar);
                String noteName = ServicesProvider.getService(UserService.class).getUserNoteName(user.userId);
                holder.headline.setText(noteName);
                updateStatusImage(holder, position);
                return convertView;
            }
        }

        private void updateStatusImage(ViewHolder holder, int position) {
            if(isAllowSelection()){
                if(selectedIndexSet.contains(position - getUserStartIndex())){
                    holder.statusImage.setVisibility(View.VISIBLE);
                    holder.statusImage.setImageResource(R.mipmap.check_blue);
                }else {
                    holder.statusImage.setVisibility(View.INVISIBLE);
                }
            }else {
                holder.statusImage.setImageResource(R.mipmap.next);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return data.get(position - getUserStartIndex());
        }
    }

    private void showActiveUserActivity() {
        List<VessageUser> activeUsers = ServicesProvider.getService(UserService.class).getActiveUsers();
        ArrayList<String> userIds = new ArrayList<>(activeUsers.size());
        for (VessageUser user : activeUsers) {
            userIds.add(user.userId);
        }
        String title = LocalizedStringHelper.getLocalizedString(R.string.active_users);
        new UsersListActivity.ShowSelectUserActivityBuilder(this)
                .setUserIdList(userIds)
                .setAllowMultiselection(isAllowMultiselection())
                .setTitle(title)
                .showActivity();
    }

    private void showNearUserActivity() {
        List<VessageUser> nearUsers = ServicesProvider.getService(UserService.class).getNearUsers();
        ArrayList<String> userIds = new ArrayList<>(nearUsers.size());
        for (VessageUser nearUser : nearUsers) {
            userIds.add(nearUser.userId);
        }
        String title = LocalizedStringHelper.getLocalizedString(R.string.near_users);
        new UsersListActivity.ShowSelectUserActivityBuilder(this)
                .setUserIdList(userIds)
                .setAllowMultiselection(isAllowMultiselection())
                .setTitle(title)
                .showActivity();
    }

    private ListView usersListView;
    private UsersListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
        usersListView = (ListView) findViewById(R.id.users_lv);
        int mode = getIntent().getIntExtra("mode",0);
        listAdapter = new UsersListAdapter(this);
        ArrayList<String> userIdList = new ArrayList<>();
        if(mode == USERS_LIST_ACTIVITY_MODE_SELECTION){
            listAdapter.setAllowSelection(true);
            listAdapter.canSelectMobileUser = canSelectMobileUser();
            listAdapter.canSelectNearUsers = canSelectNearUser();
            listAdapter.canSelectActiveUsers = canSelectActiveUser();
            listAdapter.setAllowMutilSelection(isAllowMultiselection());

            userIdList = getIntent().getStringArrayListExtra("userIdList");
        }else if(mode == USERS_LIST_ACTIVITY_MODE_LIST){
            listAdapter.setAllowSelection(false);
            userIdList = getIntent().getStringArrayListExtra("userIdList");
        }
        usersListView.setAdapter(listAdapter);
        usersListView.setOnItemClickListener(listAdapter.getOnItemClickListener());
        ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onUserProfileUpdated);
        String title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);
        listAdapter.setData(userIdList);
    }


    @Override
    protected void onDestroy() {
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED,onUserProfileUpdated);
        super.onDestroy();
    }

    private Observer onUserProfileUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser) state.getInfo();
            if(user != null){
                for (int i = 0; i < listAdapter.data.size(); i++) {
                    VessageUser s = listAdapter.data.get(i);
                    if(user.userId.equals(s.userId)){
                        listAdapter.data.remove(i);
                        listAdapter.data.add(i,user);
                        listAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int mode = getIntent().getIntExtra("mode",0);
        if(mode == USERS_LIST_ACTIVITY_MODE_SELECTION){
            //menu.add(0,1,1,R.string.select_mobile).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0,2,1,R.string.confirm).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 2){
            if(listAdapter.selectedIndexSet.size() == 0){
                Toast.makeText(this,R.string.no_user_selected,Toast.LENGTH_LONG).show();
                return true;
            }
            finishActivity(USERS_LIST_ACTIVITY_MODE_SELECTION);
            Intent intent = new Intent();
            ArrayList<String> resultArray = new ArrayList<>();
            for (Integer integer : listAdapter.selectedIndexSet) {
                String userId = listAdapter.data.get(integer).userId;
                resultArray.add(userId);
            }
            intent.putStringArrayListExtra(SELECTED_USER_IDS_ARRAY_KEY,resultArray);
            setResult(USERS_LIST_ACTIVITY_MODE_SELECTION,intent);
            finish();
        }else if(item.getItemId() == 1){
            showContactActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showContactActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, OPEN_CONTACT_REQUEST_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_CONTACT_REQUEST_ID){
            handleContactResult(data);
        }else if(requestCode == OPEN_SELECT_NEAR_USER_REQUEST_ID && resultCode == USERS_LIST_ACTIVITY_MODE_SELECTION){
            handleSelectUserResult(data);
        }
    }

    private void handleSelectUserResult(Intent data) {
        if(data == null){
            return;
        }
        List<String> userIds = data.getStringArrayListExtra(SELECTED_USER_IDS_ARRAY_KEY);
        UserService userService = ServicesProvider.getService(UserService.class);
        for (String userId : userIds) {
            VessageUser user = userService.getUserById(userId);
            if(user != null){
                selectUser(user);
            }
        }
    }

    private void handleContactResult(final Intent data) {
        if(data == null){
            return;
        }
        Uri uri = data.getData();
        AppUtil.selectContactPerson(this, uri, new AppUtil.OnSelectContactPerson() {
            @Override
            public void onSelectContactPerson(String mobile,String contact) {
                VessageUser user = ServicesProvider.getService(UserService.class).getUserByMobile(mobile);

                if(user != null){
                    selectUser(user);
                }else {
                    ServicesProvider.getService(UserService.class).registNewUserByMobile(mobile, contact, new UserService.UserUpdatedCallback() {
                        @Override
                        public void updated(VessageUser user) {
                            if(user != null){
                                selectUser(user);
                            }else {
                                Toast.makeText(UsersListActivity.this,R.string.no_such_user,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }

    private void selectUser(VessageUser user) {
        if(user.userId.equals(myUserId) && !isAllowSelectSelf() ){
            Toast.makeText(UsersListActivity.this,R.string.cant_select_self,Toast.LENGTH_SHORT).show();
            return;
        }
        boolean userExists = false;
        for (int i = 0; i < listAdapter.data.size(); i++) {
            if(VessageUser.isTheSameUser(user,listAdapter.data.get(i))){
                listAdapter.selectItem(i);
                userExists = true;
                break;
            }
        }

        if(!userExists){
            listAdapter.data.add(user);
            listAdapter.notifyDataSetChanged();
            int selectedPosition = listAdapter.data.size() - 1;
            listAdapter.selectItem(selectedPosition);
            usersListView.smoothScrollToPosition(selectedPosition);
        }
    }

    public static class ShowSelectUserActivityBuilder{
        private Intent intent;
        private Activity context;
        public ShowSelectUserActivityBuilder(Activity context){
            this.context = context;
            intent = new Intent(context,UsersListActivity.class);
            intent.putExtra("mode",USERS_LIST_ACTIVITY_MODE_SELECTION);
        }

        public ShowSelectUserActivityBuilder setAllowMultiselection(boolean allowMultiselection){
            intent.putExtra("allowMultiselection",allowMultiselection);
            return this;
        }

        public ShowSelectUserActivityBuilder setCanSelectMobile(boolean canSelectMobile){
            intent.putExtra("canSelectMobile",canSelectMobile);
            return this;
        }

        public ShowSelectUserActivityBuilder setCanSelectNearUser(boolean canSelectNearUser){
            intent.putExtra("canSelectNearUser",canSelectNearUser);
            return this;
        }

        public ShowSelectUserActivityBuilder setCanSelectActiveUser(boolean canSelectActiveUser){
            intent.putExtra("canSelectActiveUser",canSelectActiveUser);
            return this;
        }

        public ShowSelectUserActivityBuilder setTitle(String title){
            intent.putExtra("title",title);
            return this;
        }

        public ShowSelectUserActivityBuilder setUserIdList(List<String> userIdList){
            ArrayList<String> userIdArrList = new ArrayList<>(userIdList);
            intent.putStringArrayListExtra("userIdList",userIdArrList);
            return this;
        }

        public ShowSelectUserActivityBuilder setConversationUserIdList(){
            String myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
            List<Conversation> conversations = ServicesProvider.getService(ConversationService.class).getAllConversations();
            ArrayList<String> userList = new ArrayList<>(conversations.size());
            for (Conversation conversation : conversations) {
                if(StringHelper.notStringNullOrEmpty(conversation.chatterId) && !myUserId.equals(conversation.chatterId)){
                    userList.add(conversation.chatterId);
                }
            }
            return setUserIdList(userList);
        }

        public void showActivity(){
            context.startActivityForResult(intent,USERS_LIST_ACTIVITY_MODE_SELECTION);
        }
    }

    public static void showUserListActivity(Activity context, Collection<String> userIdArray,String title){
        String myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
        Intent intent = new Intent(context,UsersListActivity.class);
        intent.putExtra("title",title);
        intent.putExtra("mode",USERS_LIST_ACTIVITY_MODE_LIST);
        userIdArray.remove(myUserId);
        ArrayList<String> arrayList = new ArrayList<>(userIdArray);
        intent.putStringArrayListExtra("userIdList",arrayList);
        context.startActivityForResult(intent,USERS_LIST_ACTIVITY_MODE_LIST);
    }
}
