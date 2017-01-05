package cn.bahamut.vessage.userprofile;

import cn.bahamut.vessage.R;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2017/1/4.
 */

abstract public class NoteUserNameDelegate implements UserProfileViewDelegate {
    @Override
    public String getRightButtonTitle(UserProfileView sender, VessageUser profile) {
        return LocalizedStringHelper.getLocalizedString(R.string.note_conversation);
    }

    @Override
    public boolean showAccountId(UserProfileView sender, VessageUser profile) {
        return true;
    }
}
