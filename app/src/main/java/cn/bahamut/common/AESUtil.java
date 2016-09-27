package cn.bahamut.common;

import com.scottyab.aescrypt.AESCrypt;

/**
 * Created by alexchow on 16/9/19.
 */
public class AESUtil {

    public static String encrypt(String seed, String cleartext)
            throws Exception {
        return AESCrypt.encrypt(seed,cleartext);
    }

    public static String decrypt(String seed, String encrypted)
            throws Exception {
        return AESCrypt.decrypt(seed,encrypted);
    }

}
