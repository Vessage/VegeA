package cn.bahamut.vessage.conversation.bubblevessage;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.bahamut.common.BTSize;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/4.
 */

public class UnknowBubbleVessageHandler extends TextBubbleVessageHandler {
    public static final BubbleVessageHandler instance = new UnknowBubbleVessageHandler();

    @Override
    protected String getTextContent(Vessage vessage) {
        return LocalizedStringHelper.getLocalizedString(R.string.unknow_vsg_type);
    }

    @Override
    public BubbleVessageHandler instanceOfVessage(Activity context, Vessage vessage) {
        return instance;
    }
}
