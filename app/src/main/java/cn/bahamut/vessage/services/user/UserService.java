package cn.bahamut.vessage.services.user;

import android.content.Context;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import org.apache.commons.codec1.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.bahamut.common.JsonHelper;
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
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.restfulapi.user.ChangeAvatarRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeMainChatImageRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeNickRequest;
import cn.bahamut.vessage.restfulapi.user.GetActiveUsersRequest;
import cn.bahamut.vessage.restfulapi.user.GetNearUsersRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserChatImageRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByAccountIdRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByMobileRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoRequest;
import cn.bahamut.vessage.restfulapi.user.RegistMobileUserRequest;
import cn.bahamut.vessage.restfulapi.user.RegistUserDeviceRequest;
import cn.bahamut.vessage.restfulapi.user.RemoveUserDeviceRequest;
import cn.bahamut.vessage.restfulapi.user.SendMobileVSMSRequest;
import cn.bahamut.vessage.restfulapi.user.UpdateChatImageRequest;
import cn.bahamut.vessage.restfulapi.user.ValidateMobileVSMSRequest;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexchow on 16/3/30.
 */
public class UserService extends Observable implements OnServiceUserLogin,OnServiceUserLogout,OnServiceInit{

    public static final String NOTIFY_MY_PROFILE_UPDATED = "NOTIFY_MY_PROFILE_UPDATED";
    public static final String NOTIFY_USER_PROFILE_UPDATED = "NOTIFY_USER_PROFILE_UPDATED";
    public static final String NOTIFY_MY_CHAT_IMAGES_UPDATED = "NOTIFY_MY_CHAT_IMAGES_UPDATED";
    private static final String FETCH_ACTIVE_USER_TIME_KEY = "FETCH_ACTIVE_USER_TIME";
    private static final String REGIST_DEVICE_TOKEN_TIME_KEY = "REGIST_DEVICE_TOKEN_TIME";
    private static final String FETCH_NEAR_USER_TIME_KEY = "FETCH_NEAR_USER_TIME";

    private Context applicationContext;
    private boolean forceFetchUserProfileOnece = false;
    private Realm realm;
    private List<VessageUser> nearUsers;
    private UserChatImages myChatImages;
    private HashMap<String,UserLocalInfo> userLocalInfos = new HashMap<>();

    @Override
    public void onServiceInit(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void fetchUserByMobile(String mobile) {
        fetchUserByMobile(mobile,DefaultUserUpdatedCallback);
    }

    public List<VessageUser> getActiveUsers() {
        if(activeUsers == null){
            activeUsers = new ArrayList<>();
        }
        return activeUsers;
    }

    public Realm getRealm() {
        return realm;
    }

    public List<VessageUser> getNearUsers() {
        if(nearUsers == null){
            nearUsers = new ArrayList<>();
        }
        return nearUsers;
    }

    public interface UserUpdatedCallback{
        void updated(VessageUser user);
    }

    public interface ChangeNickCallback{
        void onChangeNick(boolean isChanged);
    }

    public interface MobileValidateCallback{
        void onValidateMobile(boolean validated,boolean isBindedNewAccount,String newAccountUserId);
    }

    public interface ChangeChatImageCallback {
        void onChatImageChanged(boolean isChanged);
    }

    public interface ChangeAvatarCallback{
        void onChangeAvatar(boolean isChanged);
    }

    public static final UserUpdatedCallback DefaultUserUpdatedCallback = new UserUpdatedCallback() {
        @Override
        public void updated(VessageUser user) {}
    };

    private volatile VessageUser me;

    private List<VessageUser> activeUsers;

    @Override
    public void onUserLogin(String userId) {
        realm = Realm.getDefaultInstance();
        initMe(userId);
        initUserLocalInfo();
        MobclickAgent.onProfileSignIn(UserSetting.getLastUserLoginedAccount());
    }

    private void initUserLocalInfo() {
        RealmResults<UserLocalInfo> results = realm.where(UserLocalInfo.class).findAll();
        for (UserLocalInfo userLocalInfo : results) {
            userLocalInfos.put(userLocalInfo.userId,userLocalInfo.copyObject());
        }
    }

    @Override
    public void onUserLogout() {
        myChatImages = null;
        me = null;
        realm.close();
        realm = null;
        userLocalInfos.clear();
        getActiveUsers().clear();
        getNearUsers().clear();
        MobclickAgent.onProfileSignOff();
        ServicesProvider.setServiceNotReady(UserService.class);
    }

    private void initMe(String userId){
        Log.d("LoginUser",userId);
        VessageUser user = getUserById(userId);

        if (user == null){
            setForceFetchUserProfileOnece();
            fetchUserByUserId(userId, new UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if(user != null){
                        me = user;
                        initMyChatImages();
                        registUserDeviceToken();
                        fetchActiveUsersFromServer(false);
                        ServicesProvider.setServiceReady(UserService.class);
                    }else {
                        ServicesProvider.postInitServiceFailed(UserService.class, LocalizedStringHelper.getLocalizedString(R.string.init_user_data_error));
                    }
                }
            });
        }else{
            me = user;
            registUserDeviceToken();
            initMyChatImages();
            setForceFetchUserProfileOnece();
            fetchUserByUserId(userId, new UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if(user != null){
                        me = user;
                    }
                }
            });
            fetchActiveUsersFromServer(false);
            ServicesProvider.setServiceReady(UserService.class);
        }
    }

    private void initMyChatImages(){
        myChatImages = getRealm().where(UserChatImages.class).equalTo("userId",me.userId).findFirst();
        if (myChatImages == null){
            fetchUserChatImages(me.userId);
        }
    }

    private volatile boolean fetchingActiveUsers = false;
    public void fetchActiveUsersFromServer(boolean checkTime){
        if(fetchingActiveUsers || (checkTime && checkTimeIsInCDForKey(FETCH_ACTIVE_USER_TIME_KEY,3))){
            return;
        }
        fetchingActiveUsers = true;
        GetActiveUsersRequest req = new GetActiveUsersRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                fetchingActiveUsers = false;
                if (isOk) {
                    saveCheckTimeForKey(FETCH_ACTIVE_USER_TIME_KEY);
                    VessageUser[] activeUsers = JsonHelper.parseArray(result,VessageUser.class);
                    if(UserService.this.activeUsers == null){
                        UserService.this.activeUsers = new ArrayList(activeUsers.length);
                    }
                    UserService.this.activeUsers.clear();
                    for (VessageUser activeUser : activeUsers) {
                        UserService.this.activeUsers.add(activeUser);
                        postUserProfileUpdatedNotify(activeUser);
                    }
                }else if(UserService.this.activeUsers != null){
                    UserService.this.activeUsers.clear();
                }
            }
        });
    }

    private volatile boolean fetchingNearUsers = false;
    public void fetchNearUsers(String location,boolean checkTime){
        if(fetchingNearUsers || (checkTime && checkTimeIsInCDForKey(FETCH_NEAR_USER_TIME_KEY,3))){
            return;
        }
        fetchingNearUsers = true;
        GetNearUsersRequest req = new GetNearUsersRequest();
        req.setLocation(location);
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                fetchingNearUsers = false;
                if (isOk) {
                    saveCheckTimeForKey(FETCH_NEAR_USER_TIME_KEY);
                    VessageUser[] nearUsers = JsonHelper.parseArray(result,VessageUser.class);
                    if(UserService.this.nearUsers == null){
                        UserService.this.nearUsers = new ArrayList(nearUsers.length);
                    }
                    UserService.this.nearUsers.clear();
                    for (VessageUser user : nearUsers) {
                        UserService.this.nearUsers.add(user);
                        postUserProfileUpdatedNotify(user);
                    }
                }else if(UserService.this.nearUsers != null){
                    UserService.this.nearUsers.clear();
                }
            }
        });
    }

    private void saveCheckTimeForKey(String key) {
        long nowTime = new Date().getTime() / 3600000;
        UserSetting.getUserSettingPreferences().edit().putLong(UserSetting.generateUserSettingKey(key),nowTime).commit();
    }

    private boolean checkTimeIsInCDForKey(String checkTimeKey, int hour) {
        long time = UserSetting.getUserSettingPreferences().getLong(UserSetting.generateUserSettingKey(checkTimeKey),0);
        long nowTime = new Date().getTime() / 3600000;
        if(nowTime - time < hour){
            return true;
        }
        return false;
    }

    public VessageUser getMyProfile(){
        return me;
    }

    public boolean isMyMobileValidated(){
        return me != null && !StringHelper.isStringNullOrWhiteSpace(me.mobile);
    }

    public boolean isMyProfileHaveChatBackground(){
        return me != null && !StringHelper.isStringNullOrWhiteSpace(me.mainChatImage);
    }

    public VessageUser getUserById(String userId){
        return getRealm().where(VessageUser.class).equalTo("userId",userId).findFirst();
    }

    public VessageUser getUserByMobile(String mobile){
        String mobileHash = DigestUtils.md5Hex(mobile);
        return getRealm().where(VessageUser.class)
                .equalTo("mobile",mobile)
                .or()
                .equalTo("mobile",mobileHash)
                .findFirst();
    }

    public void fetchUserByUserId(String userId){
        fetchUserByUserId(userId,DefaultUserUpdatedCallback);
    }

    public void fetchUserByUserId(String userId,UserUpdatedCallback handler){
        GetUserInfoRequest request = new GetUserInfoRequest();
        request.setUserId(userId);
        VessageUser user = getUserById(userId);
        if(user == null){
            fetchUserByRequest(null,request,handler);
        }else {
            fetchUserByRequest(user.lastUpdatedTime, request, handler);
        }
    }

    public void fetchUserByMobile(String mobile ,UserUpdatedCallback handler){
        GetUserInfoByMobileRequest request = new GetUserInfoByMobileRequest();
        request.setMobile(mobile);
        fetchUserByRequest(null,request, handler);
    }

    public VessageUser getCachedUserByAccountId(String accountId){
        return getRealm().where(VessageUser.class).equalTo("accountId",accountId).findFirst();
    }

    public void fetchUserByAccountId(String accountId, UserUpdatedCallback handler){
        GetUserInfoByAccountIdRequest request = new GetUserInfoByAccountIdRequest();
        request.setAccountId(accountId);
        fetchUserByRequest(null,request, handler);
    }

    public void setForceFetchUserProfileOnece(){
        forceFetchUserProfileOnece = true;
    }

    public void fetchUserByRequest(Date lastUpdatedTime,BahamutRequestBase request, final UserUpdatedCallback handler){
        if(!forceFetchUserProfileOnece && lastUpdatedTime != null){
            boolean needFetch = new Date().getTime() - lastUpdatedTime.getTime() > 20 * 60000;
            if(!needFetch){
                return;
            }
        }
        forceFetchUserProfileOnece = false;
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                VessageUser user = null;
                if(isOk){
                    getRealm().beginTransaction();
                    user = getRealm().createOrUpdateObjectFromJson(VessageUser.class, result);
                    user.lastUpdatedTime = new Date();
                    getRealm().commitTransaction();
                    postUserProfileUpdatedNotify(user);
                }
                if(handler != null){
                    handler.updated(user);
                }

            }
        });
    }

    private void postUserProfileUpdatedNotify(VessageUser user){
        postNotification(NOTIFY_USER_PROFILE_UPDATED,user.copyToObject());
    }

    public void changeMyNickName(final String newNick,final ChangeNickCallback handler){
        ChangeNickRequest req = new ChangeNickRequest();
        req.setNick(newNick);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    getRealm().beginTransaction();
                    me.nickName = newNick;
                    getRealm().commitTransaction();
                    postUserProfileUpdatedNotify(me);
                    postNotification(NOTIFY_MY_PROFILE_UPDATED,me);
                }
                if(handler != null){
                    handler.onChangeNick(isOk);
                }
            }
        });
    }

    public void changeMyMainChatImage(final String chatImage, final ChangeChatImageCallback onChangeCallback){
        ChangeMainChatImageRequest req = new ChangeMainChatImageRequest();
        req.setChatImage(chatImage);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    getRealm().beginTransaction();
                    me.mainChatImage = chatImage;
                    getRealm().commitTransaction();
                    postUserProfileUpdatedNotify(me);
                    postNotification(NOTIFY_MY_PROFILE_UPDATED,me);
                }
                if(onChangeCallback != null){
                    onChangeCallback.onChatImageChanged(isOk);
                }
            }
        });
    }

    public ChatImage[] getMyChatImages(){
        return getMyChatImages(true);
    }

    public ChatImage getMyVideoChatImage(){
        if(!isMyProfileHaveChatBackground()){
            return null;
        }
        ChatImage ci = new ChatImage();
        ci.imageType = LocalizedStringHelper.getLocalizedString(R.string.chat_image_type_video_chat);
        ci.imageId = me.mainChatImage;
        return ci;
    }

    public ChatImage[] getMyChatImages(boolean withVideoChatImage){
        ArrayList<ChatImage> res = new ArrayList<>();
        if (withVideoChatImage && isMyProfileHaveChatBackground()){
            res.add(getMyVideoChatImage());
        }
        if (myChatImages == null || myChatImages.chatImages == null){
            initMyChatImages();
        }else {
            res.addAll(0,myChatImages.chatImages);
        }
        return res.toArray(new ChatImage[0]);
    }

    public void setTypedChatImage(final String imageId, final String imageType, final ChangeChatImageCallback onChangeCallback){
        UpdateChatImageRequest req = new UpdateChatImageRequest();
        req.setImage(imageId);
        req.setImageType(imageType);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    getRealm().beginTransaction();
                    boolean exists = false;
                    for (ChatImage ci : myChatImages.chatImages){
                        if(ci.imageType.equals(imageType)){
                            ci.imageId = imageId;
                            exists = true;
                        }
                    }
                    if (!exists){
                        ChatImage ci = new ChatImage();
                        ci.imageId = imageId;
                        ci.imageType = imageType;
                        myChatImages.chatImages.add(ci);
                    }
                    getRealm().commitTransaction();
                    postNotification(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED);
                }
                onChangeCallback.onChatImageChanged(isOk);
            }
        });
    }

    public void fetchUserChatImages(String userId) {
        GetUserChatImageRequest req = new GetUserChatImageRequest();
        req.setUserId(userId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk){
                    getRealm().beginTransaction();
                    UserChatImages uci = getRealm().createOrUpdateObjectFromJson(UserChatImages.class, result);
                    if (uci.userId.equals(getMyProfile().userId)) {
                        postNotification(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED);
                    }
                    getRealm().commitTransaction();
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
                    getRealm().beginTransaction();
                    me.avatar = avatar;
                    getRealm().commitTransaction();
                    postUserProfileUpdatedNotify(me);
                    postNotification(NOTIFY_MY_PROFILE_UPDATED,me);
                }
                if(onChangeCallback != null){
                    onChangeCallback.onChangeAvatar(isOk);
                }
            }
        });
    }

    public void registNewUserByMobile(String mobile, final String noteName, final UserUpdatedCallback updatedCallback) {
        RegistMobileUserRequest req = new RegistMobileUserRequest();
        req.setMobile(mobile);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    getRealm().beginTransaction();
                    VessageUser user = getRealm().createOrUpdateObjectFromJson(VessageUser.class,result);
                    if (StringHelper.isStringNullOrWhiteSpace(user.nickName)){
                        user.nickName = noteName;
                    }
                    user.lastUpdatedTime = new Date();
                    getRealm().commitTransaction();
                    setUserNoteName(user.userId,noteName);
                    updatedCallback.updated(user);
                    postUserProfileUpdatedNotify(user);
                }else {
                    updatedCallback.updated(null);
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

    public void validateMobile(String smsAppkey,final String mobile, String zone, String code, final MobileValidateCallback callback){
        ValidateMobileVSMSRequest req = new ValidateMobileVSMSRequest();
        req.setSMSAppkey(smsAppkey);
        req.setMobile(mobile);
        req.setCode(code);
        req.setZone(zone);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                boolean isBindedNewAccount = false;
                String newAccountUserId = null;
                if (isOk) {
                    getRealm().beginTransaction();
                    try {
                        String newUserId = result.getString("newUserId");
                        if(!StringHelper.isStringNullOrWhiteSpace(newUserId)){
                            Log.d("BindAccount",me.accountId);
                            Log.d("OldUserId",me.userId);
                            Log.d("NewUserId",newUserId);
                            isBindedNewAccount = true;
                            me.userId = newUserId;
                            newAccountUserId = newUserId;
                        }

                    } catch (JSONException e) {

                    }
                    me.mobile = mobile;
                    postUserProfileUpdatedNotify(me);
                    postNotification(NOTIFY_MY_PROFILE_UPDATED,me);
                    getRealm().commitTransaction();
                }
                if(callback != null){
                    callback.onValidateMobile(isOk,isBindedNewAccount,newAccountUserId);
                }
            }
        });
    }

    public boolean registUserDeviceToken(){
        final PushAgent mPushAgent = PushAgent.getInstance(applicationContext);
        String deviceToken = null;
        String rtoken = mPushAgent.getRegistrationId();
        String stoken = UserSetting.getDeviceToken();
        if(!StringHelper.isStringNullOrWhiteSpace(rtoken))
        {
            deviceToken = rtoken;
            if(!rtoken.equals(stoken)){
                UserSetting.setDeviceToken(rtoken);
                stoken = rtoken;
            }
        }else if(!StringHelper.isStringNullOrWhiteSpace(stoken)){
            deviceToken = stoken;
        }else {
            Log.w("UserService","Device Token Not Found");
            return false;
        }

        if(deviceToken.equals(stoken) && checkTimeIsInCDForKey(REGIST_DEVICE_TOKEN_TIME_KEY,12 * 24)){
            return false;
        }

        RegistUserDeviceRequest request = new RegistUserDeviceRequest();
        request.setDeviceToken(deviceToken);
        request.setDeviceType(RegistUserDeviceRequest.DEVICE_TYPE_ANDROID);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    saveCheckTimeForKey(REGIST_DEVICE_TOKEN_TIME_KEY);
                    Log.i("UserService","regist user device success");
                }else {
                    Log.w("UserService","regist user device failure");
                }
            }
        });
        return true;
    }

    public void removeUserDevice(String deviceToken){
        RemoveUserDeviceRequest request = new RemoveUserDeviceRequest();
        request.setDeviceToken(deviceToken);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    UserSetting.getUserSettingPreferences().edit().putLong("REGIST_DEVICE_TOKEN_TIME_KEY",0).commit();
                    Log.i("UserService","user device logout");
                }else {
                    Log.w("UserService","user device logout failure");
                }
            }
        });
    }

    public void setUserNoteName(String userId,String noteName){
        UserLocalInfo info = getRealm().where(UserLocalInfo.class).equalTo("userId",userId).findFirst();
        getRealm().beginTransaction();
        if(info == null){
            info = getRealm().createObject(UserLocalInfo.class);
            info.userId = userId;
            info.noteName = noteName;
        }else {
            info.noteName = noteName;
        }
        userLocalInfos.put(userId,info.copyObject());
        getRealm().commitTransaction();
    }

    public String getUserNoteOrNickName(String userId){
        UserLocalInfo info = userLocalInfos.get(userId);
        if(info != null && info.noteName != null){
            return info.noteName;
        }else {
            VessageUser user = getUserById(userId);
            if(user != null){
                return user.nickName;
            }
        }
        return LocalizedStringHelper.getLocalizedString(R.string.vege_user);
    }

    public String getUserNotedName(String userId){
        UserLocalInfo info = userLocalInfos.get(userId);
        if(info != null && info.noteName != null){
            return info.noteName;
        }
        return null;
    }
}
