package cn.bahamut.vessage.activities.littlepaper.model;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/5/19.
 */
public class GetReceivedPaperMessagesRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/LittlePaperMessages/Received";
    }
}
