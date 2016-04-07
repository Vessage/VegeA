package cn.bahamut.vessage.services;

import cn.bahamut.service.OnServiceUserLogin;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;

/**
 * Created by alexchow on 16/3/30.
 */
public class VessageService implements OnServiceUserLogin,OnServiceUserLogout {
    @Override
    public void onUserLogin(String userId) {
        ServicesProvider.setServiceReady(VessageService.class);
    }

    @Override
    public void onUserLogout() {
        ServicesProvider.setServiceNotReady(ConversationService.class);
    }
}
