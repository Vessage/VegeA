package cn.bahamut.vessage.conversation.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.ProgressHUDHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.account.UsersListActivity;
import cn.bahamut.vessage.helper.ImageHelper;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.AssetsDefaultConstants;
import cn.bahamut.vessage.main.EditPropertyActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.services.groupchat.ChatGroup;
import cn.bahamut.vessage.services.groupchat.ChatGroupService;
import cn.bahamut.vessage.services.user.UserService;
import cn.bahamut.vessage.services.user.VessageUser;

public class ChatGroupProfileActivity extends AppCompatActivity {

    private static final int CHANGE_GROUP_NAME_REQUEST_ID = 1;
    private static final int SELECT_GROUP_USERS_REQUEST_ID = 2;
    private ViewHolder.UserListViewHolder.UserItemViewAdapter userItemAdapter;
    private ChatGroup chatGroup;
    private Map<String, VessageUser> chatUsers;
    private RecyclerView recyclerView;
    private boolean isHoster = false;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_chat_group_info);
        getSupportActionBar().setTitle(R.string.group_profile_info);
        userService = ServicesProvider.getService(UserService.class);
        chatUsers = new HashMap<>();
        recyclerView = (RecyclerView) findViewById(R.id.info_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter());
        chatGroup = ServicesProvider.getService(ChatGroupService.class).getChatGroupById(getIntent().getStringExtra("groupId")).copyToObject();
        ServicesProvider.getService(UserService.class).addObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onUserProfileUpdated);
        for (String s : chatGroup.getHosters()) {
            if (UserSetting.getUserId().equals(s)) {
                isHoster = true;
            }
        }
        prepareUserProfiles();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userService = null;
        ServicesProvider.getService(UserService.class).deleteObserver(UserService.NOTIFY_USER_PROFILE_UPDATED, onUserProfileUpdated);
    }

    private void prepareUserProfiles() {
        chatUsers.clear();
        UserService userService = ServicesProvider.getService(UserService.class);
        List<String> notLoadedId = new LinkedList<>();
        for (String userId : chatGroup.getChatters()) {
            VessageUser user = userService.getUserById(userId);
            if (user != null) {
                chatUsers.put(userId, user);
            } else {
                user = new VessageUser();
                user.userId = userId;
                chatUsers.put(userId, user);
                notLoadedId.add(userId);
            }
        }
        for (String uid : notLoadedId) {
            userService.fetchUserByUserId(uid);
        }
    }

    private Observer onUserProfileUpdated = new Observer() {
        @Override
        public void update(ObserverState state) {
            VessageUser user = (VessageUser) state.getInfo();
            if (user != null) {
                if (chatUsers.containsKey(user.userId)) {
                    chatUsers.put(user.userId, user);
                    String[] chatters = chatGroup.getChatters();
                    for (int i = 0; i < chatters.length; i++) {
                        if (user.userId.equals(chatters[i])) {
                            userItemAdapter.notifyItemChanged(i);
                        }
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isHoster) {
            menu.add(Menu.NONE, Menu.FIRST, 0, R.string.add_user_to_group).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getOrder() == 0) {
            addUserToGroup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addUserToGroup() {
        if (ChatGroup.MAX_USERS_COUNT <= chatGroup.getChatters().length) {
            Toast.makeText(ChatGroupProfileActivity.this, String.format(LocalizedStringHelper.getLocalizedString(R.string.chat_group_at_most_x_user), ChatGroup.MAX_USERS_COUNT), Toast.LENGTH_SHORT).show();
            return;
        }
        new UsersListActivity.ShowSelectUserActivityBuilder(ChatGroupProfileActivity.this)
                .setAllowMultiselection(true)
                .setCanSelectNearUser(false)
                .setCanSelectMobile(false)
                .setCanSelectActiveUser(false)
                .setMultiselectionMaxCount(ChatGroup.MAX_USERS_COUNT - chatGroup.getChatters().length)
                .setMultiselectionMinCount(1)
                .setMultiselectionMaxCount(1)
                .setRemoveMyProfile(true)
                .setTitle(LocalizedStringHelper.getLocalizedString(R.string.add_user_to_group_title))
                .setConversationUserIdList(chatGroup.getChatters())
                .showActivity(SELECT_GROUP_USERS_REQUEST_ID);
    }

    private void handleAddUserToGroup(Intent data) {
        List<String> userIds = data.getStringArrayListExtra(UsersListActivity.SELECTED_USER_IDS_ARRAY_KEY);
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(this);
        ServicesProvider.getService(ChatGroupService.class).addUserToGroup(chatGroup.groupId, userIds.get(0), new ChatGroupService.OnAddUserToGroupNameHandler() {
            @Override
            public void onFinished(String[] newChatters) {
                hud.dismiss();
                ProgressHUDHelper.showHud(ChatGroupProfileActivity.this, R.string.add_user_to_group_suc, R.mipmap.check_mark, true);
                chatGroup.setChatter(newChatters);
                prepareUserProfiles();
                recyclerView.getAdapter().notifyItemChanged(0);
            }

            @Override
            public void onFailure() {
                hud.dismiss();
                Toast.makeText(ChatGroupProfileActivity.this, R.string.add_user_to_group_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        class UserListViewHolder {
            RecyclerView userListView;

            class UserItemViewHolder extends RecyclerView.ViewHolder {
                ImageView avatar;
                TextView nick;

                public UserItemViewHolder(View itemView) {
                    super(itemView);
                    avatar = (ImageView) itemView.findViewById(R.id.avatar);
                    nick = (TextView) itemView.findViewById(R.id.nick);
                }
            }

            class OnClickUserItemViewHandler implements View.OnClickListener {

                VessageUser user;

                public OnClickUserItemViewHandler(VessageUser user) {
                    this.user = user;
                }

                @Override
                public void onClick(View v) {
                    AppMain.showUserProfileAlert(ChatGroupProfileActivity.this, user, null);
                }
            }

            class UserItemViewAdapter extends RecyclerView.Adapter<UserItemViewHolder> {

                @Override
                public UserItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View item = ChatGroupProfileActivity.this.getLayoutInflater().inflate(R.layout.conversation_chat_group_user_item, null);
                    return new UserItemViewHolder(item);
                }

                @Override
                public void onBindViewHolder(UserItemViewHolder holder, int position) {

                    String userId = chatGroup.getChatters()[position];
                    if (chatUsers.containsKey(userId)) {
                        VessageUser user = chatUsers.get(userId);
                        holder.itemView.setOnClickListener(new OnClickUserItemViewHandler(user));
                        ImageHelper.setImageByFileId(holder.avatar, user.avatar, AssetsDefaultConstants.getDefaultFace(userId.hashCode()));
                        String nick = userService.getUserNoteOrNickName(user.userId);
                        if (StringHelper.isStringNullOrWhiteSpace(nick) == false) {
                            nick = user.nickName;
                        }
                        holder.nick.setText(nick);
                    } else {
                        holder.itemView.setOnClickListener(null);
                    }
                }

                @Override
                public int getItemCount() {
                    return chatGroup.getChatters().length;
                }
            }

            public UserListViewHolder(View itemView) {
                userListView = (RecyclerView) itemView.findViewById(R.id.users_list);
                LinearLayoutManager layoutManager = new LinearLayoutManager(ChatGroupProfileActivity.this);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                userListView.setLayoutManager(layoutManager);
                if (userItemAdapter == null) {
                    userItemAdapter = new UserItemViewAdapter();
                }
                userListView.setAdapter(userItemAdapter);
            }
        }

        class GroupNameViewHolder {
            TextView groupNameTextView;

            public GroupNameViewHolder(View itemView) {
                groupNameTextView = (TextView) itemView.findViewById(R.id.group_name);
            }
        }

        private UserListViewHolder userListViewHolder;
        private GroupNameViewHolder groupNameViewHolder;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == 0) {
                userListViewHolder = new UserListViewHolder(itemView);
            } else if (viewType == 1) {
                groupNameViewHolder = new GroupNameViewHolder(itemView);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case CHANGE_GROUP_NAME_REQUEST_ID:
                handleChangeGroupName(data);
                break;
            case SELECT_GROUP_USERS_REQUEST_ID:
                handleAddUserToGroup(data);
                break;
            default:
                break;
        }
    }

    private void handleChangeGroupName(Intent data) {
        if (data == null) {
            return;
        }

        String newGroupName = data.getStringExtra(EditPropertyActivity.KEY_PROPERTY_NEW_VALUE);
        if (StringHelper.isNullOrEmpty(newGroupName)) {
            Toast.makeText(this, R.string.group_name_cant_null, Toast.LENGTH_SHORT).show();
            return;
        } else if (newGroupName.equals(chatGroup.groupName)) {
            return;
        }
        ServicesProvider.getService(ChatGroupService.class).changeGroupName(chatGroup, newGroupName, new ChatGroupService.OnChangeGroupNameHandler() {
            @Override
            public void onChanged(String newGroupName) {
                ProgressHUDHelper.showHud(ChatGroupProfileActivity.this, R.string.change_group_name_suc, R.mipmap.check_mark, true);
                chatGroup.groupName = newGroupName;
                recyclerView.getAdapter().notifyItemChanged(1);
            }

            @Override
            public void onFailure() {
                ProgressHUDHelper.showHud(ChatGroupProfileActivity.this, R.string.change_group_name_error, R.mipmap.cross_mark, true);
            }
        });
    }

    private void askExitGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatGroupProfileActivity.this)
                .setTitle(chatGroup.groupName)
                .setMessage(R.string.ask_exit_chat_group_title);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitGroup();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void exitGroup() {
        final KProgressHUD hud = ProgressHUDHelper.showSpinHUD(ChatGroupProfileActivity.this);
        ServicesProvider.getService(ChatGroupService.class).quitChatGroup(chatGroup.groupId, new ChatGroupService.OnQuitChatGroupHandler() {
            @Override
            public void onQuited(ChatGroup quitedChatGroup) {
                hud.dismiss();
                finish();
            }

            @Override
            public void onQuitError() {
                hud.dismiss();
                Toast.makeText(ChatGroupProfileActivity.this, R.string.quit_chat_group_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = null;
            if (viewType == 0) {
                itemView = ChatGroupProfileActivity.this.getLayoutInflater().inflate(R.layout.conversation_chat_group_userlist_item, null);
            } else if (viewType == 1) {
                itemView = ChatGroupProfileActivity.this.getLayoutInflater().inflate(R.layout.conversation_chat_group_name_setting_item, null);
            } else if (viewType == 2) {
                itemView = ChatGroupProfileActivity.this.getLayoutInflater().inflate(R.layout.conversation_chat_group_exit_item, null);
            }
            return new ViewHolder(itemView, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == 0) {

            } else if (position == 1) {
                holder.groupNameViewHolder.groupNameTextView.setText(chatGroup.groupName);
                holder.itemView.setOnClickListener(onClickGroupNameItem);
            } else if (position == 2) {
                holder.itemView.setOnClickListener(onClickExit);
            }
        }

        private View.OnClickListener onClickExit = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askExitGroup();
            }
        };

        private View.OnClickListener onClickGroupNameItem = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPropertyActivity.showEditPropertyActivity(ChatGroupProfileActivity.this, CHANGE_GROUP_NAME_REQUEST_ID, R.string.change_group_name, chatGroup.groupName);
            }
        };

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    public static void showChatGroupProfileActivity(Context context, ChatGroup chatGroup) {
        Intent intent = new Intent(context, ChatGroupProfileActivity.class);
        intent.putExtra("groupId", chatGroup.groupId);
        context.startActivity(intent);
    }
}
