package cn.bahamut.vessage.conversation.vessagehandler;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;

import cn.bahamut.vessage.conversation.view.ConversationViewActivity;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public class VessageHandlerBase implements VessageHandler,VessageGestureHandler {
    protected ViewGroup vessageContainer;
    protected ConversationViewPlayManager playVessageManager;
    protected Vessage presentingVessage;

    protected Context getContext(){
        return playVessageManager.getConversationViewActivity();
    }

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
    public boolean onFling(int direction, float velocityX, float velocityY) {
        if (direction == FlingDerection.LEFT){
            playVessageManager.tryShowNextVessage();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }
}
