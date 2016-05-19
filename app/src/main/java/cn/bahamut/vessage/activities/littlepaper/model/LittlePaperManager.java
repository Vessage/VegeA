package cn.bahamut.vessage.activities.littlepaper.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.UserService;
import io.realm.Realm;

/**
 * Created by alexchow on 16/5/19.
 */
public class LittlePaperManager {
    static private LittlePaperManager instance;
    public static void initManager(){
        instance = new LittlePaperManager();
        instance.loadCachedData();
    }

    public static void releaseManager(){
        instance = null;
    }

    public static int TYPE_MY_SENDED = 3;
    public static int TYPE_MY_OPENED = 2;
    public static int TYPE_MY_POSTED = 1;
    public static int TYPE_MY_NOT_DEAL = 0;

    private List<LittlePaperMessage>[] paperMessagesList = new List[]{
            new ArrayList<LittlePaperMessage>(),
            new ArrayList<LittlePaperMessage>(),
            new ArrayList<LittlePaperMessage>(),
            new ArrayList<LittlePaperMessage>()};

    public List<LittlePaperMessage> getMySendedMessages(){
        return paperMessagesList[TYPE_MY_SENDED];
    }

    public List<LittlePaperMessage> getMyOpenedMessages(){
        return paperMessagesList[TYPE_MY_OPENED];
    }

    public List<LittlePaperMessage> getMyPostededMessages(){
        return paperMessagesList[TYPE_MY_POSTED];
    }

    public List<LittlePaperMessage> getMyNotDealMessages(){
        return paperMessagesList[TYPE_MY_NOT_DEAL];
    }

    private int getTypedMessagesUpdateCount(int type){
        int sum = 0;
        for (LittlePaperMessage littlePaperMessage : paperMessagesList[type]) {
            if(littlePaperMessage.isUpdated){
                sum += 1;
            }
        }
        return sum;
    }

    public int getSendedMessageUpdatedCount(){
        return getTypedMessagesUpdateCount(TYPE_MY_SENDED);
    }

    public int getPostededMessageUpdatedCount(){
        return getTypedMessagesUpdateCount(TYPE_MY_POSTED);
    }

    public int getNotDealMessageUpdatedCount(){
        return getTypedMessagesUpdateCount(TYPE_MY_NOT_DEAL);
    }

    public int getOpenedMessageUpdatedCount(){
        return getTypedMessagesUpdateCount(TYPE_MY_OPENED);
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

    private void loadCachedData(){
        myUserId = ServicesProvider.getService(UserService.class).getMyProfile().userId;
        List<LittlePaperMessage> msgs = Realm.getDefaultInstance().where(LittlePaperMessage.class).findAll();
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
                    Realm.getDefaultInstance().beginTransaction();
                    LittlePaperMessage msg = Realm.getDefaultInstance().createOrUpdateObjectFromJson(LittlePaperMessage.class,result);
                    Realm.getDefaultInstance().commitTransaction();
                    List<LittlePaperMessage> msgList = getMyNotDealMessages();
                    for (int i = 0; i < msgList.size(); i++) {
                        LittlePaperMessage littlePaperMessage = msgList.get(i);
                        if(littlePaperMessage.paperId.equals(msg.paperId)){
                            msgList.remove(i);
                            getMyOpenedMessages().add(msg);
                            break;
                        }
                    }
                    callback.onOpenPaperMessage(msg,"UNKNOW_ERROR");
                }else if (statusCode == 400){
                    callback.onOpenPaperMessage(null,"NO_SUCH_PAPER_ID");
                }else if (statusCode == 403){
                    callback.onOpenPaperMessage(null,"PAPER_OPENED");
                }else{
                    callback.onOpenPaperMessage(null,"UNKNOW_ERROR");
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
                    msg = "NO_SUCH_PAPER_ID";
                }else if (statusCode == 403){
                    msg = "USER_POSTED_THIS_PAPER";
                }else if (isOk){
                    List<LittlePaperMessage> msgList = getMyNotDealMessages();
                    for (int i = 0; i < msgList.size(); i++) {
                        LittlePaperMessage littlePaperMessage = paperMessagesList[TYPE_MY_NOT_DEAL].get(i);
                        if(littlePaperMessage.paperId.equals(paperId)){
                            msgList.remove(i);
                            Realm.getDefaultInstance().beginTransaction();
                            if(littlePaperMessage.postmen != null){
                                Set<String> set = new HashSet<String>();
                                for (String s : littlePaperMessage.postmen) {
                                    set.add(s);
                                }
                                set.add(myUserId);
                                littlePaperMessage.postmen = set.toArray(new String[0]);
                            }else {
                                littlePaperMessage.postmen = new String[]{myUserId};
                            }
                            Realm.getDefaultInstance().commitTransaction();
                            getMyPostededMessages().add(0,littlePaperMessage);
                            break;
                        }
                    }
                }
                callback.onPostPaperToNextUser(isOk,msg);
            }
        });
    }

    public void refreshPaperMessage() {
        GetPaperMessagesStatusRequest req = new GetPaperMessagesStatusRequest();
        List<String> msgs = new LinkedList<>();
        for (LittlePaperMessage littlePaperMessage : getMySendedMessages()) {
            if(!littlePaperMessage.isOpened){
                msgs.add(littlePaperMessage.paperId);
            }
        }
        for (LittlePaperMessage littlePaperMessage : getMyPostededMessages()) {
            if(!littlePaperMessage.isOpened){
                msgs.add(littlePaperMessage.paperId);
            }
        }
        if(msgs.size() == 0){
            return;
        }
        req.setPaperId(msgs.toArray(new String[0]));
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {

            }
        });

//        BahamutRFKit.getClient(APIClient.class).execute(req) { (result:SLResult<[LittlePaperMessage]>) in
//            var updated = 0
//            if let resultMsgs = result.returnObject{
//                for m in resultMsgs{
//                    if let msg = (msgs.filter{$0.paperId == m.paperId}).first{
//                        if msg.updatedTime.dateTimeOfAccurateString.isBefore(m.updatedTime.dateTimeOfAccurateString){
//                            m.isUpdated = true
//                            updated += 1
//                            m.saveModel()
//                            if (self.mySendedMessages.removeElement{$0.paperId == m.paperId}).count > 0{
//                                self.mySendedMessages.insert(m, atIndex: 0)
//                            }else if(self.myPostededMessages.removeElement{$0.paperId == m.paperId}).count > 0{
//                                self.myPostededMessages.insert(m, atIndex: 0)
//                            }
//                        }
//                    }
//                }
//            }
//            callback(updated:updated)
//        }
    }

    public void clearPaperMessageUpdated(int type,int index) {
        if (paperMessagesList.length > type && paperMessagesList[type].size() > index ) {
            LittlePaperMessage msg = paperMessagesList[type].get(index);
            if (msg.isUpdated) {
                Realm.getDefaultInstance().beginTransaction();
                msg.isUpdated = false;
                Realm.getDefaultInstance().commitTransaction();
            }
        }
    }

    public void removePaperMessage(int type,int index) {
        if (paperMessagesList.length > type && paperMessagesList[type].size() > index ) {
            LittlePaperMessage msg = paperMessagesList[type].remove(index);
            Realm.getDefaultInstance().beginTransaction();
            msg.deleteFromRealm();
            Realm.getDefaultInstance().commitTransaction();
        }
    }

    public void clearPaperMessageList(int type) {
        Realm.getDefaultInstance().beginTransaction();
        for (LittlePaperMessage littlePaperMessage : paperMessagesList[type]) {
            littlePaperMessage.deleteFromRealm();
        }
        paperMessagesList[type].clear();
        Realm.getDefaultInstance().commitTransaction();
    }

    public void getPaperMessages(){
        GetReceivedPaperMessagesRequest req = new GetReceivedPaperMessagesRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if(isOk){
                    Realm.getDefaultInstance().beginTransaction();
                    Realm.getDefaultInstance().createOrUpdateAllFromJson(LittlePaperMessage.class,result);
                    Realm.getDefaultInstance().commitTransaction();
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
                    Realm.getDefaultInstance().beginTransaction();
                    LittlePaperMessage msg = Realm.getDefaultInstance().createOrUpdateObjectFromJson(LittlePaperMessage.class,result);
                    getMySendedMessages().add(0,msg);
                    Realm.getDefaultInstance().commitTransaction();
                }
                callback.onNewPaperMessagePost(isOk);
            }
        });
    }
}
