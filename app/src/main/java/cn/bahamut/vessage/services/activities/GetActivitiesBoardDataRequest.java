package cn.bahamut.vessage.services.activities;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/5/16.
 */
public class GetActivitiesBoardDataRequest extends BahamutRequestBase{
    @Override
    protected String getApi() {
        return "/Activities/BoardData";
    }
}
