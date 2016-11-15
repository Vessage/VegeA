package cn.bahamut.vessage.activities.sns.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bahamut.common.DateHelper;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSPost {
    public static final int TYPE_NORMAL_POST = 0;
    public static final int TYPE_MY_POST = 1;

    public String pid; //Post Id
    public String usrId; //Poster User Id
    public String img; //Post Image
    public long ts = 0; //Post Timespan
    public int lc = 0; //Like Count
    public int t = TYPE_NORMAL_POST; //Type
    public String pster; //Poster
    public int cmtCnt = 0; //Comment Count
    public long upTs = 0; //Update Timespan

    public Date getPostDate() {
        if (ts <= 0) {
            return null;
        }
        return DateHelper.getDateFromUnixTimeSpace(ts);
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

    private static SNSPost prase(JSONObject jsonObject) throws JSONException {
        SNSPost post = new SNSPost();
        post.pid = jsonObject.getString("pid");
        post.usrId = jsonObject.getString("usrId");
        post.img = jsonObject.getString("img");
        post.ts = jsonObject.getLong("ts");
        post.lc = jsonObject.getInt("lc");
        post.t = jsonObject.getInt("t");
        post.pster = jsonObject.getString("pster");
        post.cmtCnt = jsonObject.getInt("cmtCnt");
        post.upTs = jsonObject.getLong("upTs");
        return post;
    }
}
