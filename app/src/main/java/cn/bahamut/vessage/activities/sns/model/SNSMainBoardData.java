package cn.bahamut.vessage.activities.sns.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSMainBoardData {
    public int nlks = 0; //New Likes
    public int ncmt = 0; //New Comments
    public int tlks = 0; //Total likes
    public String annc = null; //Announcement
    public boolean newer = false; //first use sns
    public SNSPost[] posts;

    public static SNSMainBoardData prase(JSONObject jsonObject) {
        SNSMainBoardData result = new SNSMainBoardData();
        try {
            result.nlks = jsonObject.getInt("nlks");
            result.ncmt = jsonObject.getInt("ncmt");
            result.tlks = jsonObject.getInt("tlks");

            result.annc = jsonObject.has("annc") ? jsonObject.getString("annc") : null;
            result.newer = jsonObject.getBoolean("newer");
            result.posts = SNSPost.praseArray(jsonObject.getJSONArray("posts"));
            return result;
        } catch (JSONException e) {
            return null;
        }
    }
}
