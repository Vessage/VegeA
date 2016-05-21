package cn.bahamut.vessage.services.activities;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.common.StringHelper;
import cn.bahamut.observer.Observable;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.main.UserSetting;

/**
 * Created by alexchow on 16/5/16.
 */
public class ExtraActivitiesService extends Observable implements OnServiceUserLogin,OnServiceUserLogout{

    private static final String ON_ACTIVITIES_NEW_BADGES_UPDATED = "ON_ACTIVITIES_NEW_BADGES_UPDATED";
    private static final String ACTIVITIES_BADGES_MAP = "ACTIVITIES_BADGES_MAP";
    private List<ExtraActivityInfo> activityInfoList;
    private HashMap<String,Integer> activitiesBadges;
    private HashMap<String,Boolean> activitiesMiniBadges;

    @Override
    public void onUserLogin(String userId) {
        loadEnabledActivities();
        loadActivitiesBadges();
        ServicesProvider.setServiceReady(ExtraActivitiesService.class);
    }

    private void loadEnabledActivities() {
        ExtraActivityInfo littlePaper = new ExtraActivityInfo();
        littlePaper.iconResId = R.mipmap.little_paper_icon;
        littlePaper.title = LocalizedStringHelper.getLocalizedString(R.string.little_paper);
        littlePaper.activityClassName = "cn.bahamut.vessage.activities.littlepaper.LittlePaperMainActivity";

        activityInfoList = new LinkedList<>();
        activityInfoList.add(littlePaper);
    }

    @Override
    public void onUserLogout() {
        storeActivitiesBadges();
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
                    ActivityBoardData[] arr = JsonHelper.parseArray(result, ActivityBoardData.class);
                    int totalBadge = 0;
                    for (ActivityBoardData activityBoardData : arr) {
                        if (isActivityEnabled(activityBoardData.getId())) {
                            totalBadge += activityBoardData.getBadge();
                            int acBadge = getEnabledActivityBadge(activityBoardData.getId());
                            acBadge += activityBoardData.getBadge();
                            addActivityBadge(activityBoardData.getId(), acBadge,false);
                            if (activityBoardData.isMiniBadge()) {
                                setActivityMiniBadgeShow(activityBoardData.getId(),false);
                            }
                        }
                    }
                    storeActivitiesBadges();
                    postNotification(ON_ACTIVITIES_NEW_BADGES_UPDATED, totalBadge);
                }
            }
        });
    }

    private void loadActivitiesBadges(){
        //Activity Badge Data store as "{id},{badges},{isMiniBadgeShow};..."
        activitiesBadges = new HashMap<>();
        activitiesMiniBadges = new HashMap<>();
        String arrStrings = UserSetting.getUserSettingPreferences().getString(UserSetting.generateUserSettingKey(ACTIVITIES_BADGES_MAP),"");
        String[] activitiesData = arrStrings.split(";");
        for (String activityBadgeInfo : activitiesData) {
            if(StringHelper.isStringNullOrWhiteSpace(activityBadgeInfo)){
                continue;
            }
            String[] infos = activityBadgeInfo.split(",");
            String id = infos[0];
            int badge = Integer.parseInt(infos[1]);
            boolean miniBadge = Boolean.parseBoolean(infos[2]);
            activitiesMiniBadges.put(id,miniBadge);
            activitiesBadges.put(id,badge);
        }
    }

    private void storeActivitiesBadges(){
        //Activity Badge Data store as "{id},{badges},{isMiniBadgeShow};..."
        Set<String> badgeKeys = activitiesBadges.keySet();
        Set<String> miniBadgeKeys = activitiesMiniBadges.keySet();
        badgeKeys.addAll(miniBadgeKeys);
        StringBuilder stringBuilder = new StringBuilder();
        for (String badgeKey : badgeKeys) {
            int badge = activitiesBadges.containsKey(badgeKey) ? activitiesBadges.get(badgeKey) : 0;
            boolean miniBadge = activitiesMiniBadges.containsKey(badgeKey) ? activitiesMiniBadges.get(badgeKey) : false;
            String infoString = String.format("%s,%s,%s;",badgeKey,String.valueOf(badge),String.valueOf(miniBadge));
            stringBuilder.append(infoString);
        }
        UserSetting.getUserSettingPreferences().edit().putString(UserSetting.generateUserSettingKey(ACTIVITIES_BADGES_MAP),stringBuilder.toString()).commit();
    }

    public void setActivityMiniBadgeShow(String id) {
        setActivityMiniBadgeShow(id,true);
    }

    private void setActivityMiniBadgeShow(String id,boolean storeImmediatly) {
        activitiesMiniBadges.put(id,true);
        if(storeImmediatly){
            storeActivitiesBadges();
        }
    }

    public void setActivityMiniBadgeHidden(String id){
        setActivityMiniBadgeHidden(id,true);
    }

    private void setActivityMiniBadgeHidden(String id,boolean storeImmediatly) {
        activitiesMiniBadges.put(id,false);
        if(storeImmediatly){
            storeActivitiesBadges();
        }
    }

    public void addActivityBadge(String id, int badge){
        addActivityBadge(id,badge,true);
    }

    private void addActivityBadge(String id, int badge,boolean storeImmediatly) {
        if(activitiesBadges.containsKey(id)){
            activitiesBadges.put(id,badge + activitiesBadges.get(id));
        }else {
            activitiesBadges.put(id,badge);
        }
        if(storeImmediatly){
            storeActivitiesBadges();
        }
    }

    public void clearActivityBadge(String id){
        clearActivityBadge(id,true);
    }

    private void clearActivityBadge(String id,boolean storeImmediatly){
        activitiesBadges.put(id,0);
        if(storeImmediatly){
            storeActivitiesBadges();
        }
    }

    public int getEnabledActivityBadge(String id) {
        Integer badgeValue = activitiesBadges.get(id);
        if(badgeValue != null){
            return badgeValue;
        }
        return 0;
    }

    public boolean isAcitityShowLittleBadge(String id){
        Boolean showMiniBadge = activitiesMiniBadges.get(id);
        if(showMiniBadge != null){
            return showMiniBadge;
        }
        return false;
    }

    public boolean isActivityEnabled(String id) {
        return activitiesMiniBadges.get(id);
    }
}
