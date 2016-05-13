package cn.bahamut.vessage.services;

import android.util.Log;

import org.apache.commons.codec1.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
import cn.bahamut.vessage.models.SendVessageTask;
import cn.bahamut.vessage.models.Vessage;
import cn.bahamut.vessage.restfulapi.vessage.CancelSendVessageRequest;
import cn.bahamut.vessage.restfulapi.vessage.FinishSendVessageRequest;
import cn.bahamut.vessage.restfulapi.vessage.GetNewVessagesRequest;
import cn.bahamut.vessage.restfulapi.vessage.NotifyGotNewVessagesRequest;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageRequestBase;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageToMobileRequest;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageToUserRequest;
import cn.bahamut.vessage.restfulapi.vessage.SetVessageRead;
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

    public static interface OnSendVessageCompleted{
        void onSendVessageCompleted(boolean isOk,String sendedVessageId);
    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(VessageService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }


    public void sendVessageToMobile(String mobile, String videoPath, String myNick, String myMobile, OnSendVessageCompleted callback) {
        SendNewVessageToMobileRequest request = new SendNewVessageToMobileRequest();
        request.setReceiverMobile(mobile);
        sendVessageByRequest(request, mobile, videoPath, myNick, myMobile, callback);
    }

    public void sendVessageToUser(String receiverId, String videoPath,String myNick,String myMobile,OnSendVessageCompleted callback) {
        SendNewVessageToUserRequest request = new SendNewVessageToUserRequest();
        request.setReceiverId(receiverId);
        sendVessageByRequest(request, null, videoPath, myNick, myMobile, callback);
    }

    private void sendVessageByRequest(SendNewVessageRequestBase request, final String toMobile, final String videoPath, String myNick, String myMobile, final OnSendVessageCompleted callback) {
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
                    Realm.getDefaultInstance().beginTransaction();
                    SendVessageTask task = Realm.getDefaultInstance().createObjectFromJson(SendVessageTask.class,result);
                    task.videoPath = videoPath;
                    task.toMobile = toMobile;
                    vessageId = task.vessageId;
                    Realm.getDefaultInstance().commitTransaction();
                }
                if(callback!= null){
                    callback.onSendVessageCompleted(isOk,vessageId);
                }
            }
        });
    }

    private SendVessageTask getSendVessageTask(String vessageId) {
        return Realm.getDefaultInstance().where(SendVessageTask.class).equalTo("vessageId",vessageId).findFirst();
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
                    Realm.getDefaultInstance().beginTransaction();
                    m.removeFromRealm();
                    Realm.getDefaultInstance().commitTransaction();
                    postNotification(NOTIFY_NEW_VESSAGE_SENDED, taskInfo);
                }else {
                    postNotification(NOTIFY_FINISH_SEND_VESSAGE_FAILED, taskInfo);
                }
            }
        });
    }

    public void readVessage(Vessage vessage){
        if(vessage.isRead()){
            return;
        }

        Realm.getDefaultInstance().beginTransaction();
        vessage.setRead(true);
        Realm.getDefaultInstance().commitTransaction();
        postNotification(NOTIFY_VESSAGE_READ,vessage);
    }

    public void removeVessage(Vessage vessage){
        if (!vessage.isRead()){
            Vessage rvsg = new Vessage();
            rvsg.setRead(true);
            rvsg.extraInfo = vessage.extraInfo;
            rvsg.fileId = vessage.fileId;
            rvsg.sender = vessage.sender;
            rvsg.vessageId = vessage.vessageId;
            rvsg.sendTime = vessage.sendTime;
            postNotification(NOTIFY_VESSAGE_READ,rvsg);
        }
        String vessageId = vessage.vessageId;
        Realm.getDefaultInstance().beginTransaction();
        vessage.removeFromRealm();
        Realm.getDefaultInstance().commitTransaction();
        SetVessageRead req = new SetVessageRead();
        req.setVessageId(vessageId);

        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
            }
        });
    }

    public void newVessageFromServer(){
        GetNewVessagesRequest req = new GetNewVessagesRequest();

        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if(isOk){
                    List<Vessage> vsgs = new ArrayList<Vessage>();
                    Realm.getDefaultInstance().beginTransaction();
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            Vessage vsg = Realm.getDefaultInstance().createOrUpdateObjectFromJson(Vessage.class,result.getJSONObject(i));
                            vsgs.add(vsg);
                        } catch (JSONException e) {
                            Log.d("Here","Debug");
                        }
                    }
                    Realm.getDefaultInstance().commitTransaction();

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
        RealmResults<Vessage> results = Realm.getDefaultInstance().where(Vessage.class).equalTo("sender", chatterId).findAllSorted("sendTime", Sort.DESCENDING);
        if(results.size() > 0){
            return results.first();
        }
        return null;
    }

    public int getNotReadVessageCount(String chatterId){
        return getNotReadVessage(chatterId).size();
    }

    public List<Vessage> getNotReadVessage(String chatterId) {
        List<Vessage> vsgs = Realm.getDefaultInstance().where(Vessage.class).equalTo("sender",chatterId).equalTo("isRead",false).findAll();
        return vsgs;
    }
}
