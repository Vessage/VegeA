package cn.bahamut.vessage.activities.littlepaper.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.DateHelper;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.UserService;
import io.realm.Realm;

/**
 * Created by alexchow on 16/5/19.
 */
public class LittlePaperManager {
    public static final String LITTLE_PAPER_ACTIVITY_ID = "1000";
    static private LittlePaperManager instance;
    private List<LittlePaperReadResponse> readPaperResponses;
    private Realm realm;

    public static LittlePaperManager getInstance(){
        return instance;
    }
    public static void initManager(){
        instance = new LittlePaperManager();
        instance.realm = Realm.getDefaultInstance();
        instance.reloadCachedData();
        instance.reloadReadResponses();
    }

    public static void releaseManager(){
        instance.realm.close();
        instance.realm = null;
        instance = null;
    }

    public static final int TYPE_MY_SENDED = 3;
    public static final int TYPE_MY_OPENED = 2;
    public static final int TYPE_MY_POSTED = 1;
    public static final int TYPE_MY_NOT_DEAL = 0;

    private List<LittlePaperMessage>[] paperMessagesList = new List[]{
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()};

    public List<LittlePaperMessage> getMySendedMessages(){
        return getTypedMessages(TYPE_MY_SENDED);
    }

    public List<LittlePaperMessage> getMyOpenedMessages(){
        return getTypedMessages(TYPE_MY_OPENED);
    }

    public List<LittlePaperMessage> getMyPostededMessages(){
        return getTypedMessages(TYPE_MY_POSTED);
    }

    public List<LittlePaperMessage> getMyNotDealMessages(){
        return getTypedMessages(TYPE_MY_NOT_DEAL);
    }

    public List<LittlePaperMessage> getTypedMessages(int type){
        return paperMessagesList[type];
    }

    public int getTypedMessagesUpdateCount(int type){
        int sum = 0;
        for (LittlePaperMessage littlePaperMessage : paperMessagesList[type]) {
            if(littlePaperMessage.isUpdated){
                sum += 1;
            }
        }
        return sum;
    }

    public int getTotalBadgeCount(){
        int sum = 0;
        for (List<LittlePaperMessage> littlePaperMessages : paperMessagesList) {
            for (LittlePaperMessage littlePaperMessage : littlePaperMessages) {
                if(littlePaperMessage.isUpdated){
                    sum += 1;
                }
            }
        }
        return sum;
    }

    private String myUserId;

    public void reloadCachedData(){
        for (List<LittlePaperMessage> messageList : paperMessagesList) {
            messageList.clear();
        }
        myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
        List<LittlePaperMessage> msgs = realm.where(LittlePaperMessage.class).findAll();
        for (LittlePaperMessage msg : msgs) {
            if(msg.isMySended(myUserId)){
                paperMessagesList[TYPE_MY_SENDED].add(msg);
            }else if (msg.isMyOpened(myUserId)){
                paperMessagesList[TYPE_MY_OPENED].add(msg);
            }else if (msg.isMyPosted(myUserId)){
                paperMessagesList[TYPE_MY_POSTED].add(msg);
            }else if (msg.isReceivedNotDeal(myUserId)){
                paperMessagesList[TYPE_MY_NOT_DEAL].add(msg);
            }
        }
    }

    public LittlePaperMessage getPaperMessageByPaperId(String paperId) {
        for (List<LittlePaperMessage> paperMessages : paperMessagesList) {
            for (LittlePaperMessage paperMessage : paperMessages) {
                if(paperId.equals(paperMessage.paperId)){
                    return paperMessage;
                }
            }
        }
        return null;
    }

    public Realm getRealm() {
        return realm;
    }

    public List<LittlePaperReadResponse> getReadPaperResponses() {
        if(readPaperResponses == null){
            readPaperResponses = new ArrayList<>();
        }
        return readPaperResponses;
    }

    public int getResponsesBadge() {
        return (int)realm.where(LittlePaperReadResponse.class).equalTo("isRead",false).count();
    }

    public void refreshPaperMessageById(String paperId, final LittlePaperManagerOperateCallback callback) {
        GetPaperMessagesStatusRequest req = new GetPaperMessagesStatusRequest();
        req.setPaperId(new String[]{ paperId });
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if(isOk){
                    realm.beginTransaction();
                    for (int i = 0; i < result.length(); i++) {

                        try {
                            JSONObject object = result.getJSONObject(i);
                            LittlePaperMessage newMsg = realm.createOrUpdateObjectFromJson(LittlePaperMessage.class,object);
                            newMsg.setPrimitiveArrayValues(object);
                        } catch (JSONException e) {

                        }
                    }
                    realm.commitTransaction();
                    reloadCachedData();
                    callback.onCallback(isOk,null);
                }else {
                    callback.onCallback(false,null);
                }
            }
        });
    }

    public interface OnOpenPaperMessageCallback{
        void onOpenPaperMessage(LittlePaperMessage openedMessage, String error);
    }

    public void openAcceptlessPaperMessage(String paperId, final OnOpenPaperMessageCallback callback)  {
        OpenAcceptlessPaperRequest req = new OpenAcceptlessPaperRequest();
        req.setPaperId(paperId);

        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    realm.beginTransaction();
                    LittlePaperMessage msg = realm.createOrUpdateObjectFromJson(LittlePaperMessage.class,result);
                    msg.isUpdated = false;
                    realm.commitTransaction();
                    List<LittlePaperMessage> msgList = getMyNotDealMessages();
                    for (int i = 0; i < msgList.size(); i++) {
                        LittlePaperMessage littlePaperMessage = msgList.get(i);
                        if(littlePaperMessage.paperId.equals(msg.paperId)){
                            msgList.remove(i);
                            getMyOpenedMessages().add(msg);
                            break;
                        }
                    }
                    callback.onOpenPaperMessage(msg,LocalizedStringHelper.getLocalizedString(R.string.little_paper_unknow_error));
                }else if (statusCode == 400){
                    callback.onOpenPaperMessage(null,LocalizedStringHelper.getLocalizedString(R.string.little_paper_no_such_paper));
                }else if (statusCode == 403){
                    callback.onOpenPaperMessage(null,LocalizedStringHelper.getLocalizedString(R.string.little_paper_is_opened));
                }else{
                    callback.onOpenPaperMessage(null,LocalizedStringHelper.getLocalizedString(R.string.little_paper_unknow_error));
                }
            }
        });
    }

    public interface OnPostPaperToNextUserCallback{
        void onPostPaperToNextUser(boolean suc, String message);
    }
    public void postPaperToNextUser(final String paperId, String userId, boolean isAnonymous, final OnPostPaperToNextUserCallback callback)  {
        PostPaperMessageRequest req = new PostPaperMessageRequest();
        req.setIsAnonymous(isAnonymous);
        req.setNextReceiver(userId);
        req.setPaperId(paperId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                String msg = "SUCCESS";
                if (statusCode == 400){
                    msg = LocalizedStringHelper.getLocalizedString(R.string.little_paper_no_such_paper);
                }else if (statusCode == 403){
                    msg = LocalizedStringHelper.getLocalizedString(R.string.little_paper_posted_by_user);
                }else if (isOk){
                    List<LittlePaperMessage> msgList = getMyNotDealMessages();
                    for (int i = 0; i < msgList.size(); i++) {
                        LittlePaperMessage littlePaperMessage = paperMessagesList[TYPE_MY_NOT_DEAL].get(i);
                        if(littlePaperMessage.paperId.equals(paperId)){
                            msgList.remove(i);
                            realm.beginTransaction();
                            littlePaperMessage.uTs = DateHelper.getUnixTimeSpan();
                            if(littlePaperMessage.postmenString != null){
                                littlePaperMessage.postmenString += myUserId + ";";
                            }else {
                                littlePaperMessage.postmenString = myUserId + ";";
                            }
                            realm.commitTransaction();
                            getMyPostededMessages().add(0,littlePaperMessage);
                            break;
                        }
                    }
                }
                callback.onPostPaperToNextUser(isOk,msg);
            }
        });
    }

    public void refreshPaperMessage(final OnPaperMessageUpdated onPaperMessageUpdated) {
        GetPaperMessagesStatusRequest req = new GetPaperMessagesStatusRequest();
        List<String> msgs = new LinkedList<>();
        final Map<String,Long> originUpdatedTime = new HashMap<String, Long>();
        for (LittlePaperMessage message : getMySendedMessages()) {
            if(!message.isOpened){
                originUpdatedTime.put(message.paperId, message.uTs);
                msgs.add(message.paperId);
            }
        }
        for (LittlePaperMessage message : getMyPostededMessages()) {
            if(!message.isOpened){
                originUpdatedTime.put(message.paperId, message.uTs);
                msgs.add(message.paperId);
            }
        }
        if(msgs.size() == 0){
            onPaperMessageUpdated.onPaperMessageUpdated(0);
            return;
        }
        req.setPaperId(msgs.toArray(new String[0]));
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                int updated = 0;
                if(isOk){
                    realm.beginTransaction();
                    for (int i = 0; i < result.length(); i++) {

                        try {
                            JSONObject object = result.getJSONObject(i);
                            LittlePaperMessage newMsg = realm.createOrUpdateObjectFromJson(LittlePaperMessage.class,object);
                            newMsg.setPrimitiveArrayValues(object);
                            if(originUpdatedTime.get(newMsg.paperId) < newMsg.getUpdatedTime().getTime()){
                                newMsg.isUpdated = true;
                                updated++;
                            }
                        } catch (JSONException e) {

                        }
                    }
                    realm.commitTransaction();
                    onPaperMessageUpdated.onPaperMessageUpdated(updated);
                }else {
                    onPaperMessageUpdated.onPaperMessageUpdated(0);
                }
            }
        });
    }

    public void clearPaperMessageUpdated(int type,int index) {
        if(type == TYPE_MY_NOT_DEAL){
            return;
        }
        if (paperMessagesList.length > type && paperMessagesList[type].size() > index ) {
            LittlePaperMessage msg = paperMessagesList[type].get(index);
            if (msg.isUpdated) {
                realm.beginTransaction();
                msg.isUpdated = false;
                realm.commitTransaction();
            }
        }
    }

    public void removePaperMessage(int type,int index) {
        if (paperMessagesList.length > type && paperMessagesList[type].size() > index ) {
            LittlePaperMessage msg = paperMessagesList[type].remove(index);
            realm.beginTransaction();
            msg.deleteFromRealm();
            realm.commitTransaction();
        }
    }

    public void clearPaperMessageList(int type) {
        realm.beginTransaction();
        for (LittlePaperMessage littlePaperMessage : paperMessagesList[type]) {
            littlePaperMessage.deleteFromRealm();
        }
        paperMessagesList[type].clear();
        realm.commitTransaction();
    }

    public interface OnPaperMessageUpdated {
        void onPaperMessageUpdated(int updated);
    }
    public void getPaperMessages(final OnPaperMessageUpdated onPaperMessageUpdated){
        GetReceivedPaperMessagesRequest req = new GetReceivedPaperMessagesRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if(isOk){
                    realm.beginTransaction();
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject object = result.getJSONObject(i);
                            LittlePaperMessage newMsg = realm.createOrUpdateObjectFromJson(LittlePaperMessage.class,object);
                            newMsg.isUpdated = true;
                            newMsg.setPrimitiveArrayValues(object);
                        } catch (JSONException e) {

                        }
                    }
                    realm.commitTransaction();
                    onPaperMessageUpdated.onPaperMessageUpdated(result.length());
                }else {
                    onPaperMessageUpdated.onPaperMessageUpdated(0);
                }
            }
        });
    }


    public interface OnNewPaperMessagePost{
        void onNewPaperMessagePost(boolean suc);
    }

    public void newPaperMessage(String message, String receiverInfo, String nextReceiver, final OnNewPaperMessagePost callback)  {
        NewPaperMessageRequest req = new NewPaperMessageRequest();
        req.setMessage(message);
        req.setNextReceiver(nextReceiver);
        req.setReceiverInfo(receiverInfo);
        req.setOpenNeedAccept(true);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    realm.beginTransaction();
                    LittlePaperMessage msg = realm.createOrUpdateObjectFromJson(LittlePaperMessage.class,result);
                    getMySendedMessages().add(0,msg);
                    realm.commitTransaction();
                }
                callback.onNewPaperMessagePost(isOk);
            }
        });
    }

    public interface LittlePaperManagerOperateCallback{
        void onCallback(boolean isOk,String errorMessage);
    }

    public void getReadResponses(final LittlePaperManagerOperateCallback callback){
        GetReadPaperResponsesRequest req = new GetReadPaperResponsesRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if(isOk){
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(LittlePaperReadResponse.class,result);
                    realm.commitTransaction();
                    reloadReadResponses();
                    callback.onCallback(isOk,null);
                    clearGotResponses();
                }else {
                    callback.onCallback(false,LocalizedStringHelper.getLocalizedString(R.string.network_error));
                }
            }
        });
    }

    private void reloadReadResponses() {
        getReadPaperResponses().clear();
        List results = realm.where(LittlePaperReadResponse.class).findAll();
        getReadPaperResponses().addAll(results);
    }

    private void clearGotResponses(){
        ClearGotResponsesRequest req = new ClearGotResponsesRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {

            }
        });
    }

    public void askReadPaper(final String paperId, final LittlePaperManagerOperateCallback callback){
        AskSenderReadPaperRequest req = new AskSenderReadPaperRequest();
        req.setPaperId(paperId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    callback.onCallback(true,null);
                }else {
                    try {
                        callback.onCallback(false,result.getString("msg"));
                    } catch (JSONException e) {
                        callback.onCallback(false,LocalizedStringHelper.getLocalizedString(R.string.little_paper_unknow_error));
                    }
                }
            }
        });
    }

    public void acceptReadPaper(final String paperId, String reader, final LittlePaperManagerOperateCallback callback){
        AcceptReadPaperRequest req = new AcceptReadPaperRequest();
        req.setPaperId(paperId);
        req.setReader(reader);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    removeReadResponse(paperId);
                    callback.onCallback(true,null);
                }else {
                    try {
                        callback.onCallback(false,result.getString("msg"));
                    } catch (JSONException e) {
                        callback.onCallback(false,LocalizedStringHelper.getLocalizedString(R.string.little_paper_unknow_error));
                    }
                }
            }
        });
    }

    public void rejectReadPaper(final String paperId, String reader, final LittlePaperManagerOperateCallback callback){
        RejectReadPaperRequest req = new RejectReadPaperRequest();
        req.setPaperId(paperId);
        req.setReader(reader);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    removeReadResponse(paperId);
                    callback.onCallback(true,null);
                }else {
                    try {
                        callback.onCallback(false,result.getString("msg"));
                    } catch (JSONException e) {
                        callback.onCallback(false,LocalizedStringHelper.getLocalizedString(R.string.little_paper_unknow_error));
                    }
                }
            }
        });
    }

    public void removeReadResponse(String paperId){
        for (int i = readPaperResponses.size() - 1; i >= 0; i--) {
            if(readPaperResponses.get(i).paperId.equals(paperId)){
                readPaperResponses.remove(i);
            }
        }
        realm.beginTransaction();
        List<LittlePaperReadResponse> results = realm.where(LittlePaperReadResponse.class).equalTo("paperId",paperId).findAll();
        for (LittlePaperReadResponse resp : results) {
            resp.deleteFromRealm();
        }
        realm.commitTransaction();
    }
}
