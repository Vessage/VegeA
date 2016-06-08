package cn.bahamut.vessage.services.vessage;

import android.util.Log;

import org.apache.commons.codec1.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.restfulapi.vessage.CancelSendVessageRequest;
import cn.bahamut.vessage.restfulapi.vessage.FinishSendVessageRequest;
import cn.bahamut.vessage.restfulapi.vessage.GetNewVessagesRequest;
import cn.bahamut.vessage.restfulapi.vessage.NotifyGotNewVessagesRequest;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageRequestBase;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageToMobileRequest;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageToUserRequest;
import cn.bahamut.vessage.restfulapi.vessage.SetVessageRead;
import cn.bahamut.vessage.services.conversation.ConversationService;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by alexchow on 16/3/30.
 */
public class VessageService extends Observable implements OnServiceUserLogin,OnServiceUserLogout {

    public static final String NOTIFY_VESSAGE_READ = "NOTIFY_VESSAGE_READ";
    public static final String NOTIFY_NEW_VESSAGES_RECEIVED = "NOTIFY_NEW_VESSAGES_RECEIVED";
    public static final String NOTIFY_NEW_VESSAGE_RECEIVED = "NOTIFY_NEW_VESSAGE_RECEIVED";
    public static final String NOTIFY_NEW_VESSAGE_SENDED = "NOTIFY_NEW_VESSAGE_SENDED";
    public static final String NOTIFY_FINISH_SEND_VESSAGE_FAILED = "NOTIFY_FINISH_SEND_VESSAGE_FAILED";
    private Realm realm;

    public Realm getRealm() {
        return realm;
    }

    public static interface OnSendVessageCompleted{
        void onSendVessageCompleted(boolean isOk,String sendedVessageId);
    }

    private HashMap<String,Integer> chatterNotReadMessageCountMap;

    @Override
    public void onUserLogin(String userId) {
        realm = Realm.getDefaultInstance();
        ServicesProvider.setServiceReady(VessageService.class);
        loadChatterNotReadMessageCountMap();
    }

    private void loadChatterNotReadMessageCountMap() {
        chatterNotReadMessageCountMap = new HashMap<>();
        List<Vessage> vsgs = getRealm().where(Vessage.class).findAll();
        for (Vessage vsg : vsgs) {
            if(!vsg.isRead){
                incChatterNotReadVessageCount(vsg.sender);
            }
        }
    }

    private void incChatterNotReadVessageCount(String sender) {
        if(chatterNotReadMessageCountMap.containsKey(sender)){
            chatterNotReadMessageCountMap.put(sender,chatterNotReadMessageCountMap.get(sender) + 1);
        }else {
            chatterNotReadMessageCountMap.put(sender,1);
        }
    }

    @Override
    public void onUserLogout() {
        realm.close();
        realm = null;
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }


    public void sendVessageToMobile(String mobile, String videoPath, String myNick, String myMobile, OnSendVessageCompleted callback) {
        SendNewVessageToMobileRequest request = new SendNewVessageToMobileRequest();
        request.setReceiverMobile(mobile);
        sendVessageByRequest(request, null, videoPath, myNick, myMobile, callback);
    }

    public void sendVessageToUser(String receiverId, String videoPath,String myNick,String myMobile,OnSendVessageCompleted callback) {
        SendNewVessageToUserRequest request = new SendNewVessageToUserRequest();
        request.setReceiverId(receiverId);
        sendVessageByRequest(request, receiverId, videoPath, myNick, myMobile, callback);
    }

    private void sendVessageByRequest(SendNewVessageRequestBase request, final String toUser, final String videoPath, String myNick, String myMobile, final OnSendVessageCompleted callback) {
        JSONObject extraInfo = new JSONObject();
        try {
            extraInfo.put("accountId", UserSetting.getLastUserLoginedAccount());
            extraInfo.put("nickName",myNick);
            if(!StringHelper.isStringNullOrEmpty(myMobile)){
                extraInfo.put("mobileHash", DigestUtils.md5Hex(myMobile));
            }
            request.setExtraInfo(extraInfo.toString());
        } catch (JSONException e) {

        }
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                String vessageId = null;
                if (isOk) {
                    getRealm().beginTransaction();
                    SendVessageTask task = getRealm().createObjectFromJson(SendVessageTask.class,result);
                    task.videoPath = videoPath;
                    task.toMobile = toUser; //toMobile use to store to user, receiver mobile is always null
                    vessageId = task.vessageId;
                    getRealm().commitTransaction();
                }
                if(callback!= null){
                    callback.onSendVessageCompleted(isOk,vessageId);
                }
            }
        });
    }

    private SendVessageTask getSendVessageTask(String vessageId) {
        return getRealm().where(SendVessageTask.class).equalTo("vessageId",vessageId).findFirst();
    }

    public void cancelSendVessage(String vessageId){
        SendVessageTask m = getSendVessageTask(vessageId);
        if (m != null) {
            CancelSendVessageRequest req = new CancelSendVessageRequest();
            req.setVessageId(m.vessageId);
            req.setVessageBoxId(m.vessageBoxId);
            BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
                @Override
                public void callback(Boolean isOk, int statusCode, JSONObject result) {

                }
            });
        }
    }

    public void finishSendVessage(String fileId, String vboxId, final String vessageId){

        FinishSendVessageRequest request = new FinishSendVessageRequest();
        request.setVessageId(vessageId);
        request.setVessageBoxId(vboxId);
        request.setFileId(fileId);

        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                SendVessageTask m = getSendVessageTask(vessageId);
                SendVessageTask taskInfo = new SendVessageTask();
                taskInfo.fileId = m.fileId;
                taskInfo.toMobile = m.toMobile;
                taskInfo.vessageBoxId = m.vessageBoxId;
                taskInfo.vessageId = m.vessageId;
                taskInfo.videoPath = m.videoPath;

                if(isOk) {
                    getRealm().beginTransaction();
                    m.deleteFromRealm();
                    getRealm().commitTransaction();
                    postNotification(NOTIFY_NEW_VESSAGE_SENDED, taskInfo);
                }else {
                    postNotification(NOTIFY_FINISH_SEND_VESSAGE_FAILED, taskInfo);
                }
            }
        });
    }

    public void readVessage(Vessage vessage){
        if(vessage.isRead){
            return;
        }
        decChatterNotReadVessageCount(vessage.sender);
        getRealm().beginTransaction();
        vessage.isRead = true;
        getRealm().commitTransaction();
        postNotification(NOTIFY_VESSAGE_READ,vessage);
    }

    public void removeVessage(Vessage vessage){
        if (!vessage.isRead){
            Vessage rvsg = new Vessage();
            rvsg.isRead = true;
            rvsg.vessageId = vessage.vessageId;
            rvsg.extraInfo = vessage.extraInfo;
            rvsg.fileId = vessage.fileId;
            rvsg.sender = vessage.sender;
            rvsg.vessageId = vessage.vessageId;
            rvsg.sendTime = vessage.sendTime;
            decChatterNotReadVessageCount(rvsg.sender);
            postNotification(NOTIFY_VESSAGE_READ,rvsg);
        }
        String vessageId = vessage.vessageId;
        getRealm().beginTransaction();
        vessage.deleteFromRealm();
        getRealm().commitTransaction();
        SetVessageRead req = new SetVessageRead();
        req.setVessageId(vessageId);

        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
            }
        });
    }

    private void decChatterNotReadVessageCount(String sender) {
        if(chatterNotReadMessageCountMap.containsKey(sender)){
            chatterNotReadMessageCountMap.put(sender,chatterNotReadMessageCountMap.get(sender) - 1);
        }else {
            chatterNotReadMessageCountMap.put(sender,0);
        }
    }

    public void newVessageFromServer(){
        GetNewVessagesRequest req = new GetNewVessagesRequest();

        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if(isOk){
                    List<Vessage> vsgs = new ArrayList<>();
                    getRealm().beginTransaction();
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            Vessage vsg = getRealm().createOrUpdateObjectFromJson(Vessage.class,result.getJSONObject(i));
                            incChatterNotReadVessageCount(vsg.sender);
                            vsgs.add(vsg);
                        } catch (JSONException e) {
                            Log.d("Here","Debug");
                        }
                    }
                    getRealm().commitTransaction();

                    for (Vessage vsg : vsgs) {
                        postNotification(NOTIFY_NEW_VESSAGE_RECEIVED,vsg);
                    }
                    notifyVessageGot();
                    postNotification(NOTIFY_NEW_VESSAGES_RECEIVED,vsgs);
                }
            }
        });
    }

    private void notifyVessageGot(){
        NotifyGotNewVessagesRequest req = new NotifyGotNewVessagesRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
            }
        });
    }

    public Vessage getCachedNewestVessage(String chatterId) {
        RealmResults<Vessage> results = getRealm().where(Vessage.class).equalTo("sender", chatterId).findAllSorted("sendTime", Sort.DESCENDING);
        if(results.size() > 0){
            return results.first();
        }
        return null;
    }

    public int getNotReadVessageCount(String chatterId){
        if(chatterNotReadMessageCountMap.containsKey(chatterId)){
            return chatterNotReadMessageCountMap.get(chatterId);
        }
        return 0;
    }

    public List<Vessage> getNotReadVessage(String chatterId) {
        List<Vessage> vsgs = getRealm().where(Vessage.class).equalTo("sender",chatterId).equalTo("isRead",false).findAll();
        return vsgs;
    }
}
