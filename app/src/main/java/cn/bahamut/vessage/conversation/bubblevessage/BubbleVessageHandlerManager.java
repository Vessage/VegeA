package cn.bahamut.vessage.conversation.bubblevessage;

import java.util.HashMap;

/**
 * Created by alexchow on 2016/11/4.
 */

public class BubbleVessageHandlerManager {
    private static HashMap<Integer,BubbleVessageHandler> handlerMap = new HashMap<>();

    public static void registHandler(int vessageType,BubbleVessageHandler handler){
        handlerMap.put(vessageType,handler);
    }

    public static BubbleVessageHandler getNoVessageHandler() {
        return NoBubbleVessageHandler.instance;
    }

    public static BubbleVessageHandler getBubbleVessageHandler(int vessageTypeId) {
        BubbleVessageHandler handler = handlerMap.get(vessageTypeId);
        if (handler == null){
            return UnknowBubbleVessageHandler.instance;
        }
        return handler;
    }

}
