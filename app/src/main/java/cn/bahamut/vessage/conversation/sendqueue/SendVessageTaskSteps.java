package cn.bahamut.vessage.conversation.sendqueue;

import cn.bahamut.vessage.conversation.sendqueue.handlers.FinishNormalVessageHandler;
import cn.bahamut.vessage.conversation.sendqueue.handlers.PostVessageHandler;
import cn.bahamut.vessage.conversation.sendqueue.handlers.SendAliOSSFileHandler;

/**
 * Created by alexchow on 16/8/2.
 */
public class SendVessageTaskSteps {
    public static final String[] SEND_NORMAL_VESSAGE_STEPS = new String[]{PostVessageHandler.HANDLER_NAME, FinishNormalVessageHandler.HANDLER_NAME};
    public static final String[] SEND_FILE_VESSAGE_STEPS = new String[]{SendAliOSSFileHandler.HANDLER_NAME, PostVessageHandler.HANDLER_NAME, FinishNormalVessageHandler.HANDLER_NAME};
}
