package cn.bahamut.vessage.conversation.sendqueue;

import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.services.vessage.Vessage;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/8/2.
 */
public class SendVessageQueueTask extends RealmObject {
    @PrimaryKey
    public String taskId;
    public String filePath;
    public String receiverId;
    public Vessage vessage;
    public String steps;
    public int currentStep;

    @Ignore
    private String[] stepsArray;

    private void initStepsArray(){
        if(stepsArray == null && steps != null){
            stepsArray = steps.split(";");
        }
    }

    public String getCurrentStepName(){
        initStepsArray();
        return stepsArray[currentStep];
    }

    public String[] getStepsArray(){
        initStepsArray();
        return stepsArray;
    };

    public void setTaskStep(String[] stepsArray){
        steps = StringHelper.stringsJoinSeparator(stepsArray,";");
    }

    public boolean isTaskFinished(){
        initStepsArray();
        return currentStep == stepsArray.length;
    }

    public SendVessageQueueTask copyToObject(){
        SendVessageQueueTask task = new SendVessageQueueTask();
        task.taskId = taskId;
        task.filePath = filePath;
        task.receiverId = receiverId;
        task.vessage = vessage.copyToObject();
        task.steps = steps;
        task.currentStep = currentStep;
        return task;
    }
}
