package cn.bahamut.vessage.conversation.sendqueue;

/**
 * Created by alexchow on 16/8/2.
 */
public interface SendVessageQueueStepHandler {
    void initHandler(SendVessageQueue queue);
    void releaseHandler();
    void doTask(SendVessageQueue queue,SendVessageQueueTask task);
}
