package cn.bahamut.restfulkit.models;

import cn.bahamut.common.NotProguard;

/**
 * Created by alexchow on 16/4/5.
 */
@NotProguard
public class RegistResult {
    public Boolean suc = false;

    //regist info
    public String accountId;
    public String accountName;
    public String msg;

    public Boolean getSuc() {
        return suc;
    }

    public void setSuc(Boolean suc) {
        this.suc = suc;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
