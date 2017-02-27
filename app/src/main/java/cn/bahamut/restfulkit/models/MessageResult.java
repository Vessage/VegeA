package cn.bahamut.restfulkit.models;

import cn.bahamut.common.NotProguard;

/**
 * Created by alexchow on 16/4/5.
 */
@NotProguard
public class MessageResult {
    public String msg;

    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }

}
