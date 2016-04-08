package cn.bahamut.observer;

/**
 * Created by alexchow on 16/4/1.
 */
public class ObserverState {
    private String notifyType;
    private Object info;
    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }
}
