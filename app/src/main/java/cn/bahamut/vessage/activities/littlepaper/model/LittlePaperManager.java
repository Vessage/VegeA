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
    private Realm realm;

    public static LittlePaperManager getInstance(){
        return instance;
    }
    public static void initManager(){
        instance = new LittlePaperManager();
        instance.realm = Realm.getDefaultInstance();
        instance.reloadCachedData();
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
        List<LittlePaperMessage> msgs = getRealm().where(LittlePaperMessage.class).findAll();
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

    public interface OnOpenPaperMessageCallback{
        void onOpenPaperMessage(LittlePaperMessage openedMessage, String error);
    }

    public void openPaperMessage(String paperId, final OnOpenPaperMessageCallback callback)  {
        OpenPaperMessageRequest req = new OpenPaperMessageRequest();
        req.setPaperId(paperId);

        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    getRealm().beginTransaction();
                    LittlePaperMessage msg = getRealm().createOrUpdateObjectFromJson(LittlePaperMessage.class,result);
                    msg.isUpdated = false;
                    getRealm().commitTransaction();
                    List<LittlePaperMessage> msgList = getMyNotDealMessages();
                    for (int i = 0; i < msgList.size(); i++) {
                        LittlePaperMessage littlePaperMessage = msgList.get(i);
                        if(littlePaperMessage.paperId.equals(msg.paperId)){
                            msgList.remove(i);
                            getMyOpenedMessages().add(msg);
                            break;
                        }
                    }
                    callback.onOpenPaperMessage(msg,LocalizedStringHelper.getLocalizedString(R.string.unknow_error));
                }else if (statusCode == 400){
                    callback.onOpenPaperMessage(null,LocalizedStringHelper.getLocalizedString(R.string.little_paper_no_such_paper));
                }else if (statusCode == 403){
                    callback.onOpenPaperMessage(null,LocalizedStringHelper.getLocalizedString(R.string.little_paper_is_opened));
                }else{
                    callback.onOpenPaperMessage(null,LocalizedStringHelper.getLocalizedString(R.string.unknow_error));
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
                            getRealm().beginTransaction();
                            littlePaperMessage.updatedTime = DateHelper.toAccurateDateTimeString(new Date());
                            if(littlePaperMessage.postmenString != null){
                                littlePaperMessage.postmenString += myUserId + ";";
                            }else {
                                littlePaperMessage.postmenString = myUserId + ";";
                            }
                            getRealm().commitTransaction();
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
                originUpdatedTime.put(message.paperId, DateHelper.stringToAccurateDate(message.updatedTime).getTime());
                msgs.add(message.paperId);
            }
        }
        for (LittlePaperMessage message : getMyPostededMessages()) {
            if(!message.isOpened){
                originUpdatedTime.put(message.paperId, DateHelper.stringToAccurateDate(message.updatedTime).getTime());
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
                    getRealm().beginTransaction();
                    for (int i = 0; i < result.length(); i++) {

                        try {
                            JSONObject object = result.getJSONObject(i);
                            LittlePaperMessage newMsg = getRealm().createOrUpdateObjectFromJson(LittlePaperMessage.class,object);
                            newMsg.reSetPostMenFromJsonObject(object);
                            if(originUpdatedTime.get(newMsg.paperId) < newMsg.getUpdatedTime().getTime()){
                                newMsg.isUpdated = true;
                                updated++;
                            }
                        } catch (JSONException e) {

                        }
                    }
                    getRealm().commitTransaction();
                    onPaperMessageUpdated.onPaperMessageUpdated(updated);
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
                getRealm().beginTransaction();
                msg.isUpdated = false;
                getRealm().commitTransaction();
            }
        }
    }

    public void removePaperMessage(int type,int index) {
        if (paperMessagesList.length > type && paperMessagesList[type].size() > index ) {
            LittlePaperMessage msg = paperMessagesList[type].remove(index);
            getRealm().beginTransaction();
            msg.deleteFromRealm();
            getRealm().commitTransaction();
        }
    }

    public void clearPaperMessageList(int type) {
        getRealm().beginTransaction();
        for (LittlePaperMessage littlePaperMessage : paperMessagesList[type]) {
            littlePaperMessage.deleteFromRealm();
        }
        paperMessagesList[type].clear();
        getRealm().commitTransaction();
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
                    getRealm().beginTransaction();
                    for (int i = 0; i < result.length(); i++) {
                        try {
                            JSONObject object = result.getJSONObject(i);
                            LittlePaperMessage newMsg = getRealm().createOrUpdateObjectFromJson(LittlePaperMessage.class,object);
                            newMsg.isUpdated = true;
                            newMsg.reSetPostMenFromJsonObject(object);
                        } catch (JSONException e) {

                        }
                    }
                    getRealm().commitTransaction();
                    onPaperMessageUpdated.onPaperMessageUpdated(result.length());
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
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    getRealm().beginTransaction();
                    LittlePaperMessage msg = getRealm().createOrUpdateObjectFromJson(LittlePaperMessage.class,result);
                    getMySendedMessages().add(0,msg);
                    getRealm().commitTransaction();
                }
                callback.onNewPaperMessagePost(isOk);
            }
        });
    }
}
