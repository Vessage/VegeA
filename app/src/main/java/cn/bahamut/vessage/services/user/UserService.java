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
import cn.bahamut.vessage.restfulapi.user.ChangeMottoRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeNickRequest;
import cn.bahamut.vessage.restfulapi.user.ChangeSexRequest;
import cn.bahamut.vessage.restfulapi.user.GetActiveUsersRequest;
import cn.bahamut.vessage.restfulapi.user.GetNearUsersRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserChatImageRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByAccountIdRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoByMobileRequest;
import cn.bahamut.vessage.restfulapi.user.GetUserInfoRequest;
import cn.bahamut.vessage.restfulapi.user.GetUsersProfileRequest;
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
public class UserService extends Observable implements OnServiceUserLogin,OnServiceUserLogout,OnServiceInit {

    public static final String NOTIFY_MY_PROFILE_UPDATED = "NOTIFY_MY_PROFILE_UPDATED";
    public static final String NOTIFY_USER_PROFILE_UPDATED = "NOTIFY_USER_PROFILE_UPDATED";
    public static final String NOTIFY_MY_CHAT_IMAGES_UPDATED = "NOTIFY_MY_CHAT_IMAGES_UPDATED";
    private static final String FETCH_ACTIVE_USER_TIME_KEY = "FETCH_ACTIVE_USER_TIME";
    private static final String REGIST_DEVICE_TOKEN_TIME_KEY = "REGIST_DEVICE_TOKEN_TIME";
    private static final String FETCH_NEAR_USER_TIME_KEY = "FETCH_NEAR_USER_TIME";

    private static final String DEFAULT_TEMP_MOBILE = "13600000000";
    private static final String USE_TMP_MOBILE_KEY = "USE_TMP_MOBILE";

    private Context applicationContext;
    private boolean forceFetchUserProfileOnece = false;
    private List<VessageUser> nearUsers;
    private UserChatImages myChatImages;
    private HashMap<String, UserLocalInfo> userLocalInfos = new HashMap<>();

    private String sendVessageExtraInfo;

    @Override
    public void onServiceInit(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void fetchUserByMobile(String mobile) {
        fetchUserByMobile(mobile, DefaultUserUpdatedCallback);
    }

    public List<VessageUser> getActiveUsers() {
        ArrayList<VessageUser> result = new ArrayList<>();
        if (activeUsers == null) {
            activeUsers = new ArrayList<>();
        }
        for (VessageUser activeUser : activeUsers) {
            result.add(activeUser);
        }
        return result;
    }

    public List<VessageUser> getNearUsers() {
        ArrayList<VessageUser> result = new ArrayList<>();
        if (nearUsers == null) {
            nearUsers = new ArrayList<>();
        }
        for (VessageUser user : nearUsers) {
            result.add(user);
        }
        return result;
    }

    private void generateVessageExtraInfo() {
        String nick = getMyProfile().nickName;
        String mobile = getMyProfile().mobile;
        String mobileHash = "";
        if (!StringHelper.isStringNullOrWhiteSpace(mobile)) {
            mobileHash = DigestUtils.md5Hex(mobile);
        }
        sendVessageExtraInfo = String.format("{\"accountId\":\"%s\",\"nickName\":\"%s\"}", getMyProfile().accountId, nick);
    }

    public void fetchUserProfilesByUserIds(List<String> userIds) {
        GetUsersProfileRequest req = new GetUsersProfileRequest();
        req.setUserIds(userIds);
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        realm.createOrUpdateAllFromJson(VessageUser.class, result);
                        realm.commitTransaction();
                        VessageUser[] users = JsonHelper.parseArray(result, VessageUser.class);
                        for (VessageUser user : users) {
                            postUserProfileUpdatedNotify(user);
                        }
                    }
                }
            }
        });
    }

    public interface ChangeValueReturnBooleanCallback {
        void onChanged(boolean isChanged);
    }

    public interface UserUpdatedCallback {
        void updated(VessageUser user);
    }

    public interface MobileValidateCallback {
        void onValidateMobile(boolean validated, boolean isBindedNewAccount, String newAccountUserId);
    }

    public static final UserUpdatedCallback DefaultUserUpdatedCallback = new UserUpdatedCallback() {
        @Override
        public void updated(VessageUser user) {
        }
    };

    private volatile VessageUser me;

    private List<VessageUser> activeUsers;

    @Override
    public void onUserLogin(String userId) {
        initMe(userId);
        initUserLocalInfo();
        MobclickAgent.onProfileSignIn(UserSetting.getLastUserLoginedAccount());
    }

    private void initUserLocalInfo() {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<UserLocalInfo> results = realm.where(UserLocalInfo.class).findAll();
            for (UserLocalInfo userLocalInfo : results) {
                userLocalInfos.put(userLocalInfo.userId, userLocalInfo.copyObject());
            }
        }
    }

    @Override
    public void onUserLogout() {
        resetCheckTimeForKey(REGIST_DEVICE_TOKEN_TIME_KEY);
        myChatImages = null;
        me = null;

        userLocalInfos.clear();
        getActiveUsers().clear();
        getNearUsers().clear();
        MobclickAgent.onProfileSignOff();
        ServicesProvider.setServiceNotReady(UserService.class);
    }

    private void initMe(String userId) {
        Log.d("LoginUser", userId);
        VessageUser user = getUserById(userId);

        if (user == null) {
            setForceFetchUserProfileOnece();
            fetchUserByUserId(userId, new UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if (user != null) {
                        setMe(user);
                        generateVessageExtraInfo();
                        //initMyChatImages();
                        registUserDeviceToken();
                        fetchActiveUsersFromServer(false);
                        ServicesProvider.setServiceReady(UserService.class);
                    } else {
                        ServicesProvider.postInitServiceFailed(UserService.class, LocalizedStringHelper.getLocalizedString(R.string.init_user_data_error));
                    }
                }
            });
        } else {
            setMe(user);
            generateVessageExtraInfo();
            registUserDeviceToken();
            //initMyChatImages();
            setForceFetchUserProfileOnece();
            fetchUserByUserId(userId, new UserUpdatedCallback() {
                @Override
                public void updated(VessageUser user) {
                    if (user != null) {
                        setMe(user);
                        generateVessageExtraInfo();
                    }
                }
            });
            fetchActiveUsersFromServer(false);
            ServicesProvider.setServiceReady(UserService.class);
        }
    }

    private void setMe(VessageUser me) {
        this.me = me;
        if (isMyMobileValidated() == false && UserSetting.getUserSettingPreferences().getBoolean(USE_TMP_MOBILE_KEY, false)) {
            try (Realm realm = Realm.getDefaultInstance()) {
                VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                realm.beginTransaction();
                user.mobile = DEFAULT_TEMP_MOBILE;
                this.me.mobile = DEFAULT_TEMP_MOBILE;
                realm.commitTransaction();
            }
        }
    }

    private void initMyChatImages() {
        try (Realm realm = Realm.getDefaultInstance()) {
            UserChatImages userChatImages = realm.where(UserChatImages.class).equalTo("userId", me.userId).findFirst();
            if (userChatImages == null) {
                fetchUserChatImages(me.userId);
            }else {
                myChatImages = userChatImages.copyToObject();
            }
        }
    }

    private volatile boolean fetchingActiveUsers = false;

    public void fetchActiveUsersFromServer(boolean checkTime) {
        if (fetchingActiveUsers || (activeUsers != null && activeUsers.size() > 0 && checkTime && checkTimeIsInCDForKey(FETCH_ACTIVE_USER_TIME_KEY, 3))) {
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
                    VessageUser[] activeUsers = JsonHelper.parseArray(result, VessageUser.class);
                    if (UserService.this.activeUsers == null) {
                        UserService.this.activeUsers = new ArrayList(activeUsers.length);
                    }
                    UserService.this.activeUsers.clear();
                    int i = 0;
                    for (VessageUser activeUser : activeUsers) {
                        try {
                            JSONObject jsonObject = result.getJSONObject(i);
                            activeUser.setRealmUnSupportProperties(jsonObject);
                        } catch (JSONException e) {
                        }
                        UserService.this.activeUsers.add(activeUser);
                        postUserProfileUpdatedNotify(activeUser);
                    }
                } else if (UserService.this.activeUsers != null) {
                    UserService.this.activeUsers.clear();
                }
            }
        });
    }

    private volatile boolean fetchingNearUsers = false;

    public void fetchNearUsers(String location, boolean checkTime) {
        if (fetchingNearUsers || (nearUsers != null && nearUsers.size() > 0 && checkTime && checkTimeIsInCDForKey(FETCH_NEAR_USER_TIME_KEY, 3))) {
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
                    VessageUser[] nearUsers = JsonHelper.parseArray(result, VessageUser.class);
                    if (UserService.this.nearUsers == null) {
                        UserService.this.nearUsers = new ArrayList(nearUsers.length);
                    }
                    UserService.this.nearUsers.clear();
                    int i = 0;
                    for (VessageUser user : nearUsers) {
                        try {
                            JSONObject jsonObject = result.getJSONObject(i);
                            user.setRealmUnSupportProperties(jsonObject);
                        } catch (JSONException e) {
                        }
                        UserService.this.nearUsers.add(user);
                        postUserProfileUpdatedNotify(user);
                    }
                } else if (UserService.this.nearUsers != null) {
                    UserService.this.nearUsers.clear();
                }
            }
        });
    }

    private void resetCheckTimeForKey(String key) {
        UserSetting.getUserSettingPreferences().edit().putLong(UserSetting.generateUserSettingKey(key), 0).commit();
    }

    private void saveCheckTimeForKey(String key) {
        long nowTime = new Date().getTime() / 3600000;
        UserSetting.getUserSettingPreferences().edit().putLong(UserSetting.generateUserSettingKey(key), nowTime).commit();
    }

    private boolean checkTimeIsInCDForKey(String checkTimeKey, int hour) {
        long time = UserSetting.getUserSettingPreferences().getLong(UserSetting.generateUserSettingKey(checkTimeKey), 0);
        long nowTime = new Date().getTime() / 3600000;
        if (nowTime - time < hour) {
            return true;
        }
        return false;
    }

    public VessageUser getMyProfile() {
        return me;
    }

    public String getSendVessageExtraInfo() {
        if (StringHelper.isStringNullOrWhiteSpace(sendVessageExtraInfo)) {
            generateVessageExtraInfo();
        }
        return sendVessageExtraInfo;
    }

    public boolean isMyMobileValidated() {
        return me != null && !StringHelper.isStringNullOrWhiteSpace(me.mobile);
    }

    public boolean isMyProfileHaveChatBackground() {
        return me != null && !StringHelper.isStringNullOrWhiteSpace(me.mainChatImage);
    }

    public VessageUser getUserById(String userId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            VessageUser user = realm.where(VessageUser.class).equalTo("userId", userId).findFirst();
            return user != null ? user.copyToObject() : null;
        }
    }

    public VessageUser getUserByMobile(String mobile) {
        String mobileHash = DigestUtils.md5Hex(mobile);
        try (Realm realm = Realm.getDefaultInstance()) {
            VessageUser user = realm.where(VessageUser.class)
                    .equalTo("mobile", mobile)
                    .or()
                    .equalTo("mobile", mobileHash)
                    .findFirst();
            return user != null ? user.copyToObject() : null;
        }
    }

    public void fetchUserByUserId(String userId) {
        fetchUserByUserId(userId, DefaultUserUpdatedCallback);
    }

    public void fetchUserByUserId(String userId, UserUpdatedCallback handler) {
        GetUserInfoRequest request = new GetUserInfoRequest();
        request.setUserId(userId);
        VessageUser user = getUserById(userId);
        if (user == null) {
            fetchUserByRequest(null, request, handler);
        } else {
            fetchUserByRequest(user.lastUpdatedTime, request, handler);
        }
    }

    public void fetchUserByMobile(String mobile, UserUpdatedCallback handler) {
        GetUserInfoByMobileRequest request = new GetUserInfoByMobileRequest();
        request.setMobile(mobile);
        fetchUserByRequest(null, request, handler);
    }

    public VessageUser getCachedUserByAccountId(String accountId) {
        try (Realm realm = Realm.getDefaultInstance()) {
            VessageUser user = realm.where(VessageUser.class).equalTo("accountId", accountId).findFirst();
            return user != null ? user.copyToObject() : null;
        }
    }

    public void fetchUserByAccountId(String accountId, UserUpdatedCallback handler) {
        GetUserInfoByAccountIdRequest request = new GetUserInfoByAccountIdRequest();
        request.setAccountId(accountId);
        fetchUserByRequest(null, request, handler);
    }

    public void setForceFetchUserProfileOnece() {
        forceFetchUserProfileOnece = true;
    }

    public void fetchUserByRequest(Date lastUpdatedTime, BahamutRequestBase request, final UserUpdatedCallback handler) {
        if (!forceFetchUserProfileOnece && lastUpdatedTime != null) {
            boolean needFetch = new Date().getTime() - lastUpdatedTime.getTime() > 20 * 60000;
            if (!needFetch) {
                return;
            }
        }
        forceFetchUserProfileOnece = false;
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                VessageUser user = null;
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        user = realm.createOrUpdateObjectFromJson(VessageUser.class, result);
                        user.setRealmUnSupportProperties(result);
                        user.lastUpdatedTime = new Date();
                        realm.commitTransaction();
                        user = user.copyToObject();
                        postUserProfileUpdatedNotify(user);
                    }
                }
                if (handler != null) {
                    handler.updated(user);
                }

            }
        });
    }

    private void postUserProfileUpdatedNotify(VessageUser user) {
        postNotification(NOTIFY_USER_PROFILE_UPDATED, user.copyToObject());
    }

    public void changeMyNickName(final String newNick, final ChangeValueReturnBooleanCallback handler) {
        ChangeNickRequest req = new ChangeNickRequest();
        req.setNick(newNick);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                        realm.beginTransaction();
                        user.nickName = newNick;
                        me.nickName = newNick;
                        realm.commitTransaction();
                        generateVessageExtraInfo();
                        postUserProfileUpdatedNotify(me);
                        postNotification(NOTIFY_MY_PROFILE_UPDATED, me);
                    }
                }
                if (handler != null) {
                    handler.onChanged(isOk);
                }
            }
        });
    }

    public void changeMySex(final int newSex, final ChangeValueReturnBooleanCallback handler) {
        ChangeSexRequest req = new ChangeSexRequest();
        req.setSex(newSex);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                        realm.beginTransaction();
                        user.sex = newSex;
                        me.sex = newSex;
                        realm.commitTransaction();
                        postUserProfileUpdatedNotify(me);
                        postNotification(NOTIFY_MY_PROFILE_UPDATED, me);
                    }
                }
                if (handler != null) {
                    handler.onChanged(isOk);
                }
            }
        });
    }

    public void changeMyMotto(final String newMotto, final ChangeValueReturnBooleanCallback handler) {
        ChangeMottoRequest req = new ChangeMottoRequest();
        req.setMotto(newMotto);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                        realm.beginTransaction();
                        user.motto = newMotto;
                        me.motto = newMotto;
                        realm.commitTransaction();
                        postUserProfileUpdatedNotify(me);
                        postNotification(NOTIFY_MY_PROFILE_UPDATED, me);
                    }
                }
                if (handler != null) {
                    handler.onChanged(isOk);
                }
            }
        });
    }

    public void changeMyAvatar(final String avatar, final ChangeValueReturnBooleanCallback onChangeCallback) {
        ChangeAvatarRequest req = new ChangeAvatarRequest();
        req.setAvatar(avatar);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                        realm.beginTransaction();
                        user.avatar = avatar;
                        me.avatar = avatar;
                        realm.commitTransaction();
                        postUserProfileUpdatedNotify(me);
                        postNotification(NOTIFY_MY_PROFILE_UPDATED, me);
                    }
                }
                if (onChangeCallback != null) {
                    onChangeCallback.onChanged(isOk);
                }
            }
        });
    }

    @Deprecated
    private void changeMyMainChatImage(final String chatImage, final ChangeValueReturnBooleanCallback onChangeCallback) {
        ChangeMainChatImageRequest req = new ChangeMainChatImageRequest();
        req.setChatImage(chatImage);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                        realm.beginTransaction();
                        user.mainChatImage = chatImage;
                        me.mainChatImage = chatImage;
                        realm.commitTransaction();
                        postUserProfileUpdatedNotify(me);
                        postNotification(NOTIFY_MY_PROFILE_UPDATED, me);
                    }
                }
                if (onChangeCallback != null) {
                    onChangeCallback.onChanged(isOk);
                }
            }
        });
    }

    @Deprecated
    private ChatImage[] getMyChatImages() {
        return getMyChatImages(true);
    }

    @Deprecated
    private ChatImage getMyVideoChatImage() {
        if (!isMyProfileHaveChatBackground()) {
            return null;
        }
        ChatImage ci = new ChatImage();
        ci.imageType = LocalizedStringHelper.getLocalizedString(R.string.chat_image_type_video_chat);
        ci.imageId = me.mainChatImage;
        return ci;
    }

    @Deprecated
    private ChatImage[] getMyChatImages(boolean withVideoChatImage) {
        ArrayList<ChatImage> res = new ArrayList<>();
        if (withVideoChatImage && isMyProfileHaveChatBackground()) {
            res.add(getMyVideoChatImage());
        }
        if (myChatImages == null || myChatImages.chatImages == null) {
            initMyChatImages();
        } else {
            res.addAll(0, myChatImages.chatImages);
        }
        return res.toArray(new ChatImage[0]);
    }

    @Deprecated
    private void setTypedChatImage(final String imageId, final String imageType, final ChangeValueReturnBooleanCallback onChangeCallback) {
        UpdateChatImageRequest req = new UpdateChatImageRequest();
        req.setImage(imageId);
        req.setImageType(imageType);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();

                        boolean exists = false;
                        for (ChatImage ci : myChatImages.chatImages) {
                            if (ci.imageType.equals(imageType)) {
                                ci.imageId = imageId;
                                exists = true;
                            }
                        }
                        if (!exists) {
                            ChatImage ci = new ChatImage();
                            ci.imageId = imageId;
                            ci.imageType = imageType;
                            myChatImages.chatImages.add(ci);
                        }

                        realm.insertOrUpdate(myChatImages);
                        realm.commitTransaction();
                        postNotification(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED);
                    }
                }
                onChangeCallback.onChanged(isOk);
            }
        });
    }

    @Deprecated
    private void fetchUserChatImages(String userId) {
        GetUserChatImageRequest req = new GetUserChatImageRequest();
        req.setUserId(userId);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        UserChatImages uci = realm.createOrUpdateObjectFromJson(UserChatImages.class, result);
                        if (uci.userId.equals(getMyProfile().userId)) {
                            myChatImages = uci.copyToObject();
                            postNotification(UserService.NOTIFY_MY_CHAT_IMAGES_UPDATED);
                        }
                        realm.commitTransaction();
                    }
                }

            }
        });
    }

    @Deprecated
    private void registNewUserByMobile(String mobile, final String noteName, final UserUpdatedCallback updatedCallback) {
        RegistMobileUserRequest req = new RegistMobileUserRequest();
        req.setMobile(mobile);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.beginTransaction();
                        VessageUser user = realm.createOrUpdateObjectFromJson(VessageUser.class, result);
                        if (StringHelper.isStringNullOrWhiteSpace(user.nickName)) {
                            user.nickName = noteName;
                        }
                        user.lastUpdatedTime = new Date();
                        realm.commitTransaction();
                        setUserNoteName(user.userId, noteName);
                        updatedCallback.updated(user.copyToObject());
                        postUserProfileUpdatedNotify(user.copyToObject());
                    }
                } else {
                    updatedCallback.updated(null);
                }

            }
        });
    }

    public void sendValidateCodeToMobile(String mobile) {
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

    public void setTempMobile() {
        try (Realm realm = Realm.getDefaultInstance()) {
            VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
            realm.beginTransaction();
            user.mobile = DEFAULT_TEMP_MOBILE;
            me.mobile = DEFAULT_TEMP_MOBILE;
            realm.commitTransaction();
        }
        UserSetting.getUserSettingPreferences().edit().putBoolean(UserSetting.generateUserSettingKey(USE_TMP_MOBILE_KEY), true).commit();
    }

    public boolean isUsingTempMobile() {
        return DEFAULT_TEMP_MOBILE.equals(me.mobile);
    }

    public void validateMobile(String smsAppkey, boolean bindExistsAccount, final String mobile, String zone, String code, final MobileValidateCallback callback) {
        ValidateMobileVSMSRequest req = new ValidateMobileVSMSRequest();
        req.setSMSAppkey(smsAppkey);
        req.setMobile(mobile);
        req.setCode(code);
        req.setZone(zone);
        req.setBindExistsAccount(bindExistsAccount);
        BahamutRFKit.getClient(APIClient.class).executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                boolean isBindedNewAccount = false;
                String newAccountUserId = null;
                if (isOk) {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        VessageUser user = realm.where(VessageUser.class).equalTo("userId", me.userId).findFirst();
                        realm.beginTransaction();
                        try {
                            String newUserId = result.getString("newUserId");
                            if (!StringHelper.isStringNullOrWhiteSpace(newUserId)) {
                                Log.d("BindAccount", me.accountId);
                                Log.d("OldUserId", me.userId);
                                Log.d("NewUserId", newUserId);
                                isBindedNewAccount = true;
                                me.userId = newUserId;
                                user.userId = newUserId;
                                newAccountUserId = newUserId;
                            }

                        } catch (JSONException e) {

                        }
                        me.mobile = mobile;
                        user.mobile = mobile;
                        postUserProfileUpdatedNotify(me);
                        postNotification(NOTIFY_MY_PROFILE_UPDATED, me);
                        realm.commitTransaction();
                    }
                }
                if (callback != null) {
                    callback.onValidateMobile(isOk, isBindedNewAccount, newAccountUserId);
                }
            }
        });
    }

    public boolean registUserDeviceToken() {
        final PushAgent mPushAgent = PushAgent.getInstance(applicationContext);
        String deviceToken = null;
        String rtoken = mPushAgent.getRegistrationId();
        String stoken = UserSetting.getDeviceToken();
        if (!StringHelper.isStringNullOrWhiteSpace(rtoken)) {
            deviceToken = rtoken;
            if (!rtoken.equals(stoken)) {
                UserSetting.setDeviceToken(rtoken);
                stoken = rtoken;
            }
        } else if (!StringHelper.isStringNullOrWhiteSpace(stoken)) {
            deviceToken = stoken;
        } else {
            Log.w("UserService", "Device Token Not Found");
            return false;
        }

        if (deviceToken.equals(stoken) && checkTimeIsInCDForKey(REGIST_DEVICE_TOKEN_TIME_KEY, 12 * 24)) {
            return false;
        }

        RegistUserDeviceRequest request = new RegistUserDeviceRequest();
        request.setDeviceToken(deviceToken);
        request.setDeviceType(RegistUserDeviceRequest.DEVICE_TYPE_ANDROID);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    saveCheckTimeForKey(REGIST_DEVICE_TOKEN_TIME_KEY);
                    Log.i("UserService", "regist user device success");
                } else {
                    Log.w("UserService", "regist user device failure");
                }
            }
        });
        return true;
    }

    public void removeUserDevice(String deviceToken) {
        RemoveUserDeviceRequest request = new RemoveUserDeviceRequest();
        request.setDeviceToken(deviceToken);
        BahamutRFKit.getClient(APIClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    UserSetting.getUserSettingPreferences().edit().putLong("REGIST_DEVICE_TOKEN_TIME_KEY", 0).commit();
                    Log.i("UserService", "user device logout");
                } else {
                    Log.w("UserService", "user device logout failure");
                }
            }
        });
    }

    public void setUserNoteName(String userId, String noteName) {
        try (Realm realm = Realm.getDefaultInstance()) {
            UserLocalInfo info = realm.where(UserLocalInfo.class).equalTo("userId", userId).findFirst();
            realm.beginTransaction();
            if (info == null) {
                info = new UserLocalInfo();
                info.userId = userId;
                info.noteName = noteName;
                info = realm.copyToRealmOrUpdate(info);
            } else {
                info.noteName = noteName;
            }
            userLocalInfos.put(userId, info.copyObject());
            realm.commitTransaction();
        }
    }

    public String getUserNotedNameIfExists(String userId){
        UserLocalInfo info = userLocalInfos.get(userId);
        if (info != null && info.noteName != null) {
            return info.noteName;
        }
        return null;
    }

    public String getUserNoteOrNickName(String userId) {
        if (getMyProfile().userId.equals(userId)){
            return LocalizedStringHelper.getLocalizedString(R.string.me);
        }
        UserLocalInfo info = userLocalInfos.get(userId);
        if (info != null && info.noteName != null) {
            return info.noteName;
        } else {
            VessageUser user = getUserById(userId);
            if (user != null) {
                return user.nickName;
            }
        }
        return LocalizedStringHelper.getLocalizedString(R.string.unknow_vg_user);
    }

    public String getUserNotedName(String userId) {
        if (UserSetting.getUserId().equals(userId)) {
            return LocalizedStringHelper.getLocalizedString(R.string.me);
        }
        UserLocalInfo info = userLocalInfos.get(userId);
        if (info != null && info.noteName != null) {
            return info.noteName;
        }
        return null;
    }
}