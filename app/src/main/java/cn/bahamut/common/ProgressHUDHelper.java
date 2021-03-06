package cn.bahamut.common;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by alexchow on 16/4/23.
 */
public class ProgressHUDHelper {
    public static KProgressHUD showSpinHUD(Activity context) {
        KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .show();
        return hud;
    }

    public interface OnDismiss {
        void onHudDismiss();
    }

    public static KProgressHUD showHud(Context context,int msgResId, int customImageId, boolean autoDismiss) {
        return showHud(context, context.getResources().getString(msgResId), customImageId, autoDismiss, null);
    }

    public static KProgressHUD showHud(Context context,int msgResId, int customImageId, boolean autoDismiss,OnDismiss onDismiss) {
        return showHud(context, context.getResources().getString(msgResId), customImageId, autoDismiss, onDismiss);
    }

    public static KProgressHUD showHud(Context context, String msg, int customImageId, boolean autoDismiss) {
        return showHud(context, msg, customImageId, autoDismiss, null);
    }

    public static KProgressHUD showHud(Context context, String msg, int customImageId, boolean autoDismiss, final OnDismiss onDismiss) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(customImageId);
        int size = DensityUtil.dip2px(context, 64);
        final KProgressHUD hud = KProgressHUD.create(context)
                .setLabel(msg)
                .setCustomView(imageView)
                .setSize(size, size)
                .setAutoDismiss(true);
        hud.show();
        if(autoDismiss){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hud.dismiss();
                    if(onDismiss != null){
                        onDismiss.onHudDismiss();
                    }
                }
            },1000);
        }
        return hud;
    }
}
