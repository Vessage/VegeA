package cn.bahamut.vessage.services.groupchat;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;

import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.restfulapi.groupchat.AddUserJoinGroupChatRequest;
import cn.bahamut.vessage.restfulapi.groupchat.CreateGroupChatRequest;
import cn.bahamut.vessage.restfulapi.groupchat.EditGroupNameRequest;
import cn.bahamut.vessage.restfulapi.groupchat.GetGroupChatRequest;
import cn.bahamut.vessage.restfulapi.groupchat.QuitGroupChatRequest;
import io.realm.Realm;

/**
 * Created by alexchow on 16/7/19.
 */
public class ChatGroupService extends Observable implements OnServiceUserLogin,OnServiceUserLogout,OnServiceInit {
    public static final String NOTIFY_CHAT_GROUP_UPDATED = "NOTIFY_CHAT_GROUP_UPDATED";
    @Override
    public void onServiceInit(Context applicationContext) {

    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(ChatGroupService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ChatGroupService.class);
    }

    public ChatGroup getCachedChatGroup(String groupId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            ChatGroup chatGroup = realm.where(ChatGroup.class).equalTo("groupId", groupId).findFirst();
            return chatGroup != null ? chatGroup.copyToObject() : null;
        }
    }

    public void fetchChatGroup(String chatterId){
        fetchChatGroup(chatterId, new OnFetchChatGroupAdapter(){});
    }

    public void fetchChatGroup(String groupId, final OnFetchChatGroupHandler onFetchChatGroupHandler) {
        GetGroupChatRequest request = new GetGroupChatRequest();
        request.setGroupId(groupId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        ChatGroup chatGroup = realm.createOrUpdateObjectFromJson(ChatGroup.class, result);
                        chatGroup.setPrimitiveArrayValues(result);
                        realm.commitTransaction();
                        chatGroup = chatGroup.copyToObject();
                        onFetchChatGroupHandler.onFetchedChatGroup(chatGroup);
                        postNotification(NOTIFY_CHAT_GROUP_UPDATED, chatGroup);
                    }
                } else {
                    onFetchChatGroupHandler.onFetchChatGroupError();
                }
            }
        });
    }

    public void createNewChatGroup(String groupName, String[] userIds, final OnCreatChatGroupHandler handler) {
        CreateGroupChatRequest request = new CreateGroupChatRequest();
        request.setGroupUsers(userIds);
        request.setGroupName(groupName);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        ChatGroup chatGroup = realm.createOrUpdateObjectFromJson(ChatGroup.class, result);
                        chatGroup.setPrimitiveArrayValues(result);
                        realm.commitTransaction();
                        chatGroup = chatGroup.copyToObject();
                        handler.onCreatedChatGroup(chatGroup);
                        postNotification(NOTIFY_CHAT_GROUP_UPDATED, chatGroup);
                    }
                } else {
                    handler.onCreateChatGroupError();
                }
            }
        });
    }

    public ChatGroup getChatGroupById(String groupId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            ChatGroup group = realm.where(ChatGroup.class).equalTo("groupId", groupId).findFirst();
            return group != null ? group.copyToObject() : null;
        }
    }

    public interface OnAddUserToGroupNameHandler{
        void onFinished(String[] newChatters);
        void onFailure();
    }

    public void addUserToGroup(final String groupId, final String userId, final OnAddUserToGroupNameHandler handler) {
        AddUserJoinGroupChatRequest request = new AddUserJoinGroupChatRequest();
        request.setGroupId(groupId);
        request.setUserId(userId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        ChatGroup group = realm.where(ChatGroup.class).equalTo("groupId", groupId).findFirst();
                        ArrayList<String> newUserIds = new ArrayList<String>(group.getChatters().length + 1);
                        for (String s : group.getChatters()) {
                            newUserIds.add(s);
                        }
                        newUserIds.add(userId);

                        String[] newUserIdArr = newUserIds.toArray(new String[0]);
                        group.setChatter(newUserIdArr);
                        realm.commitTransaction();
                        postNotification(NOTIFY_CHAT_GROUP_UPDATED, group.copyToObject());
                        handler.onFinished(newUserIdArr);
                    }
                }else {
                    handler.onFailure();
                }
            }
        });
    }

    public interface OnChangeGroupNameHandler{
        void onChanged(String newGroupName);
        void onFailure();
    }

    public void changeGroupName(ChatGroup chatGroup, final String newGroupName, final OnChangeGroupNameHandler handler) {
        final String groupId = chatGroup.groupId;
        EditGroupNameRequest request = new EditGroupNameRequest();
        request.setGroupId(groupId);
        request.setInviteCode(chatGroup.inviteCode);
        request.setNewGroupName(newGroupName);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        ChatGroup group = realm.where(ChatGroup.class).equalTo("groupId", groupId).findFirst();
                        group.groupName = newGroupName;
                        realm.commitTransaction();
                        handler.onChanged(newGroupName);
                        postNotification(NOTIFY_CHAT_GROUP_UPDATED, group.copyToObject());
                    }
                }else {
                    handler.onFailure();
                }
            }
        });
    }

    public interface OnQuitChatGroupHandler{
        void onQuited(ChatGroup quitedChatGroup);
        void onQuitError();
    }

    public void quitChatGroup(final String groupId, final OnQuitChatGroupHandler handler) {
        QuitGroupChatRequest request = new QuitGroupChatRequest();
        request.setGroupId(groupId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        ChatGroup group = realm.where(ChatGroup.class).equalTo("groupId", groupId).findFirst();
                        group.setChatter(new String[0]);
                        realm.commitTransaction();
                        handler.onQuited(group.copyToObject());
                        postNotification(NOTIFY_CHAT_GROUP_UPDATED, group.copyToObject());
                    }
                }else {
                    handler.onQuitError();
                }
            }
        });
    }

    public interface OnCreatChatGroupHandler {
        void onCreatedChatGroup(ChatGroup chatGroup);
        void onCreateChatGroupError();
    }

    private class OnFetchChatGroupAdapter implements OnFetchChatGroupHandler{
        public void onFetchChatGroupError() {

        }
        public void onFetchedChatGroup(ChatGroup chatGroup) {

        }
    }

    public interface OnFetchChatGroupHandler {
        void onFetchedChatGroup(ChatGroup chatGroup);
        void onFetchChatGroupError();
    }
}
