package cn.bahamut.vessage.services.user;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexchow on 16/5/23.
 */
public class UserLocalInfo extends RealmObject {
    @PrimaryKey
    public String userId;
    public String noteName;
}
