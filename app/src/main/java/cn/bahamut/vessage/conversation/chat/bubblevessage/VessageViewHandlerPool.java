package cn.bahamut.vessage.conversation.chat.bubblevessage;

import android.app.Activity;
import android.util.Log;

import java.util.LinkedList;

import cn.bahamut.vessage.services.vessage.Vessage;

/**
 * Created by alexchow on 2017/2/24.
 */

public class VessageViewHandlerPool<T extends BubbleVessageHandler> {

    private static final String TAG = "VessageViewHandlerPool";
    private LinkedList<T> pool = new LinkedList<>();
    private LinkedList<T> usedHandlers = new LinkedList<>();

    public void registHandler(Activity context, T handler, boolean used) {
        if (used) {
            usedHandlers.add(0, handler);
        } else {
            pool.add(0, handler);
        }
        Log.d(TAG, "Regist Handler:" + handler.hashCode());
    }

    public void registHandler(Activity context, T handler) {
        registHandler(context, handler, true);
    }

    public T getHandler(Activity context, Vessage vessage) {

        if (pool.size() > 0) {
            T handler = pool.removeFirst();
            usedHandlers.add(0, handler);
            Log.d(TAG, "Get Handler:" + handler.hashCode());
            return handler;
        }
        Log.d(TAG, "Null Handler");
        return null;
    }

    public void recycleHandler(Activity context, T handler) {
        usedHandlers.remove(handler);
        pool.add(0, handler);
        Log.d(TAG, "Recycle Handler:" + handler.hashCode());
    }
}