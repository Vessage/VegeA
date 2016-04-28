package cn.bahamut.service;

import android.content.Context;

import java.util.HashMap;

import cn.bahamut.observer.Observable;
import cn.bahamut.observer.ObserverState;

/**
 * Created by alexchow on 16/3/30.
 */
public class ServicesProvider extends Observable {

    public static final String NOTIFY_ALL_SERVICES_READY = "NOTIFY_ALL_SERVICES_READY";
    public static final String NOTIFY_SERVICES_INITED = "NOTIFY_SERVICES_INITED";
    public static final String NOTIFY_INIT_SERVICE_FAILED = "NOTIFY_INIT_SERVICE_FAILED";

    public static final String NOTIFY_USER_WILL_LOGOIN = "NOTIFY_USER_WILL_LOGOIN";
    public static final String NOTIFY_USER_LOGOIN = "NOTIFY_USER_LOGOIN";
    public static final String NOTIFY_USER_LOGOUT = "NOTIFY_USER_LOGOUT";
    public static final ServicesProvider instance = new ServicesProvider();

    public static class ServiceNotReadyException extends Exception{

    }

    private ServicesProvider(){

    }

    static public void initServices(Context applicationContext){
        for (ServiceInfo serviceInfo : instance.servicesMap.values()) {
            if(serviceInfo.service instanceof OnServiceInit){
                ((OnServiceInit)serviceInfo.service).onServiceInit(applicationContext);
            }
        }
        ObserverState state = new ObserverState();
        state.setNotifyType(ServicesProvider.NOTIFY_SERVICES_INITED);
        instance.postNotification(state);
    }

    static public void userLogin(String userId) {
        instance.postNotification(NOTIFY_USER_WILL_LOGOIN);
        for (ServiceInfo serviceInfo : instance.servicesMap.values()) {
            if (serviceInfo.service instanceof OnServiceUserLogin) {
                ((OnServiceUserLogin) serviceInfo.service).onUserLogin(userId);
            }
        }
        instance.postNotification(NOTIFY_USER_LOGOIN);
    }

    static public void userLogout(){
        synchronized (instance){
            for (ServiceInfo serviceInfo : instance.servicesMap.values()) {
                if(serviceInfo.service instanceof OnServiceUserLogout){
                    ((OnServiceUserLogout)serviceInfo.service).onUserLogout();
                }
            }
            instance.postNotification(NOTIFY_USER_LOGOUT);
        }
    }

    private static class ServiceInfo{
        Object service;
        boolean ready = false;
    }
    private HashMap<Class,ServiceInfo> servicesMap = new HashMap<Class,ServiceInfo>();

    static public boolean registService(Object service){
        Class cls = service.getClass();
        if(instance.servicesMap.containsKey(cls)){
            return false;
        }
        ServiceInfo info = new ServiceInfo();
        info.service = service;
        instance.servicesMap.put(cls,info);
        return true;
    }

    static public<T> void postInitServiceFailed(Class<T> cls,String reason){
        ObserverState state = new ObserverState();
        state.setNotifyType(NOTIFY_INIT_SERVICE_FAILED);
        instance.postNotification(state);
    }

    static public boolean isAllServicesReady(){
        for (ServiceInfo serviceInfo : instance.servicesMap.values()) {
            if(!serviceInfo.ready){
                return false;
            }
        }
        return true;
    }

    static public<T> boolean setServiceReady(Class<T> cls){
        boolean result = false;
        ServiceInfo info = instance.servicesMap.get(cls);
        if (info != null){
            info.ready = true;
            result = true;
        }
        synchronized (instance){
            if(result && isAllServicesReady()){
                instance.postNotification(NOTIFY_ALL_SERVICES_READY);
            }
            return result;
        }
    }

    static public<T> boolean setServiceNotReady(Class<T> cls){
        ServiceInfo info = instance.servicesMap.get(cls);
        if (info != null){
            info.ready = false;
            return true;
        }
        return false;
    }

    static public<T> T getService(Class<T> cls){
        ServiceInfo info = instance.servicesMap.get(cls);
        try{
            T service =  (T)info.service;
            if(info.ready){
                return service;
            }
            throw new ServiceNotReadyException();
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}
