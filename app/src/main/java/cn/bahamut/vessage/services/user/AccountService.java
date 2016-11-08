package cn.bahamut.vessage.services.user;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bahamut.common.JsonHelper;
import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.AccountClient;
import cn.bahamut.restfulkit.client.base.OnRequestCompleted;
import cn.bahamut.restfulkit.models.LoginResult;
import cn.bahamut.restfulkit.models.MessageResult;
import cn.bahamut.restfulkit.models.RegistResult;
import cn.bahamut.restfulkit.models.ValidateResult;
import cn.bahamut.restfulkit.request.account.ChangePasswordRequest;
import cn.bahamut.restfulkit.request.account.LoginBahamutAccountRequest;
import cn.bahamut.restfulkit.request.account.RegistNewAccountRequest;
import cn.bahamut.restfulkit.request.account.ValidateTokenRequest;
import cn.bahamut.service.OnServiceInit;
import cn.bahamut.service.OnServiceUserLogout;
import cn.bahamut.service.ServicesProvider;
import cn.bahamut.vessage.main.AppMain;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.main.VessageConfig;
import cn.bahamut.vessage.restfulapi.user.RegistNewVessageUserRequest;

/**
 * Created by alexchow on 16/3/30.
 */
public class AccountService implements OnServiceInit,OnServiceUserLogout{

    @Override
    public void onServiceInit(Context context) {
        useAccountClient();
        ServicesProvider.setServiceReady(AccountService.class);
    }

    @Override
    public void onUserLogout() {
        BahamutRFKit.instance.resetKit();
        useAccountClient();
    }

    public static interface SignCompletedCallback{
        void onSignCompleted(ValidateResult result);
        void onSignError(String errorMessage);
    }

    public void signIn(String loginString,String password, final SignCompletedCallback callback){
        LoginBahamutAccountRequest req = new LoginBahamutAccountRequest();
        req.setLoginApi(loginString);
        req.setAccountString(loginString);
        req.setPassword(password);
        req.setAppkey(VessageConfig.getAppkey());
        req.setLoginApi(VessageConfig.getBahamutConfig().getAccountLoginApiUrl());
        BahamutRFKit.getClient(AccountClient.class).signIn(req, new AccountClient.SignInCallback() {
            @Override
            public void onSignIn(LoginResult result, MessageResult errorMessage) {
                if (result != null) {
                    UserSetting.setLastUserLoginedAccount(result.getAccountID());
                    validateLoginResult(result, callback);
                } else {
                    callback.onSignError(errorMessage.getMsg());
                }
            }
        });
    }

    public void signUp(String username, final String password, final SignCompletedCallback callback){
        RegistNewAccountRequest req = new RegistNewAccountRequest();
        req.setAccountName(username);
        req.setPassword(password);
        req.setAppkey(VessageConfig.getAppkey());
        req.setRegistApi(VessageConfig.getBahamutConfig().getAccountRegistApiUrl());
        BahamutRFKit.getClient(AccountClient.class).signUp(req, new AccountClient.SignUpCallback() {
            @Override
            public void onSignUp(RegistResult result, MessageResult errorMessage) {
                if (result != null && result.getSuc()) {
                    UserSetting.setLastUserLoginedAccount(result.getAccountId());
                    signIn(result.getAccountId(), password, callback);
                } else {
                    callback.onSignError(errorMessage.getMsg());
                }
            }
        });
    }

    public void changePassword(String originPassword, String newPassword,AccountClient.ChangePasswordCallback callback){
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setAccountId(UserSetting.getLastUserLoginedAccount());
        request.setOriginPassword(originPassword);
        request.setNewPassword(newPassword);
        request.setUserId(UserSetting.getUserId());
        request.setAppkey(VessageConfig.getAppkey());
        request.setAppToken(UserSetting.getUserValidateResult().getAppToken());
        request.setTokenApi(VessageConfig.getBahamutConfig().getAccountApiUrlPrefix() + "/Password");
        BahamutRFKit.getClient(AccountClient.class).changePassword(request, callback);
    }

    private void validateLoginResult(final LoginResult loginResult, final SignCompletedCallback callback){
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setTokenApi(loginResult.getAppServiceUrl() + "/Tokens");
        request.setAccountId(loginResult.getAccountID());
        request.setAccessToken(loginResult.getAccessToken());
        request.setAppkey(VessageConfig.getAppkey());
        BahamutRFKit.getClient(AccountClient.class).validateAccessToken(request, new AccountClient.ValidateAccessTokenCallback() {
            @Override
            public void validateAccessTokenCallback(ValidateResult validateResult, MessageResult errorMessage) {
                if (validateResult != null) {
                    if (validateResult.isNotRegistAccount()) {
                        registNewVessageUser(loginResult, validateResult, callback);
                    } else {
                        AppMain.getInstance().useValidateResult(validateResult);
                        callback.onSignCompleted(validateResult);
                    }
                } else if(errorMessage != null){
                    callback.onSignError(errorMessage.getMsg());
                }else {
                    callback.onSignError("UNKNOW_ERROR");
                }
            }
        });
    }

    private void registNewVessageUser(LoginResult loginResult, final ValidateResult validateResult, final SignCompletedCallback callback){
        RegistNewVessageUserRequest request = new RegistNewVessageUserRequest();
        request.setAccessToken(loginResult.getAccessToken());
        request.setAccountId(loginResult.getAccountID());
        request.setNickName(loginResult.getAccountName());
        request.setRegistNewUserApiServerUrl(validateResult.getRegistAPIServer());
        request.setAppkey(VessageConfig.getAppkey());
        request.setRegion(VessageConfig.getRegion());
        request.setMotto("Using Vege");
        BahamutRFKit.getClient(AccountClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    try {
                        ValidateResult registedValidationResult = JsonHelper.parseObject(result, ValidateResult.class);
                        AppMain.getInstance().useValidateResult(registedValidationResult);
                        callback.onSignCompleted(registedValidationResult);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onSignError("REGIST_ERROR");
                    }

                } else {
                    callback.onSignError("REGIST_ERROR");
                }
            }
        });
    }

    private void useAccountClient(){
        AccountClient client = new AccountClient();
        BahamutRFKit.instance.useClient(client);
    }

}
