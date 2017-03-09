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
    public Map<String, Object> conversationExtraInfo;
    public boolean snsPreviewEnabled = false;
    public boolean showAccountId = false;
    public boolean closeAfterConversationOpened = true;

    @Override
    public String getRightButtonTitle(UserProfileView sender, VessageUser profile) {
        if (StringHelper.isStringNullOrWhiteSpace(operateTitle)) {
            return LocalizedStringHelper.getLocalizedString(R.string.chat);
        }
        return operateTitle;
    }

    @Override
    public void onClickButtonRight(UserProfileView sender, VessageUser profile) {
        if (closeAfterConversationOpened) {
            sender.close();
        }
        ConversationViewActivity.openConversation(sender.getContext(), profile.userId, conversationExtraInfo);
    }

    @Override
    public boolean showAccountId(UserProfileView sender, VessageUser profile) {
        return showAccountId;
    }

    @Override
    public boolean snsPreviewEnabled(UserProfileView sender, VessageUser profile) {
        return snsPreviewEnabled;
    }
}
