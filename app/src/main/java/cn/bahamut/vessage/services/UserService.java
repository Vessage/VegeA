package cn.bahamut.vessage.services;

import android.content.Context;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.json.JSONObject;

import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.models.VessageUser;
import cn.bahamut.vessage.restfulapi.user.ChangeAvatarRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeMainChatImageRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeNickRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByAccountIdRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByMobileRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoRequest;
import cn.bahamut.vessage.restfulapi.user.RegistUserDeviceRequest;
import cn.bahamut.vessage.restfulapi.user.SendMobileVSMSRequest;
import cn.bahamut.vessage.restfulapi.user.ValidateMobileVSMSRequest;
import io.realm.Realm;

/**
 * Created by alexchow on 16/3/30.
 */
public class UserService extends Observable implements OnServiceUserLogin,OnServiceUserLogout,OnServiceInit{

    public static final String NOTIFY_USER_PROFILE_UPDATED = "NOTIFY_USER_PROFILE_UPDATED";

    private Context applicationContext;
    @Override
    public void onServiceInit(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public interface UserUpdatedCallback{
        void updated(VessageUser user);
    }

    public interface ChangeNickCallback{
        void onChangeNick(boolean isChanged);
    }

    public interface MobileValidateCallback{
        void onValidateMobile(boolean validated);
    }

    public interface ChangeChatBackgroundImageCallback{
        void onChangeChatBackgroundImage(boolean isChanged);
    }

    public interface ChangeAvatarCallback{
        void onChangeAvatar(boolean isChanged);
    }

    public static final UserUpdatedCallback DefaultUserUpdatedCallback = new UserUpdatedCallback() {
        @Override
        public void updated(VessageUser user) {}
    };

    private VessageUser me;

    @Override
    public void onUserLogin(String userId) {
        initMe(userId);
        MobclickAgent.onProfileSignIn(UserSetting.getLastUserLoginedAccount());
    }

    @Override
    public void onUserLogout() {
        me = null;
        MobclickAgent.onProfileSignOff();
        ServicesProvider.setServiceNotReady(UserService.class);
        disableUPush();
    }

    public void initMe(String userId){
        VessageUser user = getUserById(userId);
        enableUPush();
        if (user == null){
            fetchUserByUserId(userId, new UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if(user != null){
                        me = user;
                        ServicesProvider.setServiceReady(UserService.class);
                    }else {
                        ServicesProvider.postInitServiceFailed(UserService.class,"Null User");
                    }
                }
            });
        }else{
            me = user;
            ServicesProvider.setServiceReady(UserService.class);
        }
    }

    private void enableUPush(){
        PushAgent mPushAgent = PushAgent.getInstance(applicationContext);
        String token = UserSetting.getDeviceToken();
        String rtoken = mPushAgent.getRegistrationId();
        final String savedDeviceToken = token == null ? rtoken : token;
        Log.i("Saved Device Token",savedDeviceToken);
        if(!StringHelper.isStringNullOrEmpty(savedDeviceToken)){
            registUserDeviceToken(savedDeviceToken);
        }
        mPushAgent.enable(new IUmengRegisterCallback() {
            @Override
            public void onRegistered(String s) {
                if(StringHelper.isStringNullOrEmpty(s)){
                    Log.w("Get Device Token","get device token error");
                }else if(!s.equals(savedDeviceToken)){
                    Log.i("Device Token",s);
                    registUserDeviceToken(s);
                    UserSetting.setDeviceToken(s);
                }
            }
        });
    }

    private void disableUPush(){
        UserSetting.setDeviceToken(null);
        PushAgent mPushAgent = PushAgent.getInstance(applicationContext);
        mPushAgent.disable();
    }

    public VessageUser getMyProfile(){
        return me;
    }

    public boolean isMyMobileValidated(){
        return me != null && me.mobile != null;
    }

    public boolean isMyProfileHaveChatBackground(){
        return me != null && me.mainChatImage != null;
    }

    public VessageUser getUserById(String userId){
        return Realm.getDefaultInstance().where(VessageUser.class).equalTo("userId",userId).findFirst();
    }

    public VessageUser getUserByMobile(String mobile){
        return Realm.getDefaultInstance().where(VessageUser.class).equalTo("mobile",mobile).findFirst();
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

    public VessageUser getCachedUserByAccountId(String accountId){
        return Realm.getDefaultInstance().where(VessageUser.class).equalTo("accountId",accountId).findFirst();
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
                VessageUser user = null;
                if(isOk){
                    Realm.getDefaultInstance().beginTransaction();
                    user = Realm.getDefaultInstance().createOrUpdateObjectFromJson(VessageUser.class, result);
                    Realm.getDefaultInstance().commitTransaction();
                    postUserProfileUpdatedNotify(user);
                }
                if(handler != null){
                    handler.updated(user);
                }

            }
        });
    }

    private void postUserProfileUpdatedNotify(VessageUser user){
        postNotification(NOTIFY_USER_PROFILE_UPDATED,user);
    }

    public void changeMyNickName(final String newNick,final ChangeNickCallback handler){
        ChangeNickRequest req = new ChangeNickRequest();
        req.setNick(newNick);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    Realm.getDefaultInstance().beginTransaction();
                    me.nickName = newNick;
                    Realm.getDefaultInstance().commitTransaction();
                    postUserProfileUpdatedNotify(me);
                }
                if(handler != null){
                    handler.onChangeNick(isOk);
                }
            }
        });
    }

    public void changeMyChatImage(final String chatImage, final ChangeChatBackgroundImageCallback onChangeCallback){
        ChangeMainChatImageRequest req = new ChangeMainChatImageRequest();
        req.setChatImage(chatImage);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    Realm.getDefaultInstance().beginTransaction();
                    me.mainChatImage = chatImage;
                    Realm.getDefaultInstance().commitTransaction();
                    postUserProfileUpdatedNotify(me);
                }
                if(onChangeCallback != null){
                    onChangeCallback.onChangeChatBackgroundImage(isOk);
                }
            }
        });
    }

    public void changeMyAvatar(final String avatar, final ChangeAvatarCallback onChangeCallback){
        ChangeAvatarRequest req = new ChangeAvatarRequest();
        req.setAvatar(avatar);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    Realm.getDefaultInstance().beginTransaction();
                    me.avatar = avatar;
                    Realm.getDefaultInstance().commitTransaction();
                    postUserProfileUpdatedNotify(me);
                }
                if(onChangeCallback != null){
                    onChangeCallback.onChangeAvatar(isOk);
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

    public void validateMobile(final String mobile, String zone, String code, final MobileValidateCallback callback){
        ValidateMobileVSMSRequest req = new ValidateMobileVSMSRequest();
        req.setMobile(mobile);
        req.setCode(code);
        req.setZone(zone);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    Realm.getDefaultInstance().beginTransaction();
                    me.mobile = mobile;
                    Realm.getDefaultInstance().commitTransaction();
                }
                if(callback != null){
                    callback.onValidateMobile(isOk);
                }
            }
        });
    }

    public void registUserDeviceToken(String deviceToken){
        RegistUserDeviceRequest request = new RegistUserDeviceRequest();
        request.setDeviceToken(deviceToken);
        request.setDeviceType(RegistUserDeviceRequest.DEVICE_TYPE_ANDROID);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    Log.i("UserService","regist user device success");
                }else {
                    Log.w("UserService","regist user device failure");
                }
            }
        });
    }
}
