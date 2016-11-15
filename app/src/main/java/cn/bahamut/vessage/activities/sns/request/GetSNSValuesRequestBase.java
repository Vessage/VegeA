package cn.bahamut.vessage.activities.sns.request;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 2016/11/13.
 */

public class GetSNSValuesRequestBase extends BahamutRequestBase {

    public void setTimeSpan(long timeSpan){
        putParameter("ts",String.valueOf(timeSpan));
    }

    public void setPageCount(int pageCount){
        putParameter("cnt",String.valueOf(pageCount));
    }
}
