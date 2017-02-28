package cn.bahamut.vessage.main;

import java.util.LinkedList;
import java.util.List;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.services.activities.ExtraActivityInfo;

/**
 * Created by alexchow on 2017/2/28.
 */

public class VGCoreConstants {
    public static final String NEAR_ACTIVE_ACTIVITY_ID = "100";
    public static final String GROUP_CHAT_ACTIVITY_ID = "101";

    public static final List<ExtraActivityInfo> VGCoreActivity = loadCoreActivity();

    private static List<ExtraActivityInfo> loadCoreActivity() {
        List<ExtraActivityInfo> list = new LinkedList<>();

        ExtraActivityInfo ac = null;

        ac = new ExtraActivityInfo();
        ac.title = LocalizedStringHelper.getLocalizedString(R.string.near_active_user_ac_title);
        ac.activityId = VGCoreConstants.NEAR_ACTIVE_ACTIVITY_ID;
        list.add(ac);

        ac = new ExtraActivityInfo();
        ac.title = LocalizedStringHelper.getLocalizedString(R.string.group_chat_user_ac_title);
        ac.activityId = VGCoreConstants.GROUP_CHAT_ACTIVITY_ID;
        list.add(ac);

        return list;
    }
}
