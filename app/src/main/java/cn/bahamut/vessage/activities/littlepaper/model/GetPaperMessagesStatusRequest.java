package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.common.StringHelper;
import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/5/19.
 */
public class GetPaperMessagesStatusRequest extends BahamutRequestBase{
    @Override
    protected String getApi() {
        return "/LittlePaperMessages";
    }

    public void setPaperId(String[] paperIds){
        String paperIdsString = StringHelper.stringsJoinSeparator(paperIds,";");
        putParameter("paperIds",paperIdsString);
    }
}
