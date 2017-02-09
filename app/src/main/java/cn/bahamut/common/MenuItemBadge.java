package cn.bahamut.common;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/5/22.
 */
public class MenuItemBadge {
    public static MenuItem update(MenuItem item, int iconResId, boolean showDotBadge) {
        return update(item, iconResId, showDotBadge, R.drawable.red_dot);
    }

    public static MenuItem update(MenuItem item, int iconResId, boolean showDotBadge, int badgeDrawableResId) {
        View view = item.getActionView();
        view.findViewById(R.id.menu_badge_icon).setBackgroundResource(iconResId);
        ImageView dotBadge = (ImageView) view.findViewById(R.id.menu_badge);
        dotBadge.setImageResource(badgeDrawableResId);
        dotBadge.setVisibility(showDotBadge ? View.VISIBLE : View.INVISIBLE);
        return item;
    }
}
