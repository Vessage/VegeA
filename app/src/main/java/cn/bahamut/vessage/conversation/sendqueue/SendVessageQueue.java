package cn.bahamut.vessage.conversation.sendqueue;

import android.util.Log;

import java.util.HashMap;

import cn.bahamut.common.IDUtil;
import cn.bahamut.observer.Observable;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.vessage.Vessage;
import cn.bahamut.vessage.services.vessage.VessageService;
import io.realm.Realm;

/**
 * Created by alexchow on 16/4/12.
 */
public class SendVessageQueue extends Observable {
    public static final String ON_SENDED_VESSAGE = "ON_SENDED_VESSAGE";
    public static final String ON_SENDING_PROGRESS = "ON_SENDING_PROGRESS";
    public static final String ON_SEND_VESSAGE_FAILURE = "ON_SEND_VESSAGE_FAILURE";

    public class SendingTaskInfo {
        public static final int STATE_SENDING = 10;
        public static final int STATE_SENDED = 11;
        public static final int STATE_SEND_FAILURE = -1;
        public SendVessageQueueTask task;
        public double progress;
        public int taskStatusCode = 0;
        public int state = STATE_SENDING;
        public String message;
    }

    static private SendVessageQueue instance;
    public static SendVessageQueue getInstance() {
        if(instance == null){
            instance = new SendVessageQueue();
        }
        return instance;
    }

    private HashMap<String,SendVessageQueueStepHandler> stepHandlers;

    public void init() {
        stepHandlers = new HashMap<>();
    }

    public void release(){
        stepHandlers.clear();
    }

    public void registStepHandler(String handlerId,SendVessageQueueStepHandler handler){
        stepHandlers.put(handlerId,handler);
        handler.initHandler(this);
    }

    public Realm getRealm(){
        return ServicesProvider.getService(VessageService.class).getRealm();
    }

    public void pushSendVessageTask(String receiverId, Vessage vessage,String[] steps,String uploadFileUrl){
        getRealm().beginTransaction();
        Vessage vsg = getRealm().createObject(Vessage.class,IDUtil.generateUniqueId());
        vsg.setValuesByOther(vessage);
        SendVessageQueueTask task = getRealm().createObject(SendVessageQueueTask.class,IDUtil.generateUniqueId());
        task.receiverId = receiverId;
        task.vessage = vsg;
        task.filePath = uploadFileUrl;
        task.taskId = IDUtil.generateUniqueId();
        task.currentStep = -1;
        task.setTaskStep(steps);
        getRealm().commitTransaction();
        nextStep(task);
    }

    public void nextStep(SendVessageQueueTask task){
        getRealm().beginTransaction();
        task.currentStep+=1;
        getRealm().commitTransaction();
        if (task.isTaskFinished()){
            finishTask(task);
        }else {
            startTask(task);
        }
    }

    private void startTask(SendVessageQueueTask task) {
        SendVessageQueueStepHandler handler = stepHandlers.get(task.getCurrentStepName());
        if (handler != null) {
            Log.i("SendVessageQueue","Task Do Work -> " + task.taskId + ":" + task.getCurrentStepName());
            notifyTaskStepProgress(task, 0);
            handler.doTask(this, task);
        }
    }

    public boolean startTask(String taskId){
        SendVessageQueueTask task = getRealm().where(SendVessageQueueTask.class).equalTo("taskId",taskId).findFirst();
        if(task != null){
            startTask(task);
            return true;
        }
        return false;
    }

    private void finishTask(SendVessageQueueTask task) {
        Log.i("SendVessageQueue", "Task Finished -> " + task.taskId);
        notifyTaskStepProgress(task, 0);
        getRealm().beginTransaction();
        SendVessageQueueTask task1 = task.copyToObject();
        task.vessage.deleteFromRealm();
        task.deleteFromRealm();
        getRealm().commitTransaction();
        SendingTaskInfo taskInfo = generateSendingInfo(task1);
        taskInfo.state = SendingTaskInfo.STATE_SENDED;
        postNotification(ON_SENDED_VESSAGE, taskInfo);
    }

    public void doTaskError(SendVessageQueueTask task,int taskStatusCode, String errorMessage) {
        SendingTaskInfo taskInfo = generateSendingInfo(task);
        taskInfo.state = SendingTaskInfo.STATE_SEND_FAILURE;
        taskInfo.message = errorMessage;
        taskInfo.taskStatusCode = taskStatusCode;
        postNotification(ON_SEND_VESSAGE_FAILURE, taskInfo);
    }

    public void notifyTaskStepProgress(SendVessageQueueTask task, double stepProgress) {
        SendingTaskInfo taskInfo = generateSendingInfo(task);
        taskInfo.state = SendingTaskInfo.STATE_SENDING;
        taskInfo.progress += 1.0 / task.getStepsArray().length * stepProgress;
        postNotification(ON_SENDING_PROGRESS,taskInfo);
        Log.i("SendVessageQueue","Task Progressing -> " + task.taskId);
        Log.i("SendVessageQueue","Task Progress -> " + taskInfo.progress);
    }

    private SendingTaskInfo generateSendingInfo(SendVessageQueueTask task) {
        SendingTaskInfo state = new SendingTaskInfo();
        state.task = task.copyToObject();
        int totalStep = task.getStepsArray().length;
        state.progress = task.currentStep * 1.0 / totalStep;
        return state;
    }
}
