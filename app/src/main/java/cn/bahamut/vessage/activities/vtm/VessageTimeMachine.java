package cn.bahamut.vessage.activities.vtm;

import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by alexchow on 2016/11/19.
 */

public class VessageTimeMachine {

    private Realm realm;

    public class VessageTimeMachineRecordItem{
        public String chatterId;
        public Vessage vessage;
    }

    private static final String TAG = "VessageTimeMachine";
    private static VessageTimeMachine instance;

    public static void initTimeMachine(){
        if (instance == null){
            instance = new VessageTimeMachine();
            instance.init();
        }
    }

    public static void releaseTimeMachine(){
        if (instance != null){
            instance.release();
            instance = null;
        }
    }

    public static VessageTimeMachine getInstance() {
        return instance;
    }

    private VessageTimeMachine(){
        SendVessageQueue.getInstance().addObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onVessageReceived);
        ServicesProvider.getService(VessageService.class).addObserver(VessageService.NOTIFY_VESSAGE_READ, onVessageReceived);
    }

    private void init() {
        realm = Realm.getDefaultInstance();
    }

    private void release(){

        realm = null;
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onVessageReceived);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_VESSAGE_READ, onVessageReceived);
    }

    public VessageTimeMachineRecordItem[] getVessageRecords(String chatter,long ts,int limit){
        List<VTMRecord> resultSet = realm.where(VTMRecord.class).equalTo("chatterId",chatter).lessThan("mtime",ts).findAllSorted("mtime", Sort.DESCENDING).subList(0,limit);
        VessageTimeMachineRecordItem[] result = new VessageTimeMachineRecordItem[resultSet.size()];
        Gson gson = new Gson();
        int i = 0;
        for (VTMRecord vtmRecord : resultSet) {
            VessageTimeMachineRecordItem item = new VessageTimeMachineRecordItem();
            item.chatterId = vtmRecord.chatterId;
            item.vessage = gson.fromJson(vtmRecord.modelValue,Vessage.class);
            result[i++] = item;
        }
        return result;
    }

    private final Observer onVessageReceived = new Observer() {
        @Override
        public void update(ObserverState state) {
            Vessage vessage = null;
            String chatterKey = null;
            if (state.getNotifyType().equals(SendVessageQueue.ON_NEW_TASK_PUSHED)){
                SendVessageQueueTask task = (SendVessageQueueTask) state.getInfo();
                chatterKey = task.receiverId;
                vessage = task.vessage;
            }else if (state.getNotifyType().equals(VessageService.NOTIFY_VESSAGE_READ)){
                if(state.getInfo() instanceof Vessage){
                    vessage = (Vessage) state.getInfo();
                    chatterKey = vessage.sender;
                }
            }
            if (vessage != null && StringHelper.isStringNullOrWhiteSpace(chatterKey) == false){
                realm.beginTransaction();
                VTMRecord record = realm.createObject(VTMRecord.class);
                record.chatterId = vessage.sender;
                record.ctime = DateHelper.getUnixTimeSpan();
                record.mtime = vessage.ts;
                String modelValue = new Gson().toJson(vessage);
                record.modelValue = modelValue;
                realm.commitTransaction();
                Log.i(TAG,String.format("Received A Vessage:%s",vessage.vessageId));
            }

        }
    };
}
