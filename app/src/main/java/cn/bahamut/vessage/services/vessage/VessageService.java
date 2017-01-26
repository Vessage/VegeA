package cn.bahamut.vessage.services.vessage;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.SoundManager;
import cn.bahamut.vessage.restfulapi.vessage.CancelSendVessageRequest;
import cn.bahamut.vessage.restfulapi.vessage.FinishSendVessageRequest;
import cn.bahamut.vessage.restfulapi.vessage.GetNewVessagesRequest;
import cn.bahamut.vessage.restfulapi.vessage.NotifyGotNewVessagesRequest;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageRequestBase;
import cn.bahamut.vessage.restfulapi.vessage.SendNewVessageToUserRequest;
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

    public static final String NOTIFY_NEW_VESSAGE_FINISH_POSTED = "NOTIFY_NEW_VESSAGE_FINISH_POSTED";
    public static final String NOTIFY_FINISH_POST_VESSAGE_FAILED = "NOTIFY_FINISH_POST_VESSAGE_FAILED";
    private static final String TAG = "VessageService";

    public static interface OnSendVessageCompleted{
        void onSendVessageCompleted(boolean isOk,String sendedVessageId);
    }

    private HashMap<String,Integer> chatterNotReadMessageCountMap;

    @Override
    public void onUserLogin(String userId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();
            for (Vessage vessage : realm.where(Vessage.class).equalTo("isRead", true).findAll()) {
                vessage.deleteFromRealm();
            }
            realm.commitTransaction();
        }
        ServicesProvider.setServiceReady(VessageService.class);
        loadChatterNotReadMessageCountMap();
    }

    private void loadChatterNotReadMessageCountMap() {
        chatterNotReadMessageCountMap = new HashMap<>();
        try (Realm realm = Realm.getDefaultInstance()) {
            List<Vessage> vsgs = realm.where(Vessage.class).findAll();
            for (Vessage vsg : vsgs) {
                if (!vsg.isRead) {
                    incChatterNotReadVessageCount(vsg.sender);
                }
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
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }

    public void sendVessageToReceiver(String recevierId,Vessage vessage,OnSendVessageCompleted callback){
        SendNewVessageToUserRequest request = new SendNewVessageToUserRequest();
        request.setReceiverId(recevierId);
        request.setIsGroup(vessage.isGroup);
        sendVessageByRequest(request,vessage,callback);
    }

    private void sendVessageByRequest(SendNewVessageRequestBase request, Vessage vessage, final OnSendVessageCompleted callback) {
        request.setBody(vessage.body);
        request.setExtraInfo(vessage.extraInfo);
        request.setFileId(vessage.fileId);
        request.setTypeId(vessage.typeId);
        request.setReady(true);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                String vessageId = null;
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        SendVessageResultModel model = realm.createObjectFromJson(SendVessageResultModel.class, result);
                        vessageId = model.vessageId;
                        realm.commitTransaction();
                    }
                }
                if(callback!= null){
                    callback.onSendVessageCompleted(isOk,vessageId);
                }
            }
        });
    }

    private SendVessageResultModel getSendVessageResultModel(String vessageId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            SendVessageResultModel model = realm.where(SendVessageResultModel.class).equalTo("vessageId", vessageId).findFirst();
            return model != null ? model.copyToObject() : null;
        }
    }

    public void cancelSendVessage(String vessageId){
        SendVessageResultModel m = getSendVessageResultModel(vessageId);
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

    public void finishSendVessage(String vessageId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            SendVessageResultModel model = realm.where(SendVessageResultModel.class).equalTo("vessageId", vessageId).findFirst();
            SendVessageResultModel model1 = model.copyToObject();
            realm.beginTransaction();
            model.deleteFromRealm();
            realm.commitTransaction();
            postNotification(NOTIFY_NEW_VESSAGE_FINISH_POSTED, model1);
        }
    }

    @Deprecated
    private void finishSendVessage(final String vessageId, String fileId, final OnSendVessageCompleted callback) {
        final SendVessageResultModel model = getSendVessageResultModel(vessageId);

        FinishSendVessageRequest request = new FinishSendVessageRequest();
        request.setVessageId(model.vessageId);
        request.setVessageBoxId(model.vessageBoxId);
        request.setFileId(fileId);

        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                try (Realm realm = Realm.getDefaultInstance()) {
                    SendVessageResultModel model = realm.where(SendVessageResultModel.class).equalTo("vessageId", vessageId).findFirst();
                    SendVessageResultModel model1 = model.copyToObject();
                    if (isOk) {
                        realm.beginTransaction();
                        model.deleteFromRealm();
                        realm.commitTransaction();
                        postNotification(NOTIFY_NEW_VESSAGE_FINISH_POSTED, model1);
                    } else {
                        postNotification(NOTIFY_FINISH_POST_VESSAGE_FAILED, model1);
                    }
                    callback.onSendVessageCompleted(isOk, model1.vessageId);
                }
            }
        });
    }

    public void readVessage(Vessage vessage) {
        if (vessage.isRead || !vessage.isNormalVessage()) {
            return;
        }
        decChatterNotReadVessageCount(vessage.sender);
        try (Realm realm = Realm.getDefaultInstance()) {
            vessage.isRead = true;
            Vessage vsg = realm.where(Vessage.class).equalTo("vessageId", vessage.vessageId).findFirst();
            if (vsg != null) {
                realm.beginTransaction();
                vsg.isRead = true;
                realm.commitTransaction();
            }
            postNotification(NOTIFY_VESSAGE_READ, vessage.copyToObject());
        }
    }

    public void removeVessages(List<Vessage> vessages) {
        try (Realm realm = Realm.getDefaultInstance()) {

            for (Vessage vessage : vessages) {
                Vessage vsg = realm.where(Vessage.class).equalTo("vessageId", vessage.vessageId).findFirst();
                if (vsg != null) {
                    realm.beginTransaction();
                    vsg.deleteFromRealm();
                    realm.commitTransaction();
                }

                if (!vessage.isRead) {
                    Vessage rvsg = vessage.copyToObject();
                    decChatterNotReadVessageCount(rvsg.sender);
                    postNotification(NOTIFY_VESSAGE_READ, rvsg);
                }
            }
        }
        Log.i(TAG, String.format("%d Vessages Removed", vessages.size()));
    }

    private void decChatterNotReadVessageCount(String sender) {
        if(chatterNotReadMessageCountMap.containsKey(sender)){
            chatterNotReadMessageCountMap.put(sender,chatterNotReadMessageCountMap.get(sender) - 1);
        }else {
            chatterNotReadMessageCountMap.put(sender,0);
        }
    }

    public void newVessageFromServer() {
        GetNewVessagesRequest req = new GetNewVessagesRequest();

        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if (isOk) {
                    List<Vessage> vsgs = new ArrayList<>();
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                Vessage vsg = realm.createOrUpdateObjectFromJson(Vessage.class, result.getJSONObject(i));
                                incChatterNotReadVessageCount(vsg.sender);
                                vsgs.add(vsg.copyToObject());
                            } catch (JSONException e) {
                                Log.d("Here", "Debug");
                            }
                        }
                        realm.commitTransaction();

                        if (vsgs.size() > 0) {
                            for (Vessage vsg : vsgs) {
                                postNotification(NOTIFY_NEW_VESSAGE_RECEIVED, vsg);
                            }
                            postNotification(NOTIFY_NEW_VESSAGES_RECEIVED, vsgs);

                            SoundManager.getInstance().playNewMessageRington();

                            notifyVessageGot();

                        }
                    }
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
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Vessage> results = realm.where(Vessage.class).equalTo("sender", chatterId).findAllSorted("ts", Sort.DESCENDING);
            if (results.size() > 0) {
                return results.first().copyToObject();
            }
/*
        else {
            String json = "{\"_id\": \"$newObjectId\",\"Video\": \"579eaacf9c46b95c3f884f9d\",\"TypeId\": 1,\"IsRead\": false,\"IsGroup\": false,\"SendTime\": \"$date\",\"VideoReady\": true,\"Sender\": \"579e91219c46b95c53194ba8\",\"Body\": \"{\\\"textMessage\\\":\\\"有疑问可以问我哦，对话将在两周后自动消失~\\\",\\\"textMessageShownEvent\\\":\\\"cGxheU5leHRCdXR0b25BbmltYXRpb24oKQ\\\"}\",\"ExtraInfo\": \"{}\"}";
            try {
                Vessage vsg = JsonHelper.parseObject(json,Vessage.class);
                vsg.vessageId = IDUtil.generateUniqueId();
                vsg.fileId = "579eaacf9c46b95c3f884f9d";
                vsg.typeId = Vessage.TYPE_IMAGE;
                vsg.isGroup = false;
                vsg.sendTime = DateHelper.toAccurateDateTimeString(new Date());
                vsg.sender = "579e91219c46b95c53194ba8";
                vsg.body = "{\"textMessage\":\"有问我有有问我有问我有问我有有问我有有问问我有有问我有问我有问我有有问我有有问问我有有问我有问我有问我有有问我有有问我有问我有问我有有问我有有问我有问我有问我有\",\"textMessageShownEvent\":\"cGxheU5leHRCdXR0b25BbmltYXRpb24oKQ\"}";
                return vsg;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
*/
            return null;
        }
    }

    public int getNotReadVessageCount(String chatterId){
        if(chatterNotReadMessageCountMap.containsKey(chatterId)){
            return chatterNotReadMessageCountMap.get(chatterId);
        }
        return 0;
    }

    public List<Vessage> getNotReadVessage(String chatterId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            List<Vessage> results = realm.where(Vessage.class).equalTo("sender", chatterId).equalTo("isRead", false).findAll();
            List<Vessage> vsgs = new ArrayList<>(results.size());
            for (Vessage result : results) {
                vsgs.add(result.copyToObject());
            }
            return vsgs;
        }
    }
}
