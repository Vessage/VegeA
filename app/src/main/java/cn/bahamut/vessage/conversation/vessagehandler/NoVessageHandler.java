package cn.bahamut.vessage.conversation.vessagehandler;

import android.view.View;
import android.view.ViewGroup;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public class NoVessageHandler extends VessageHandlerBase {
    private View noVessageView;
    public NoVessageHandler(ConversationViewPlayManager playVessageManager, ViewGroup vessageContainer) {
        super(playVessageManager, vessageContainer);
        noVessageView = playVessageManager.getConversationViewActivity().getLayoutInflater().inflate(R.layout.no_vessage_container,null);
    }

    @Override
    public void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage) {
        super.onPresentingVessageSeted(oldVessage, newVessage);
        vessageContainer.removeAllViews();
        vessageContainer.addView(noVessageView);
    }
}
