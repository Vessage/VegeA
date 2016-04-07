package cn.bahamut.restfulkit.client;

import org.json.JSONObject;

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
                    LoginResult loginResult = new LoginResult();
                    loginResult.setFieldValuesByJson(result);
                    callback.onSignIn(loginResult, null);
                } else {
                    MessageResult messageResult = new MessageResult();
                    messageResult.setFieldValuesByJson(result);
                    callback.onSignIn(null, messageResult);
                }
            }
        });
    }

    public void signUp(RegistNewAccountRequest req, final SignUpCallback callback){
        executeRequest(req, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    RegistResult registResult = new RegistResult();
                    registResult.setFieldValuesByJson(result);
                    callback.onSignUp(registResult, null);
                } else {
                    MessageResult messageResult = new MessageResult();
                    messageResult.setFieldValuesByJson(result);
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
                    ValidateResult validateResult = new ValidateResult();
                    validateResult.setFieldValuesByJson(result);
                    callback.validateAccessTokenCallback(validateResult, null);
                }else{
                    MessageResult messageResult = new MessageResult();
                    messageResult.setFieldValuesByJson(result);
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
                    MessageResult messageResult = new MessageResult();
                    messageResult.setFieldValuesByJson(result);
                    callback.onChangePassword(false, messageResult);
                }
            }
        });
    }

    @Override
    protected void prepareRequest(BahamutRequestBase request, BahamutClientInfo clientInfo) {

    }
}
