package cn.bahamut.vessage.services;

import org.json.JSONObject;

import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.restfulapi.user.ChangeAvatarRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeMainChatImageRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeNickRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByAccountIdRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByMobileRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoRequest;
import cn.bahamut.vessage.restfulapi.user.SendMobileVSMSRequest;
import cn.bahamut.vessage.restfulapi.user.ValidateMobileVSMSRequest;
import io.realm.Realm;

/**
 * Created by alexchow on 16/3/30.
 */
public class UserService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    public static final String NOTIFY_USER_PROFILE_UPDATED = "NOTIFY_USER_PROFILE_UPDATED";
    interface UserUpdatedCallback{
        void updated(VessageUser user);
    }

    private VessageUser me;

    @Override
    public void onUserLogin(String userId) {
        initMe(userId);
    }

    @Override
    public void onUserLogout() {
        me = null;
        ServicesProvider.setServiceNotReady(UserService.class);
    }

    public void initMe(String userId){
        VessageUser user = getUserById(userId);
        if (user == null){
            fetchUserByUserId(userId, new UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    me = user;
                    ServicesProvider.setServiceReady(UserService.class);
                }
            });
        }else{
            me = user;
            ServicesProvider.setServiceReady(UserService.class);
        }
    }

    public boolean isMyMobileValidated(){
        if(me != null){
            return me.mobile != null;
        }
        return false;
    }

    public boolean isMyProfileHaveChatBackground(){
        if(me != null){
            return me.mainChatImage != null;
        }
        return false;
    }

    public VessageUser getUserById(String userId){
        VessageUser user = Realm.getDefaultInstance().where(VessageUser.class).equalTo("userId",userId).findFirst();
        return user;
    };

    public VessageUser getUserByMobile(String mobile){
        VessageUser user = Realm.getDefaultInstance().where(VessageUser.class).equalTo("mobile",mobile).findFirst();
        return user;
    }

    public void fetchUserByUserId(String userId,UserUpdatedCallback handler){
        GetUserInfoRequest request = new GetUserInfoRequest();
        request.setUserId(userId);
        fetchUserByRequest(request,handler);
    }

    public void fetchUserByMobile(String mobile ,UserUpdatedCallback handler){
        GetUserInfoByMobileRequest request = new GetUserInfoByMobileRequest();
        request.setMobile(mobile);
        fetchUserByRequest(request, handler);
    }

    public void fetchUserByAccountId(String accountId, UserUpdatedCallback handler){
        GetUserInfoByAccountIdRequest request = new GetUserInfoByAccountIdRequest();
        request.setAccountId(accountId);
        fetchUserByRequest(request, handler);
    }

    public void fetchUserByRequest(BahamutRequestBase request, final UserUpdatedCallback handler){
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                VessageUser user = Realm.getDefaultInstance().createOrUpdateObjectFromJson(VessageUser.class, result);
                handler.updated(user);
                ObserverState state = new ObserverState();
                state.setNotifyType(NOTIFY_USER_PROFILE_UPDATED);
                postNotification(state);
            }
        });
    }

    private void postMyProfileUpdatedNotify(){
        ObserverState state = new ObserverState();
        state.setNotifyType(NOTIFY_USER_PROFILE_UPDATED);
        postNotification(state);
    }

    public void changeMyNickName(final String newNick,final UserUpdatedCallback handler){
        ChangeNickRequest req = new ChangeNickRequest();
        req.setNick(newNick);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    me.nickName = newNick;
                    postMyProfileUpdatedNotify();
                }
            }
        });
    }

    public void changeMyChatImage(final String chatImage){
        ChangeMainChatImageRequest req = new ChangeMainChatImageRequest();
        req.setChatImage(chatImage);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    me.mainChatImage = chatImage;
                    postMyProfileUpdatedNotify();
                }
            }
        });
    }

    public void changeMyAvatar(final String avatar){
        ChangeAvatarRequest req = new ChangeAvatarRequest();
        req.setAvatar(avatar);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    me.avatar = avatar;
                    postMyProfileUpdatedNotify();
                }
            }
        });
    }

    public void sendValidateCodeToMobile(String mobile){
        SendMobileVSMSRequest req = new SendMobileVSMSRequest();
        req.setMobile(mobile);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    //TODO:
                }
            }
        });
    }

    public void validateMobile(String mobile,String zone, String code){
        ValidateMobileVSMSRequest req = new ValidateMobileVSMSRequest();
        req.setMobile(mobile);
        req.setCode(code);
        req.setZone(zone);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    //TODO:
                }
            }
        });
    }
}
