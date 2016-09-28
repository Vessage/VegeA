package cn.bahamut.vessage.conversation.vessagehandler;

import android.view.ViewGroup;

import cn.bahamut.vessage.conversation.view.ConversationViewActivity;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public class VessageHandlerBase implements VessageHandler {
    protected ViewGroup vessageContainer;
    protected ConversationViewPlayManager playVessageManager;
    protected Vessage presentingVessage;

    public VessageHandlerBase(ConversationViewPlayManager playVessageManager,ViewGroup vessageContainer){
        this.vessageContainer = vessageContainer;
        this.playVessageManager = playVessageManager;
    }

    @Override
    public void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage) {
        this.presentingVessage = newVessage;
    }

    @Override
    public void releaseHandler() {
        playVessageManager = null;
        presentingVessage = null;
    }

    @Override
    public void onFling(int direction, float velocityX, float velocityY) {
        if (direction == ConversationViewActivity.FlingDerection.LEFT){
            playVessageManager.tryShowNextVessage();
        }
    }
}
