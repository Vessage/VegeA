package cn.bahamut.vessage.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.ConversationViewActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.services.conversation.Conversation;
import cn.bahamut.vessage.services.conversation.ConversationService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class UsersListActivity extends AppCompatActivity {

    public static final int USERS_LIST_ACTIVITY_MODE_SELECTION = 1;
    public static final int USERS_LIST_ACTIVITY_MODE_LIST = 2;
    public static final String SELECTED_USER_IDS_ARRAY_KEY = "SELECTED_USER_IDS_ARRAY_KEY";
    private ArrayList<String> userIdList;

    private class UsersListAdapter extends BaseAdapter{
        private Context context;
        private boolean allowSelection;
        private boolean allowMutilSelection;
        private Set<Integer> selectedIndexSet = new LinkedHashSet<>();
        private ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(allowSelection){
                    if(allowMutilSelection){
                        selectedIndexSet.add(position);
                    }else {
                        selectedIndexSet.clear();
                        selectedIndexSet.add(position);
                    }
                    notifyDataSetChanged();
                }else {
                    VessageUser user = data.get(position);
                    if(user != null){
                        Conversation conversation = ServicesProvider.getService(ConversationService.class).openConversationByUser(user);
                        ConversationViewActivity.openConversationView(context,conversation);
                    }else {
                        Toast.makeText(context,R.string.user_data_not_ready,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

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
            public ImageView avatar;
            public TextView headline;
            public ImageView statusImage;
        }

        protected List<VessageUser> data;
        protected LayoutInflater mInflater = null;

        public void setData(List<String> userIds){
            UserService userService = ServicesProvider.getService(UserService.class);
            data = new ArrayList<>(userIds.size());
            for (String userId : userIds) {
                VessageUser user = userService.getUserById(userId);
                if(user != null){
                    data.add(user);
                }else {
                    user = new VessageUser();
                    user.userId = userId;
                    data.add(user);
                }
            }
        }

        public UsersListAdapter(Context context){
            this.context = context;

            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if(data == null){
                return 0;
            }
            return data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            //如果缓存convertView为空，则需要创建View
            if (convertView == null || ((ViewHolder) convertView.getTag()) == null) {
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.users_list_view_item, null);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatarImageView);
                holder.headline = (TextView) convertView.findViewById(R.id.headlineTextView);
                holder.statusImage = (ImageView)convertView.findViewById(R.id.statusImageView);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            VessageUser user = data.get(position);
            ImageHelper.setImageByFileId(holder.avatar, user.avatar, R.mipmap.default_avatar);
            String noteName = ServicesProvider.getService(UserService.class).getUserNoteName(user.userId);
            holder.headline.setText(noteName);
            updateStatusImage(holder,position);
            return convertView;
        }

        private void updateStatusImage(ViewHolder holder, int position) {
            if(isAllowSelection()){
                if(selectedIndexSet.contains(position)){
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
            return data.get(position);
        }
    }

    private ListView usersListView;
    private UsersListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        usersListView = (ListView) findViewById(R.id.usersListView);
        int mode = getIntent().getIntExtra("mode",0);
        listAdapter = new UsersListAdapter(this);
        if(mode == USERS_LIST_ACTIVITY_MODE_SELECTION){
            listAdapter.setAllowSelection(true);
            boolean multiSelection = getIntent().getBooleanExtra("allowMultiselection",false);
            listAdapter.setAllowMutilSelection(multiSelection);

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
                for (String s : userIdList) {
                    if(s.equals(user.userId)){
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
            menu.add(0,1,1,R.string.confirm).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(listAdapter.selectedIndexSet.size() == 0){
            Toast.makeText(this,R.string.no_user_selected,Toast.LENGTH_LONG).show();
            return true;
        }
        if(item.getItemId() == 1){
            finishActivity(USERS_LIST_ACTIVITY_MODE_SELECTION);
            Intent intent = new Intent();
            ArrayList<String> resultArray = new ArrayList<>();
            for (Integer integer : listAdapter.selectedIndexSet) {
                String userId = userIdList.get(integer);
                resultArray.add(userId);
            }
            intent.putStringArrayListExtra(SELECTED_USER_IDS_ARRAY_KEY,resultArray);
            setResult(USERS_LIST_ACTIVITY_MODE_SELECTION,intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void showSelectUserActivity(Activity context, boolean allowMultiselection,String title){
        String myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
        Intent intent = new Intent(context,UsersListActivity.class);
        intent.putExtra("allowMultiselection",allowMultiselection);
        intent.putExtra("mode",USERS_LIST_ACTIVITY_MODE_SELECTION);
        intent.putExtra("title",title);
        List<Conversation> conversations = ServicesProvider.getService(ConversationService.class).getAllConversations();
        ArrayList<String> userList = new ArrayList<>(conversations.size());
        for (Conversation conversation : conversations) {
            if(StringHelper.notStringNullOrEmpty(conversation.chatterId) && !myUserId.equals(conversation.chatterId)){
                userList.add(conversation.chatterId);
            }
        }
        intent.putStringArrayListExtra("userIdList",userList);
        context.startActivityForResult(intent,USERS_LIST_ACTIVITY_MODE_SELECTION);
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
