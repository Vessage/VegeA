package cn.bahamut.vessage.activities.littlepaper.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/6/14.
 */
public class LittlePaperReadResponse extends RealmObject {
    public static final int TYPE_ASK_SENDER = 1;
    public static final int TYPE_RETURN_ASKER = 2;
    public static final int CODE_ACCEPT_READ = 1;
    public static final int CODE_REJECT_READ = 2;
    @PrimaryKey
    public String paperId;
    public String asker;
    public String askerNick;
    public String paperReceiver;

    public int type = 0;
    public int code = 0;

    //Local Properties
    public boolean isRead = false;
}
