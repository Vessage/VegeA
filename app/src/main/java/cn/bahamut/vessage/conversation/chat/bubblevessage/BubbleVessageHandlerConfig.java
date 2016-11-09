package cn.bahamut.vessage.conversation.chat.bubblevessage;

import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2016/11/4.
 */

public class BubbleVessageHandlerConfig {
    public static void loadHandlers(){
        BubbleVessageHandlerManager.registHandler(Vessage.TYPE_NO_VESSAGE,NoBubbleVessageHandler.instance);
        BubbleVessageHandlerManager.registHandler(Vessage.TYPE_CHAT_VIDEO,new VideoBubbleVessageHandler());
        BubbleVessageHandlerManager.registHandler(Vessage.TYPE_FACE_TEXT,new TextBubbleVessageHandler());
        BubbleVessageHandlerManager.registHandler(Vessage.TYPE_IMAGE,new ImageBubbleVessageHandler());
    }
}
