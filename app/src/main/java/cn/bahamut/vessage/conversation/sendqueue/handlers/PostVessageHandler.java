package cn.bahamut.vessage.conversation.sendqueue.handlers;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueue;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueStepHandler;
import cn.bahamut.vessage.conversation.sendqueue.SendVessageQueueTask;
import cn.bahamut.vessage.services.vessage.VessageService;

/**
 * Created by alexchow on 16/8/2.
 */
public class PostVessageHandler implements SendVessageQueueStepHandler {
    public static final String HANDLER_NAME = "PostVessage";
    @Override
    public void initHandler(SendVessageQueue queue) {

    }

    @Override
    public void releaseHandler() {

    }

    @Override
    public void doTask(final SendVessageQueue queue, final SendVessageQueueTask task) {
        final VessageService vessageService = ServicesProvider.getService(VessageService.class);
        vessageService.sendVessageToReceiver(task.receiverId, task.vessage, new VessageService.OnSendVessageCompleted() {
            @Override
            public void onSendVessageCompleted(boolean isOk, String sendedVessageId) {
                if(isOk){
                    queue.getRealm().beginTransaction();
                    task.returnVId = sendedVessageId;
                    queue.getRealm().commitTransaction();
                    queue.nextStep(task);
                }else {
                    queue.doTaskError(task, 0, "POST_VESSAGE_ERROR");
                }
            }
        });
    }
}
