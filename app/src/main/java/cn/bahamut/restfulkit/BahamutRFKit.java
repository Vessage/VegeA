package cn.bahamut.restfulkit;

import java.util.HashMap;

import cn.bahamut.restfulkit.client.base.BahamutClient;
import cn.bahamut.restfulkit.client.base.BahamutClientLifeProcess;

/**
 * Created by alexchow on 16/4/2.
 */
public class BahamutRFKit {

    static public final BahamutRFKit instance = new BahamutRFKit();

    public HashMap<Class<BahamutClient>,BahamutClient> clients = new HashMap<>();

    public void resetKit(){
        for (BahamutClient bahamutClient : clients.values()) {
            if(bahamutClient instanceof BahamutClientLifeProcess){
                ((BahamutClientLifeProcess) bahamutClient).closeClient();
            }
        }
        clients.clear();
    }

    public void useClient(BahamutClient client){
        if(client instanceof BahamutClientLifeProcess){
            ((BahamutClientLifeProcess) client).startClient();
        }
        clients.put((Class<BahamutClient>) client.getClass(),client);
    }

    static public<T extends BahamutClient> T getClient(Class<T> cls){
        return (T)instance.clients.get(cls);
    }
}