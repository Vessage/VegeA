package cn.bahamut.vessage.conversation.vessagehandler;

import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public interface VessageHandler {
    void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage);
    void releaseHandler();
}
