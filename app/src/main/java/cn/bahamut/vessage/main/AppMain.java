package cn.bahamut.vessage.main;

import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.services.AccountService;
import cn.bahamut.vessage.services.ConversationService;
import cn.bahamut.vessage.services.UserService;
import cn.bahamut.vessage.services.VessageService;

/**
 * Created by alexchow on 16/4/1.
 */
public class AppMain {
    static public final AppMain instance = new AppMain();
    public boolean start(){
        configureServices();
        return true;
    }

    private void configureServices() {
        ServicesProvider.registService(new AccountService());
        ServicesProvider.registService(new UserService());
        ServicesProvider.registService(new ConversationService());
        ServicesProvider.registService(new VessageService());
        ServicesProvider.initServices();
    }
}
