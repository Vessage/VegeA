package cn.bahamut.vessage.services;

import org.apache.commons.codec1.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.models.SendVessageResultModel;
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
import io.realm.Sort;

/**
 * Created by alexchow on 16/3/30.
 */
public class VessageService extends Observable implements OnServiceUserLogin,OnServiceUserLogout {

    public static final String NOTIFY_VESSAGE_READ = "NOTIFY_VESSAGE_READ";
    public static final String NOTIFY_NEW_VESSAGES_RECEIVED = "NOTIFY_NEW_VESSAGES_RECEIVED";
    public static final String NOTIFY_NEW_VESSAGE_RECEIVED = "NOTIFY_NEW_VESSAGE_RECEIVED";
    public static interface OnSendVessageCompleted{
        void onSendVessageCompleted(boolean isOk,SendVessageTask taskModel);
    }

    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(VessageService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }


    public void sendVessageToMobile(String mobile, String videoPath, String myNick, String myMobile, OnSendVessageCompleted callback){
        SendNewVessageToMobileRequest request = new SendNewVessageToMobileRequest();
        request.setReceiverMobile(mobile);
        sendVessageByRequest(request,videoPath, myNick, myMobile, callback);
    }

    public void sendVessageToUser(String receiverId, String videoPath,String myNick,String myMobile,OnSendVessageCompleted callback){
        SendNewVessageToUserRequest request = new SendNewVessageToUserRequest();
        request.setReceiverId(receiverId);
        sendVessageByRequest(request,videoPath,  myNick, myMobile, callback);
    }

    private void sendVessageByRequest(SendNewVessageRequestBase request, final String videoPath, String myNick, String myMobile, final OnSendVessageCompleted callback) {
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
                if (isOk) {

                    try {
                        SendVessageResultModel resultModel = JsonHelper.parseObject(result,SendVessageResultModel.class);
                        Realm.getDefaultInstance().beginTransaction();
                        SendVessageTask task = Realm.getDefaultInstance().createObjectFromJson(SendVessageTask.class, result);
                        task.vessageBoxId = resultModel.getVessageBoxId();
                        task.vessageId = resultModel.getVessageId();
                        task.videoPath = videoPath;
                        Realm.getDefaultInstance().commitTransaction();
                        callback.onSendVessageCompleted(true,task);

                    } catch (JSONException e) {
                        callback.onSendVessageCompleted(false,null);
                    }

                } else {
                    callback.onSendVessageCompleted(false,null);
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

    public void finishSendVessage(String vboxId,String vessageId){
        SendVessageTask m = getSendVessageTask(vessageId);
        if(m!= null){
            FinishSendVessageRequest request = new FinishSendVessageRequest();
            request.setVessageId(vessageId);
            request.setVessageBoxId(vboxId);
            BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
                @Override
                public void callback(Boolean isOk, int statusCode, JSONObject result) {

                }
            });
        }
    }

    public void readVessage(Vessage vessage){
        Realm.getDefaultInstance().beginTransaction();
        vessage.isRead = true;
        Realm.getDefaultInstance().commitTransaction();
        postVessageRead(vessage);
    }

    private void postVessageRead(Vessage vessage){
        ObserverState state = new ObserverState();
        state.setNotifyType(NOTIFY_VESSAGE_READ);
        state.setInfo(vessage);
        postNotification(state);
    }

    public void removeVessage(Vessage vessage){
        if (!vessage.isRead){
            postVessageRead(vessage);
        }
        Realm.getDefaultInstance().beginTransaction();
        vessage.removeFromRealm();
        Realm.getDefaultInstance().commitTransaction();
        SetVessageRead req = new SetVessageRead();
        req.setVessageId(vessage.vessageId);

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
                    List<Vessage> vsgs = new ArrayList<Vessage>(result.length());
                    Realm.getDefaultInstance().beginTransaction();
                    for (int i = 0; i < vsgs.size(); i++) {
                        try {
                            Vessage vsg = Realm.getDefaultInstance().createOrUpdateObjectFromJson(Vessage.class,result.getJSONObject(i));
                            vsgs.set(i,vsg);
                        } catch (JSONException e) {

                        }
                    }
                    Realm.getDefaultInstance().commitTransaction();

                    for (Vessage vsg : vsgs) {
                        ObserverState state = new ObserverState();
                        state.setNotifyType(NOTIFY_NEW_VESSAGE_RECEIVED);
                        state.setInfo(vsg);
                        postNotification(state);
                    }

                    notifyVessageGot();

                    ObserverState state = new ObserverState();
                    state.setNotifyType(NOTIFY_NEW_VESSAGES_RECEIVED);
                    state.setInfo(vsgs);
                    postNotification(state);

                }else {

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
        return Realm.getDefaultInstance().where(Vessage.class).endsWith("sender", chatterId).findAllSorted("sendTime", Sort.DESCENDING).first();
    }

    public List<Vessage> getNotReadVessage(String chatterId) {
        return Realm.getDefaultInstance().where(Vessage.class).equalTo("isRead",false).equalTo("sender",chatterId).findAll();
    }
}
