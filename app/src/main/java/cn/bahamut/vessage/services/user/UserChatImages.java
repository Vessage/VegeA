package cn.bahamut.vessage.services.user;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/8/2.
 */
public class UserChatImages extends RealmObject {
    @PrimaryKey
    public String userId;
    public RealmList<ChatImage> chatImages;
}
