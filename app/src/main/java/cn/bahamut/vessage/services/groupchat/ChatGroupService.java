package cn.bahamut.vessage.services.groupchat;

import android.content.Context;

import org.json.JSONObject;

import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.restfulapi.groupchat.CreateGroupChatRequest;
import cn.bahamut.vessage.restfulapi.groupchat.GetGroupChatRequest;
import cn.bahamut.vessage.restfulapi.groupchat.QuitGroupChatRequest;
import io.realm.Realm;

/**
 * Created by alexchow on 16/7/19.
 */
public class ChatGroupService extends Observable implements OnServiceUserLogin,OnServiceUserLogout,OnServiceInit {
    public static final String NOTIFY_CHAT_GROUP_UPDATED = "NOTIFY_CHAT_GROUP_UPDATED";
    private Realm realm;
    @Override
    public void onServiceInit(Context applicationContext) {

    }

    @Override
    public void onUserLogin(String userId) {
        realm = Realm.getDefaultInstance();
        ServicesProvider.setServiceReady(ChatGroupService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ChatGroupService.class);
        realm.close();
        realm = null;
    }

    public ChatGroup getCachedChatGroup(String groupId) {
        return realm.where(ChatGroup.class).equalTo("groupId",groupId).findFirst();
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
                if (isOk){
                    getRealm().beginTransaction();
                    ChatGroup chatGroup = getRealm().createOrUpdateObjectFromJson(ChatGroup.class,result);
                    chatGroup.setPrimitiveArrayValues(result);
                    getRealm().commitTransaction();
                    chatGroup = chatGroup.copyToObject();
                    onFetchChatGroupHandler.onFetchedChatGroup(chatGroup);
                    postNotification(NOTIFY_CHAT_GROUP_UPDATED,chatGroup);
                }else {
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
                if (isOk){
                    getRealm().beginTransaction();
                    ChatGroup chatGroup = getRealm().createOrUpdateObjectFromJson(ChatGroup.class,result);
                    chatGroup.setPrimitiveArrayValues(result);
                    getRealm().commitTransaction();
                    chatGroup = chatGroup.copyToObject();
                    handler.onCreatedChatGroup(chatGroup);
                    postNotification(NOTIFY_CHAT_GROUP_UPDATED,chatGroup);
                }else {
                    handler.onCreateChatGroupError();
                }
            }
        });
    }

    public Realm getRealm() {
        return realm;
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
                    getRealm().beginTransaction();
                    ChatGroup group = getRealm().where(ChatGroup.class).equalTo("groupId",groupId).findFirst();
                    group.setChatter(new String[0]);
                    getRealm().commitTransaction();
                    handler.onQuited(group.copyToObject());
                    postNotification(NOTIFY_CHAT_GROUP_UPDATED,group.copyToObject());
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
