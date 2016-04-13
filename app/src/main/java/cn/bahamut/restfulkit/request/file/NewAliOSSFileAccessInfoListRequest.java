package cn.bahamut.restfulkit.request.file;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 16/4/11.
 */
public class NewAliOSSFileAccessInfoListRequest extends BahamutRequestBase {
    @Override
    protected String getApi() {
        return "/AliOSSFiles";
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.POST;
    }

    public void setFileType(String fileType){
        putParameter("fileType", fileType);
    }

    public void setFileSize(int fileSize){
        putParameter("fileSize", String.valueOf(fileSize));
    }
}
