package cn.bahamut.vessage.services.activities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.common.JsonHelper;
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
public class ExtraActivitiesService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    public static final String ON_ACTIVITIES_NEW_BADGES_UPDATED = "ON_ACTIVITIES_NEW_BADGES_UPDATED";
    private List<ExtraActivityInfo> activityInfoList;
    private Realm realm;

    @Override
    public void onUserLogin(String userId) {
        if(loadEnabledActivities()){
            realm = Realm.getDefaultInstance();
            ServicesProvider.setServiceReady(ExtraActivitiesService.class);
            getActivitiesBoardData();
        }
    }

    private boolean loadEnabledActivities() {
        activityInfoList = new LinkedList<>();

        try {
            JSONObject jsonObject = new JSONObject(TextHelper.readInputStreamText(AppMain.getInstance().getResources().openRawResource(R.raw.extra_activities)));
            JSONArray activities = jsonObject.getJSONArray("activities");
            for (int i = 0; i < activities.length(); i++) {
                JSONObject acObject = activities.getJSONObject(i);
                ExtraActivityInfo activityInfo = new ExtraActivityInfo();
                activityInfo.iconResId = AppMain.getInstance().getResources().getIdentifier(acObject.getString("iconId"),"mipmap",AppMain.getInstance().getPackageName());
                activityInfo.title = LocalizedStringHelper.getLocalizedString(acObject.getString("titleId"));
                activityInfo.activityClassName = acObject.getString("entryCls");
                activityInfo.activityId = acObject.getString("activityId");
                activityInfoList.add(activityInfo);
            }
            return true;
        } catch (Exception e) {
            ServicesProvider.instance.postInitServiceFailed(ExtraActivitiesService.class,LocalizedStringHelper.getLocalizedString(R.string.load_extra_activity_config_error));
            return false;
        }
    }

    @Override
    public void onUserLogout() {
        realm.close();
        realm = null;
        ServicesProvider.setServiceNotReady(ExtraActivitiesService.class);
    }

    public ExtraActivitiesService(){

    }
    public List<ExtraActivityInfo> getEnabledActivities(){
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
                    for (BoardData activityBoardData : arr) {
                        if (isActivityEnabled(activityBoardData.getId())) {
                            totalBadge += activityBoardData.getBadge();
                            int acBadge = getEnabledActivityBadge(activityBoardData.getId());
                            acBadge += activityBoardData.getBadge();
                            addActivityBadge(activityBoardData.getId(),acBadge);
                            if (activityBoardData.isMiniBadge()) {
                                setActivityMiniBadgeShow(activityBoardData.getId());
                            }
                        }
                    }
                    if(totalBadge > 0){
                        setActivityBadgeNotified();
                        postNotification(ON_ACTIVITIES_NEW_BADGES_UPDATED, totalBadge);
                    }

                }
            }
        });
    }

    private void setActivityBadgeNotified() {
        UserSetting.getUserSettingPreferences().edit().putBoolean(UserSetting.generateUserSettingKey(ON_ACTIVITIES_NEW_BADGES_UPDATED),true).commit();
    }

    public void clearActivityBadgeNotify(){
        UserSetting.getUserSettingPreferences().edit().putBoolean(UserSetting.generateUserSettingKey(ON_ACTIVITIES_NEW_BADGES_UPDATED),false).commit();
    }

    public boolean isActivityBadgeNotified(){
        return UserSetting.getUserSettingPreferences().getBoolean(UserSetting.generateUserSettingKey(ON_ACTIVITIES_NEW_BADGES_UPDATED),false);
    }

    public void setActivityMiniBadgeShow(String id) {
        setActivityMiniBadge(id,true);
    }

    public void setActivityMiniBadgeHidden(String id){
        setActivityMiniBadge(id,false);
    }

    private void setActivityMiniBadge(String id, boolean show) {
        ExtraActivityBadge badge = getRealm().where(ExtraActivityBadge.class).equalTo("activityId",id).findFirst();
        getRealm().beginTransaction();
        if(badge == null){
            badge = getRealm().createObject(ExtraActivityBadge.class);
            badge.activityId = id;
            badge.miniBadge = show;
        }else {
            badge.miniBadge = show;
        }
        getRealm().commitTransaction();
    }

    private void addActivityBadge(String id, int badgeAddtion) {
        ExtraActivityBadge badge = getRealm().where(ExtraActivityBadge.class).equalTo("activityId",id).findFirst();
        getRealm().beginTransaction();
        if(badge == null){
            badge = getRealm().createObject(ExtraActivityBadge.class);
            badge.activityId = id;
            badge.badgeValue = badgeAddtion;
        }else {
            badge.badgeValue = badge.badgeValue + badgeAddtion;
        }
        getRealm().commitTransaction();
    }

    public void clearActivityBadge(String id){
        ExtraActivityBadge badge = getRealm().where(ExtraActivityBadge.class).equalTo("activityId",id).findFirst();
        getRealm().beginTransaction();
        if(badge == null){
            badge = getRealm().createObject(ExtraActivityBadge.class);
            badge.activityId = id;
            badge.badgeValue = 0;
        }else {
            badge.badgeValue = 0;
        }
        getRealm().commitTransaction();
    }

    public int getEnabledActivityBadge(String id) {
        ExtraActivityBadge badge = getRealm().where(ExtraActivityBadge.class).equalTo("activityId",id).findFirst();
        if(badge != null){
            return badge.badgeValue;
        }
        return 0;
    }

    public boolean isAcitityShowLittleBadge(String id){
        ExtraActivityBadge badge = getRealm().where(ExtraActivityBadge.class).equalTo("activityId",id).findFirst();
        if(badge != null){
            return badge.miniBadge;
        }
        return false;
    }

    public boolean isActivityEnabled(String id) {
        return true;
    }

    public Realm getRealm() {
        return realm;
    }
}
