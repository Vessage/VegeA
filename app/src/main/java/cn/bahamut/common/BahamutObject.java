package cn.bahamut.common;

import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by alexchow on 16/4/7.
 */
public interface BahamutObject {
    void setFieldValuesByJson(JSONObject jsonObject);
}
