package cn.bahamut.vessage.userprofile;

import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2017/1/4.
 */

public interface UserProfileViewDelegate {
    String getRightButtonTitle(UserProfileView sender, VessageUser profile);

    void onClickButtonRight(UserProfileView sender, VessageUser profile);

    boolean showAccountId(UserProfileView sender, VessageUser profile);

    boolean snsPreviewEnabled(UserProfileView sender, VessageUser profile);
}
