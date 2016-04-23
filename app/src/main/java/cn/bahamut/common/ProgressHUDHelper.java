package cn.bahamut.common;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;

import com.kaopiz.kprogresshud.KProgressHUD;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/4/23.
 */
public class ProgressHUDHelper {
    public static KProgressHUD showHud(Context context,String text, int customImageId, boolean autoDismiss) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(customImageId);
        final KProgressHUD hud = KProgressHUD.create(context)
                .setLabel(text)
                .setCustomView(imageView)
                .setAutoDismiss(true);
        hud.show();
        if(autoDismiss){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hud.dismiss();
                }
            },1000);
        }
        return hud;
    }
}
