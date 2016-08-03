package cn.bahamut.vessage.conversation.vessagehandler;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.view.ConversationViewPlayManager;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 16/8/3.
 */
public class UnknowVessageHandler extends VessageHandlerBase {
    private final View vessageView;

    public UnknowVessageHandler(ConversationViewPlayManager playVessageManager, ViewGroup vessageContainer) {
        super(playVessageManager,vessageContainer);
        vessageView = playVessageManager.getConversationViewActivity().getLayoutInflater().inflate(R.layout.unknow_vessage_container,null);
    }

    @Override
    public void onPresentingVessageSeted(Vessage oldVessage, Vessage newVessage) {
        super.onPresentingVessageSeted(oldVessage, newVessage);
        vessageContainer.removeAllViews();
        vessageContainer.addView(vessageView);
        setContainerLayoutParams();
    }

    private void setContainerLayoutParams() {
        Point size = new Point();
        playVessageManager.getConversationViewActivity().getWindowManager().getDefaultDisplay().getSize(size);
        ViewGroup.LayoutParams params = vessageView.getLayoutParams();
        params.height = size.y / 2;
        params.width = params.height * 3 / 4;
        vessageView.setLayoutParams(params);
    }
}
