package cn.bahamut.vessage.services.activities;

import io.realm.RealmObject;

/**
 * Created by alexchow on 16/5/22.
 */
public class ExtraActivityBadge extends RealmObject{
    public String activityId;
    public int badgeValue;
    public boolean miniBadge;
}
