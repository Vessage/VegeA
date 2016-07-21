package cn.bahamut.vessage.main;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 16/7/19.
 */
public class AssetsDefaultConstants {

    static public int getDefaultFace(int code){
        return DEFAULT_AVATARS[Math.abs(code) % DEFAULT_AVATARS.length];
    }

    static public final int[] DEFAULT_AVATARS = new int[]{
            R.raw.default_avatar_0,
            R.raw.default_avatar_1,
            R.raw.default_avatar_2,
            R.raw.default_avatar_3,
            R.raw.default_avatar_4,
            R.raw.default_avatar_5,
            R.raw.default_avatar_6
    };

    static public final int DEFAULT_FACE = R.raw.default_face;

    static public final int[] DEFAULT_CONVERSATION_BCGS = new int[]{
            R.raw.conversation_bcg_0,
            R.raw.conversation_bcg_1,
            R.raw.conversation_bcg_2,
            R.raw.conversation_bcg_3,
            R.raw.conversation_bcg_4,
    };

    public static int randomConversationBackground() {
        return DEFAULT_CONVERSATION_BCGS[Math.abs((int)(Math.random() * 100)) % DEFAULT_CONVERSATION_BCGS.length];
    }
}
