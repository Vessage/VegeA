package cn.bahamut.restfulkit.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexchow on 16/4/5.
 */
public class RegistResult {
    private Boolean suc = false;

    //regist info
    private String accountId;
    private String accountName;
    private String msg;

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
