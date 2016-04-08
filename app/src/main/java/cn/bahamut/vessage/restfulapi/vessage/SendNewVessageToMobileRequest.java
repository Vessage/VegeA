package cn.bahamut.vessage.restfulapi.vessage;

/**
 * Created by alexchow on 16/4/5.
 */
public class SendNewVessageToMobileRequest extends SendNewVessageRequestBase {
    @Override
    public String getApi() {
        return "/Vessages/ForMobile";
    }

    public void setReceiverMobile(String receiverMobile) {
        putParameter("receiverMobile", receiverMobile);
    }
}
