package cn.bahamut.vessage.conversation.sendqueue.handlers;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueStepHandler;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/8/2.
 */
public class FinishFileVessageHandler implements SendVessageQueueStepHandler {
    public static final String HANDLER_NAME = "FinishPostFileVessage";
    @Override
    public void initHandler(SendVessageQueue queue) {

    }

    @Override
    public void releaseHandler() {

    }

    @Override
    public void doTask(final SendVessageQueue queue, final SendVessageQueueTask task) {
        ServicesProvider.getService(VessageService.class).finishSendVessage(task.vessage.vessageId, task.vessage.fileId, new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, String sendedVessageId) {
                if(isOk){
                    queue.nextStep(task);
                }else {
                    queue.doTaskError(task, 0, "POST_FINISH_SEND_VESSAGE_ERROR");
                }
            }
        });
    }
}
