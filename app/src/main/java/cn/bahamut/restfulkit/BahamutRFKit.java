package cn.bahamut.restfulkit;

import java.util.HashMap;

import cn.bahamut.restfulkit.client.BahamutClient;
import cn.bahamut.restfulkit.client.SetClient;
import cn.bahamut.restfulkit.models.LoginResult;
import cn.bahamut.vessage.models.ValidationResult;

/**
 * Created by alexchow on 16/4/2.
 */
public class BahamutRFKit {

    static public final BahamutRFKit instance = new BahamutRFKit();

    private String appKey;
    private String version;

    private LoginResult loginInfo;
    private ValidationResult validationInfo;

    public HashMap<Class<BahamutClient>,BahamutClient> clients;

    public void resetKit(String appKey,String version){
        this.appKey = appKey;
        this.version = version;
        clients = new HashMap<>();
    }

    public void useClient(BahamutClient client){
        if(client instanceof SetClient){
            ((SetClient) client).setClient(loginInfo);
            ((SetClient) client).setClient(validationInfo);
        }
        clients.put((Class<BahamutClient>) client.getClass(),client);
    }

    public void reuse(LoginResult loginResult,ValidationResult validationResult){
        this.loginInfo = loginResult;
        this.validationInfo = validationResult;
    }

    public static BahamutClient getClient(Class<BahamutClient> cls){
        return instance.clients.get(cls);
    }
}