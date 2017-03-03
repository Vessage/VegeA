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

public class SNSPostComment {
    public String cmt; //Comment Content
    public String psterNk; //Poster nick
    public String pster; //Poster User Id
    public String atNick; //@UserNick
    public String postId; //SNS Post Id
    public String img; //SNS post image
    public long ts = 0; //Time Span Create
    public String txt; //SNS post text content

    public Date getPostDate() {
        if (ts <= 0) {
            return null;
        }
        return DateHelper.getDateFromUnixTimeSpace(ts);
    }

    public static SNSPostComment[] praseArray(JSONArray jsonArray) {
        try {
            List<SNSPostComment> comments = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                comments.add(SNSPostComment.prase(jsonObject));
            }
            return comments.toArray(new SNSPostComment[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new SNSPostComment[0];
    }

    private static SNSPostComment prase(JSONObject jsonObject) throws JSONException {
        SNSPostComment comment = new SNSPostComment();
        comment.cmt = jsonObject.has("cmt") ? jsonObject.getString("cmt") : null;
        comment.psterNk = jsonObject.has("psterNk") ? jsonObject.getString("psterNk") : null;
        comment.pster = jsonObject.has("pster") ? jsonObject.getString("pster") : null;
        comment.atNick = jsonObject.has("atNick") ? jsonObject.getString("atNick") : null;
        comment.postId = jsonObject.has("postId") ? jsonObject.getString("postId") : null;
        comment.img = jsonObject.has("img") ? jsonObject.getString("img") : null;
        comment.ts = jsonObject.has("ts") ? jsonObject.getLong("ts") : 0;
        return comment;
    }
}
