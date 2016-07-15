package cn.bahamut.common;

import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/7.
 */
public interface BahamutObject {
    void setFieldValuesByJson(JSONObject jsonObject);
}
