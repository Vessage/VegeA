package cn.bahamut.vessage.activities.sns.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bahamut.common.DateHelper;
import cn.bahamut.common.JsonHelper;

/**
 * Created by alexchow on 2016/11/13.
 */

public class SNSPostLike {
    public long ts = 0; //time span create
    public String usrId; //post like user id
    public String nick; //post like user nick
    public String img; //SNS post image
    public String txt; //SNS post text content

    public Date getPostDate() {
        if (ts <= 0) {
            return null;
        }
        return DateHelper.getDateFromUnixTimeSpace(ts);
    }

    public static SNSPostLike[] praseArray(JSONArray jsonArray) {
        try {
            List<SNSPostLike> likes = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                likes.add(SNSPostLike.prase(jsonObject));
            }
            return likes.toArray(new SNSPostLike[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new SNSPostLike[0];
    }

    private static SNSPostLike prase(JSONObject jsonObject) throws JSONException {
        return JsonHelper.parseObject(jsonObject, SNSPostLike.class);
    }
}