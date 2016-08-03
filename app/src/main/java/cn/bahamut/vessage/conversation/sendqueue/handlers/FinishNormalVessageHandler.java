package cn.bahamut.vessage.conversation.sendqueue.handlers;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueStepHandler;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/8/2.
 */
public class FinishNormalVessageHandler implements SendVessageQueueStepHandler {
    public static final String HANDLER_NAME = "FinishPostNormalVessage";
    @Override
    public void initHandler(SendVessageQueue queue) {

    }

    @Override
    public void releaseHandler() {

    }

    @Override
    public void doTask(SendVessageQueue queue, SendVessageQueueTask task) {
        ServicesProvider.getService(VessageService.class).finishSendVessage(task.vessage.vessageId);
        queue.nextStep(task);
    }
}
