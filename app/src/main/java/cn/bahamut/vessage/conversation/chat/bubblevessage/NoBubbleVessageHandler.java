package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/4.
 */

public class NoBubbleVessageHandler extends TextBubbleVessageHandler {
    public static final BubbleVessageHandler instance = new NoBubbleVessageHandler();

    @Override
    protected String getTextContent(Vessage vessage) {
        return LocalizedStringHelper.getLocalizedString(R.string.no_more_vessages);
    }

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        return instance;
    }
}
