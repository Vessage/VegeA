package cn.bahamut.common;

import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/5/22.
 */
public class MenuItemBadge {
    public static MenuItem update(MenuItem item, int iconResId, boolean showDotBadge) {
        View view = item.getActionView();
        view.findViewById(R.id.menu_badge_icon).setBackgroundResource(iconResId);
        View dotBadge = view.findViewById(R.id.menu_badge);
        dotBadge.setVisibility(showDotBadge ? View.VISIBLE : View.INVISIBLE);
        return item;
    }
}
