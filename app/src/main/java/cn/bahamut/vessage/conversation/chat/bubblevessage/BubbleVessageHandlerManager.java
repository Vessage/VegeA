package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;

import java.util.HashMap;

import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/4.
 */

public class BubbleVessageHandlerManager {
    private static HashMap<Integer, BubbleVessageHandler> handlerMap = new HashMap<>();

    public static void registHandler(int vessageType, BubbleVessageHandler handler) {
        handlerMap.put(vessageType, handler);
    }

    public static BubbleVessageHandler getNoVessageHandler() {
        return NoBubbleVessageHandler.instance;
    }

    public static BubbleVessageHandler getBubbleVessageHandler(Activity context, Vessage vessage) {
        BubbleVessageHandler handler = handlerMap.get(vessage.typeId);
        if (handler == null) {
            return UnknowBubbleVessageHandler.instance;
        }
        return handler.instanceOfVessage(context, vessage);
    }
}