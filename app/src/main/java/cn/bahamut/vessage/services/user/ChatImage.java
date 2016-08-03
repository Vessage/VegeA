package cn.bahamut.vessage.services.user;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/8/2.
 */
public class ChatImage extends RealmObject {

    @PrimaryKey
    public String imageId;
    public String imageType;
}
