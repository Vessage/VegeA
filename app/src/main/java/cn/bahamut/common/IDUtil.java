package cn.bahamut.common;

import java.util.UUID;

/**
 * Created by alexchow on 16/4/1.
 */
public class IDUtil {
    static String generateUniqueId(){
        return UUID.randomUUID().toString();
    }
}
