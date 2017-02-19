package cn.bahamut.vessage.services.activities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.common.TextHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;
import io.realm.Realm;

/**
 * Created by alexchow on 16/5/16.
 */
public class ExtraActivitiesService extends Observable implements OnServiceUserLogin,OnServiceUserLogout {

    public static final String ON_ACTIVITIES_NEW_BADGES_UPDATED = "ON_ACTIVITIES_NEW_BADGES_UPDATED";
    private static final String FIRST_ACTIVITY_BADGE_VERSION = "FIRST_ACTIVITY_BADGE_VERSION";
    private static final int ACTIVITY_BADGE_VERSION = 1; //Increase while new activity online

    public static final String ON_ACTIVITY_BADGE_UPDATED = "ON_ACTIVITY_BADGE_UPDATED";

    private List<ExtraActivityInfo> activityInfoList;

    private Map<String, ExtraActivityInfo> registedActivitiesInfo;

    @Override
    public void onUserLogin(String userId) {
        if (loadEnabledActivities()) {
            ServicesProvider.setServiceReady(ExtraActivitiesService.class);
            getActivitiesBoardData();
        }
    }

    private boolean loadEnabledActivities() {
        activityInfoList = new LinkedList<>();
        registedActivitiesInfo = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(TextHelper.readInputStreamText(AppMain.getInstance().getResources().openRawResource(R.raw.extra_activities)));
            JSONArray activities = jsonObject.getJSONArray("activities");
            for (int i = 0; i < activities.length(); i++) {
                JSONObject acObject = activities.getJSONObject(i);
                ExtraActivityInfo activityInfo = new ExtraActivityInfo();

                activityInfo.iconResId = AppMain.getInstance().getResources().getIdentifier(acObject.getString("iconId"), "drawable", AppMain.getInstance().getPackageName());
                activityInfo.title = LocalizedStringHelper.getLocalizedString(acObject.getString("titleId"));
                activityInfo.activityClassName = acObject.getString("entryCls");
                activityInfo.activityId = acObject.getString("activityId");
                activityInfoList.add(activityInfo);
                registedActivitiesInfo.put(activityInfo.activityId, activityInfo);
            }

            ExtraActivityInfo nearActiveUserAc = new ExtraActivityInfo();
            nearActiveUserAc.title = LocalizedStringHelper.getLocalizedString(R.string.near_active_user_ac_title);
            nearActiveUserAc.activityId = "100";
            registedActivitiesInfo.put(nearActiveUserAc.activityId, nearActiveUserAc);

            return true;
        } catch (Exception e) {
            ServicesProvider.postInitServiceFailed(ExtraActivitiesService.class, LocalizedStringHelper.getLocalizedString(R.string.load_extra_activity_config_error));
            return false;
        }
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ExtraActivitiesService.class);
    }

    public ExtraActivitiesService() {

    }

    public List<ExtraActivityInfo> getEnabledActivities() {
        return activityInfoList;
    }

    public void getActivitiesBoardData() {
        GetActivitiesBoardDataRequest req = new GetActivitiesBoardDataRequest();
        BahamutRFKit.getClient(APIClient.class).executeRequestArray(req, new OnRequestCompleted<JSONArray>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONArray result) {
                if (isOk) {
                    BoardData[] arr = JsonHelper.parseArray(result, BoardData.class);
                    int totalBadge = 0;
                    boolean miniBadge = false;
                    for (BoardData activityBoardData : arr) {
                        if (isActivityEnabled(activityBoardData.getId())) {
                            totalBadge += activityBoardData.getBadge();
                            int acBadge = getEnabledActivityBadge(activityBoardData.getId());
                            acBadge += activityBoardData.getBadge();
                            addActivityBadge(activityBoardData.getId(), acBadge, true);
                            if (activityBoardData.isMiniBadge()) {
                                miniBadge = true;
                                setActivityMiniBadge(activityBoardData.getId(), true, true);
                            }
                        }
                    }
                    if (totalBadge > 0 || miniBadge) {
                        setActivityBadgeNotified();
                        postNotification(ON_ACTIVITIES_NEW_BADGES_UPDATED, totalBadge);
                    }
                }
            }
        });
    }

    private void setActivityBadgeNotified() {
        UserSetting.getUserSettingPreferences().edit().putBoolean(UserSetting.generateUserSettingKey(ON_ACTIVITIES_NEW_BADGES_UPDATED), true).commit();
    }

    public void clearActivityBadgeNotify() {
        UserSetting.getUserSettingPreferences().edit().putBoolean(UserSetting.generateUserSettingKey(ON_ACTIVITIES_NEW_BADGES_UPDATED), false).commit();
    }

    public boolean isActivityBadgeNotified() {
        if (UserSetting.getUserSettingPreferences().getInt(UserSetting.generateUserSettingKey(FIRST_ACTIVITY_BADGE_VERSION), 0) < ACTIVITY_BADGE_VERSION) {
            UserSetting.getUserSettingPreferences().edit().putInt(UserSetting.generateUserSettingKey(FIRST_ACTIVITY_BADGE_VERSION), ACTIVITY_BADGE_VERSION).commit();
            return true;
        }
        return UserSetting.getUserSettingPreferences().getBoolean(UserSetting.generateUserSettingKey(ON_ACTIVITIES_NEW_BADGES_UPDATED), false);
    }

    public void setActivityMiniBadgeShow(String id) {
        setActivityMiniBadge(id, true, true);
    }

    public void setActivityMiniBadgeHidden(String id) {
        setActivityMiniBadge(id, false, true);
    }

    private void setActivityMiniBadge(String id, boolean show, boolean notify) {
        try (Realm realm = Realm.getDefaultInstance()) {

            ExtraActivityBadge badge = realm.where(ExtraActivityBadge.class).equalTo("activityId", id).findFirst();
            realm.beginTransaction();
            if (badge == null) {
                badge = realm.createObject(ExtraActivityBadge.class);
                badge.activityId = id;
                badge.miniBadge = show;
            } else if (badge.miniBadge != show) {
                badge.miniBadge = show;
            } else {
                notify = false;
            }
            if (notify) {
                postNotification(ON_ACTIVITY_BADGE_UPDATED, badge.copyToObject());
            }
            realm.commitTransaction();
        }
    }

    private void addActivityBadge(String id, int badgeAddtion, boolean notify) {
        try (Realm realm = Realm.getDefaultInstance()) {
            ExtraActivityBadge badge = realm.where(ExtraActivityBadge.class).equalTo("activityId", id).findFirst();
            realm.beginTransaction();
            if (badge == null) {
                badge = realm.createObject(ExtraActivityBadge.class);
                badge.activityId = id;
                badge.badgeValue = badgeAddtion;
            } else if (badgeAddtion != 0) {
                badge.badgeValue = badge.badgeValue + badgeAddtion;
            } else {
                notify = false;
            }
            if (notify) {
                postNotification(ON_ACTIVITY_BADGE_UPDATED, badge.copyToObject());
            }
            realm.commitTransaction();
        }
    }

    public void clearActivityBadge(String id) {
        clearActivityBadge(id, true);
    }

    private void clearActivityBadge(String id, boolean notify) {
        try (Realm realm = Realm.getDefaultInstance()) {
            ExtraActivityBadge badge = realm.where(ExtraActivityBadge.class).equalTo("activityId", id).findFirst();
            realm.beginTransaction();
            if (badge == null) {
                badge = realm.createObject(ExtraActivityBadge.class);
                badge.activityId = id;
                badge.badgeValue = 0;
            } else if (badge.badgeValue != 0) {
                badge.badgeValue = 0;
            } else {
                notify = false;
            }
            if (notify) {
                postNotification(ON_ACTIVITY_BADGE_UPDATED, badge.copyToObject());
            }
            realm.commitTransaction();
        }
    }

    public void clearActivityAllBadge(String id) {
        setActivityMiniBadge(id, false, true);
        clearActivityBadge(id, true);
    }

    public int getEnabledActivityBadge(String id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            ExtraActivityBadge badge = realm.where(ExtraActivityBadge.class).equalTo("activityId", id).findFirst();
            int result = badge != null ? badge.badgeValue : 0;
            return result;
        }
    }

    public boolean isAcitityShowLittleBadge(String id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            ExtraActivityBadge badge = realm.where(ExtraActivityBadge.class).equalTo("activityId", id).findFirst();
            boolean result = badge != null && badge.miniBadge;

            return result;
        }
    }

    public boolean isActivityEnabled(String id) {
        return true;
    }

    public ExtraActivityInfo getActivityInfo(String activityId) {
        for (ExtraActivityInfo extraActivityInfo : activityInfoList) {
            if (extraActivityInfo.activityId.equals(activityId)) {
                return extraActivityInfo;
            }
        }
        return null;
    }

    public String getActivityName(String activityId) {
        if (registedActivitiesInfo != null) {
            ExtraActivityInfo info = registedActivitiesInfo.get(activityId);
            if (info != null && !StringHelper.isStringNullOrWhiteSpace(info.title)) {
                return info.title;
            }
        }
        return LocalizedStringHelper.getLocalizedString(R.string.unknow_activity);
    }
}
