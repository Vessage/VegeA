package cn.bahamut.vessage.main;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import cn.bahamut.vessage.R;

/**
 * Created by alexchow on 2016/12/23.
 */
public class SoundManager {
    private static SoundManager instance = new SoundManager();
    private SoundPool soundPool;

    public static void init(Context context) {
        instance.loadSounds(context);
    }

    public static SoundManager getInstance() {
        return instance;
    }

    private void loadSounds(Context context) {
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(context, R.raw.new_msg, 1);
    }

    public void playNewMessageRington() {
        if (soundPool != null) {
            soundPool.play(1, 1, 1, 0, 0, 1);
        }
    }
}
