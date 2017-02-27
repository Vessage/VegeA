package cn.bahamut.common;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;

/**
 * Created by alexchow on 16/4/8.
 */
public class JsonHelper {

    public static <T> T parseObject(JSONObject jsonObject, Class<T> cls) throws JSONException {
        if (jsonObject != null) {
            return new Gson().fromJson(jsonObject.toString(), cls);
        }
        throw new JSONException("Null Json Object");
    }

    public static <T> T parseObject(String json, Class<T> cls) throws JSONException {
        if (json != null) {
            return new Gson().fromJson(json.toString(), cls);
        }
        throw new JSONException("Null Json Object");
    }

    public static <T> T[] parseArray(JSONArray ja, Class<T> clazz) {
        if (clazz == null || isNull(ja)) {
            return null;
        }

        int len = ja.length();

        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, len);

        for (int i = 0; i < len; ++i) {
            try {
                JSONObject jo = ja.getJSONObject(i);
                T o = parseObject(jo, clazz);
                array[i] = o;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array;
    }

    private static boolean isNull(Object obj) {
        if (obj instanceof JSONObject) {
            return JSONObject.NULL.equals(obj);
        }
        return obj == null;
    }

    public static String toJSON(Object o) {
        return new Gson().toJson(o);
    }
}