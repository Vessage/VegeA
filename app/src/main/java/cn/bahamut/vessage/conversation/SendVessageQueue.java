package cn.bahamut.vessage.conversation;

import java.io.File;

/**
 * Created by alexchow on 16/4/12.
 */
public class SendVessageQueue {
    static private SendVessageQueue instance;
    public static SendVessageQueue getInstance() {
        if(instance == null){
            instance = new SendVessageQueue();
        }
        return instance;
    }

    public boolean sendVessageToUser(String userId, File videoFile) {
        return false;
    }

    public boolean sendVessageToMobile(String mobile, File videoFile) {
        return false;
    }
}
