package cn.bahamut.vessage.activities.tim;

import android.graphics.Color;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 2017/1/7.
 */

public class TIMConstants {

    static public class TIMStyle {
        public int backgroundResId;
        public String colorString;

        public TIMStyle(int backgroundResId, String colorString) {
            this.backgroundResId = backgroundResId;
            this.colorString = colorString;
        }

        public int getColor() {
            return Color.parseColor(this.colorString);
        }
    }

    static public String selectedStyleIndexKey = "TIMselectedStyleKey";
    static public String cachedFontSizeKey = "TIMFontSizeKey";
    static public String cachedFontKey = "TIMFontKey";

    static public float minFontSize = 18f;
    static public float maxFontSize = 72f;

    static private TIMStyle[] TIMDefaultStyles = new TIMStyle[]{
            new TIMStyle(R.drawable.tim_bcg_0, "#8D75FF"),
            new TIMStyle(R.drawable.tim_bcg_1, "#DE67FF"),
            new TIMStyle(R.drawable.tim_bcg_2, "#4C4C4C"),
            new TIMStyle(R.drawable.tim_bcg_3, "#ffffff"),
            new TIMStyle(R.drawable.tim_bcg_4, "#02C1FA"),
            new TIMStyle(R.drawable.tim_bcg_5, "#CACACA"),
            new TIMStyle(R.drawable.tim_bcg_6, "#ffffff"),
            new TIMStyle(R.drawable.tim_bcg_7, "#ffffff"),
            new TIMStyle(R.drawable.tim_bcg_8, "#000000"),
    };

    static public int getStyleCount() {
        return TIMDefaultStyles.length;
    }

    static public TIMStyle getStyleOfIndex(int index) {
        if (index >= TIMDefaultStyles.length || index < 0) {
            return TIMDefaultStyles[0];
        }
        return TIMDefaultStyles[index];
    }
}
