package cn.bahamut.vessage.activities.sns.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.NotProguard;

/**
 * Created by alexchow on 2016/11/13.
 */
@NotProguard
public class SNSPost {
    public static final int TYPE_NORMAL_POST = 0;
    public static final int TYPE_MY_POST = 1;
    public static final int TYPE_SINGLE_USER_POST = 2;

    public static final int STATE_REMOVED = -1;
    public static final int STATE_DELETED = -2;

    public static final int STATE_PRIVATE = 0;
    public static final int STATE_NORMAL = 1;

    public String pid; //Post Id
    public String usrId; //Poster User Id
    public String img; //Post Image
    public long ts = 0; //Post Timespan
    public int lc = 0; //Like Count
    public int t = TYPE_NORMAL_POST; //Type
    public int st = STATE_NORMAL; //State
    public String pster; //Poster Nick
    public int cmtCnt = 0; //Comment Count
    public long upTs = 0; //Update Timespan
    public String body;

    public Date getPostDate() {
        if (ts <= 0) {
            return null;
        }
        return DateHelper.getDateFromUnixTimeSpace(ts);
    }

    public JSONObject getBodyObject() throws JSONException {
        return new JSONObject(body);
    }

    public static SNSPost[] praseArray(JSONArray jsonArray) {
        try {
            List<SNSPost> posts = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                posts.add(SNSPost.prase(jsonObject));
            }
            return posts.toArray(new SNSPost[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new SNSPost[0];
    }

    public static SNSPost prase(JSONObject jsonObject) throws JSONException {
        SNSPost post = new SNSPost();
        post.pid = jsonObject.has("pid") ? jsonObject.getString("pid") : null;
        post.usrId = jsonObject.has("usrId") ? jsonObject.getString("usrId") : null;
        post.img = jsonObject.has("img") ? jsonObject.getString("img") : null;
        post.pster = jsonObject.has("pster") ? jsonObject.getString("pster") : null;
        post.ts = jsonObject.has("ts") ? jsonObject.getLong("ts") : 0;
        post.lc = jsonObject.has("lc") ? jsonObject.getInt("lc") : 0;
        post.t = jsonObject.has("t") ? jsonObject.getInt("t") : 0;
        post.cmtCnt = jsonObject.has("cmtCnt") ? jsonObject.getInt("cmtCnt") : 0;
        post.upTs = jsonObject.has("upTs") ? jsonObject.getLong("upTs") : 0;
        post.body = jsonObject.has("body") ? jsonObject.getString("body") : null;
        post.st = jsonObject.has("st") ? jsonObject.getInt("st") : STATE_NORMAL;
        return post;
    }
}
