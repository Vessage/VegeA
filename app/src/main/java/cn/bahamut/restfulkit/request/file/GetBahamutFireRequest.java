package cn.bahamut.restfulkit.request.file;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/11.
 */
public class GetBahamutFireRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/BahamutFires";
    }

    public void setFileId(String fileId){
        setApi("/BahamutFires/" + fileId);
    }
}
