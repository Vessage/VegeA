package cn.bahamut.vessage.restfulapi.user;

import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.RequestMethod;

/**
 * Created by alexchow on 2016/9/26.
 */

public class UpdateChatImageRequest extends BahamutRequestBase {
    @Override
    public RequestMethod getMethod() {
        return RequestMethod.PUT;
    }

    @Override
    protected String getApi() {
        return "/VessageUsers/ChatImages";
    }

    public void setImageType(String imageType) {
        putParameter("imageType", imageType);
    }

    public void setImage(String image){
        putParameter("image",image);
    }

}
