package cn.bahamut.vessage.conversation.bubblevessage;

import android.app.Activity;
import android.content.Context;
import android.util.SizeF;
import android.view.View;
import android.view.ViewGroup;

import cn.bahamut.common.BTSize;
import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/4.
 */

public interface BubbleVessageHandler {

    BTSize getContentViewSize(Activity context, Vessage vessage, BTSize maxLimitedSize, View contentView);
    ViewGroup getContentView(Activity context, Vessage vessage);
    void presentContent(Activity context,Vessage oldVessage,Vessage newVessage,View contentView);

    void onUnloadVessage(Activity context);
    void onPrepareVessage(Activity context,Vessage vessage);

    BubbleVessageHandler instanceOfVessage(Activity context,Vessage vessage);
}
