package cn.bahamut.vessage.userprofile;

import java.util.Map;

import cn.bahamut.common.StringHelper;
import cn.bahamut.vessage.R;
import cn.bahamut.vessage.conversation.chat.ConversationViewActivity;
import cn.bahamut.vessage.main.LocalizedStringHelper;
import cn.bahamut.vessage.services.user.VessageUser;

/**
 * Created by alexchow on 2017/1/4.
 */

public class OpenConversationDelegate implements UserProfileViewDelegate {

    public String operateTitle;
    public boolean showAccountId = true;
    public Map<String, Object> conversationExtraInfo;

    @Override
    public String getRightButtonTitle(UserProfileView sender, VessageUser profile) {
        if (StringHelper.isStringNullOrWhiteSpace(operateTitle)) {
            return LocalizedStringHelper.getLocalizedString(R.string.chat);
        }
        return operateTitle;
    }

    @Override
    public void onClickButtonRight(UserProfileView sender, VessageUser profile) {
        ConversationViewActivity.openConversation(sender.getContext(), profile.userId, conversationExtraInfo);
    }

    @Override
    public boolean showAccountId(UserProfileView sender, VessageUser profile) {
        return showAccountId;
    }

    @Override
    public boolean snsPreviewEnabled(UserProfileView sender, VessageUser profile) {
        return showAccountId;
    }
}
