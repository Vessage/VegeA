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
                if (isOk) {

                    try {
                        LoginResult loginResult = JsonHelper.parseObject(result,LoginResult.class);
                        callback.onSignIn(loginResult, null);
                    } catch (JSONException e) {

                        MessageResult messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                        callback.onSignIn(null, messageResult);
                    }
                } else {
                    try {
                        MessageResult messageResult = JsonHelper.parseObject(result,MessageResult.class);
                        callback.onSignIn(null,messageResult);
                    } catch (JSONException e) {
                        MessageResult messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                        callback.onSignIn(null, messageResult);
                    }
                }
            }
        });
    }

    public void signUp(RegistNewAccountRequest req, final SignUpCallback callback){
        executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                try{
                    RegistResult registResult = JsonHelper.parseObject(result,RegistResult.class);
                }catch (Exception e){
                    MessageResult messageResult = null;
                    try {
                        messageResult = JsonHelper.parseObject(result,MessageResult.class);
                    } catch (JSONException e1) {
                        messageResult = new MessageResult();
                        messageResult.setMsg("DATA_ERROR");
                    }
                    callback.onSignUp(null, messageResult);
                }

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
                        messageResult.setMsg("DATA_ERROR");
                        callback.validateAccessTokenCallback(null, messageResult);
                    }
                }else{
                    try {
                        MessageResult messageResult = JsonHelper.parseObject(result,MessageResult.class);
                    } catch (JSONException e) {
                        MessageResult messageResult = new MessageResult();
                        messageResult.setMsg("NETWORK_ERROR");
                        callback.validateAccessTokenCallback(null, messageResult);
                    }
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