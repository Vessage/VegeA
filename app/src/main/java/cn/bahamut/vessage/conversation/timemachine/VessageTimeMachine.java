package cn.bahamut.vessage.conversation.timemachine;

import android.util.Log;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observer;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 2016/11/19.
 */

public class VessageTimeMachine {
    private static final String TAG = "VessageTimeMachine";
    private static VessageTimeMachine instance;

    public static void initTimeMachine(){
        if (instance == null){
            instance = new VessageTimeMachine();
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

    private void release(){
        SendVessageQueue.getInstance().deleteObserver(SendVessageQueue.ON_NEW_TASK_PUSHED, onVessageReceived);
        ServicesProvider.getService(VessageService.class).deleteObserver(VessageService.NOTIFY_VESSAGE_READ, onVessageReceived);
    }

    public void flush(){

    }

    private final Observer onVessageReceived = new Observer() {
        @Override
        public void update(ObserverState state) {
            Vessage vessage = null;
            String chatterKey = null;
            if (state.getNotifyType().equals(SendVessageQueue.ON_NEW_TASK_PUSHED)){
                SendVessageQueue.SendingTaskInfo info = (SendVessageQueue.SendingTaskInfo) state.getInfo();
                chatterKey = info.task.receiverId;
                vessage = info.task.vessage;
            }else if (state.getNotifyType().equals(VessageService.NOTIFY_VESSAGE_READ)){
                if(state.getInfo() instanceof Vessage){
                    vessage = (Vessage) state.getInfo();
                    chatterKey = vessage.sender;
                }
            }
            if (vessage != null && StringHelper.isStringNullOrWhiteSpace(chatterKey) == false){
                Log.i(TAG,String.format("Received A Vessage:%s",vessage.vessageId));
            }

        }
    };
}
