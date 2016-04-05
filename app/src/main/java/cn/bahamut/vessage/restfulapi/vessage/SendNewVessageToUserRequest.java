package cn.bahamut.vessage.restfulapi.vessage;

/**
 * Created by alexchow on 16/4/5.
 */
public class SendNewVessageToUserRequest extends SendNewVessageRequestBase {
    @Override
    public String getApi() {
        return "/Vessages/ForUser";
    }

    void setReceiverId(String receiverId){
        putParameter("receiverId", receiverId);
    }
}
