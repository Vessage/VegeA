package cn.bahamut.observer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by alexchow on 16/4/1.
 */
public class Observable extends java.util.Observable implements java.util.Observer{

    private HashMap<String,List<Observer>> observers = new HashMap<String,List<Observer>>();
    public Observable(){
        super.addObserver(this);
    }

    private List<Observer> getObserverList(String notifyType){
        List<Observer> list = observers.get(notifyType);
        if(list == null){
            list = new LinkedList<Observer>();
            observers.put(notifyType,list);
        }
        return list;
    }

    public void addObserver(String notifyType, Observer observer){
        List<Observer> list = getObserverList(notifyType);
        if(list.contains(observer) == false){
            list.add(observer);
        }
    }

    public void deleteObserver(String notifyType, Observer observer){
        List<Observer> list = getObserverList(notifyType);
        list.remove(observer);
    }

    public void notify(ObserverState state){
        super.notifyObservers(state);
    }

    @Deprecated
    @Override
    final public void addObserver(java.util.Observer observer) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void notifyObservers() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void notifyObservers(Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    final public void update(java.util.Observable observable, Object data) {
        ObserverState state = (ObserverState)data;
        if(state != null && state.getNotifyType() != null){
            List<Observer> list = observers.get(state.getNotifyType());
            if(list != null){
                for (Observer observer : list) {
                    observer.update(state);
                }
            }
        }
    }
}
