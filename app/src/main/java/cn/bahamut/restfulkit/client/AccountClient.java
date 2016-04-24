package cn.bahamut.restfulkit.client;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.restfulkit.client.base.BahamutClientBase;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.restfulkit.models.BahamutClientInfo;
import cn.bahamut.restfulkit.models.LoginResult;
import cn.bahamut.restfulkit.models.MessageResult;
import cn.bahamut.restfulkit.models.RegistResult;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.restfulkit.request.BahamutRequestBase;
import cn.bahamut.restfulkit.request.account.ChangePasswordRequest;
import cn.bahamut.restfulkit.request.account.LoginBahamutAccountRequest;
import cn.bahamut.restfulkit.request.account.RegistNewAccountRequest;
import cn.bahamut.restfulkit.request.account.ValidateTokenRequest;

/**
 * Created by alexchow on 16/4/5.
 */
public class AccountClient extends BahamutClientBase {

    public interface SignInCallback{
        void onSignIn(LoginResult result, MessageResult errorMessage);
    }

    public interface SignUpCallback{
        void onSignUp(RegistResult result, MessageResult errorMessage);
    }

    public interface ChangePasswordCallback{
        void onChangePassword(boolean isDone, MessageResult errorMessage);
    }

    public interface ValidateAccessTokenCallback{
        void validateAccessTokenCallback(ValidateResult validateResult,MessageResult errorMessage);
    }

    public void signIn(LoginBahamutAccountRequest req, final SignInCallback callback){
        executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                LoginResult loginResult = null;
                MessageResult messageResult = null;
                if (isOk) {
                    try {
                        loginResult = JsonHelper.parseObject(result,LoginResult.class);
                    } catch (JSONException e) {
                        messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                    }
                } else {
                    try {
                        messageResult = JsonHelper.parseObject(result,MessageResult.class);
                        callback.onSignIn(null,messageResult);
                    } catch (JSONException e) {
                        messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                    }
                }
                callback.onSignIn(loginResult, messageResult);
            }
        });
    }

    public void signUp(RegistNewAccountRequest req, final SignUpCallback callback){
        executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                MessageResult messageResult = null;
                RegistResult registResult = null;
                try{
                    registResult = JsonHelper.parseObject(result,RegistResult.class);
                    if(!registResult.getSuc()){
                        messageResult = new MessageResult();
                        messageResult.setMsg(registResult.getMsg());
                    }
                }catch (Exception e){
                    try {
                        messageResult = JsonHelper.parseObject(result,MessageResult.class);
                    } catch (JSONException e1) {
                        messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                    }
                }
                callback.onSignUp(registResult, messageResult);
            }
        });
    }


    public void validateAccessToken(ValidateTokenRequest request, final ValidateAccessTokenCallback callback){
        executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    ValidateResult validateResult = null;
                    try {
                        validateResult = JsonHelper.parseObject(result, ValidateResult.class);
                        callback.validateAccessTokenCallback(validateResult, null);
                    } catch (JSONException e) {
                        MessageResult messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                        callback.validateAccessTokenCallback(null, messageResult);
                    }
                }else{
                    MessageResult messageResult = null;
                    try {
                        messageResult = JsonHelper.parseObject(result,MessageResult.class);
                    } catch (JSONException e) {
                        messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                    }
                    callback.validateAccessTokenCallback(null, messageResult);
                }
            }
        });
    }

    public void changePassword(ChangePasswordRequest request, final ChangePasswordCallback callback){
        executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if(isOk){
                    callback.onChangePassword(true, null);
                }else{
                    try {
                        MessageResult messageResult = JsonHelper.parseObject(result,MessageResult.class);
                        callback.onChangePassword(false,messageResult);
                    } catch (JSONException e) {
                        MessageResult messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                        callback.onChangePassword(false, messageResult);
                    }
                }
            }
        });
    }

    @Override
    protected void prepareRequest(BahamutRequestBase request, BahamutClientInfo clientInfo) {

    }
}
