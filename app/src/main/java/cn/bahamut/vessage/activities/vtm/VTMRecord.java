package cn.bahamut.vessage.activities.vtm;

import io.realm.RealmObject;

/**
 * Created by alexchow on 2016/11/23.
 */

public class VTMRecord extends RealmObject {
    public String chatterId;
    public String modelValue;
    public long mtime;
    public long ctime;
}
