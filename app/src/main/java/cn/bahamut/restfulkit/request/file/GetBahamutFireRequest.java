package cn.bahamut.restfulkit.request.file;

import cn.bahamut.restfulkit.request.BahamutRequestBase;

/**
 * Created by alexchow on 16/4/11.
 */
public class GetBahamutFireRequest extends BahamutRequestBase {

    public void setFileId(String fileId){
        setApi("/BahamutFires/" + fileId);
    }

    @Override
    public boolean canPostRequest(int inQueueRequestCount) {
        return true;
    }
}
