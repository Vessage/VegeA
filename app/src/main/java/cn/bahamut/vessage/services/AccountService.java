package cn.bahamut.vessage.services;

import org.json.JSONObject;

import cn.bahamut.restfulkit.BahamutRFKit;
import cn.bahamut.restfulkit.client.APIClient;
import cn.bahamut.restfulkit.client.AccountClient;
import cn.bahamut.restfulkit.client.FireClient;
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
import cn.bahamut.vessage.main.BahamutConfig;
import cn.bahamut.vessage.main.UserSetting;
import cn.bahamut.vessage.main.VessageConfig;
import cn.bahamut.vessage.restfulapi.user.RegistNewVessageUserRequest;

/**
 * Created by alexchow on 16/3/30.
 */
public class AccountService implements OnServiceInit,OnServiceUserLogout{

    @Override
    public void onServiceInit() {
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
        req.setPassword(password);
        BahamutRFKit.getClient(AccountClient.class).signIn(req, new AccountClient.SignInCallback() {
            @Override
            public void onSignIn(LoginResult result, MessageResult errorMessage) {
                if (result != null) {
                    UserSetting.setLastUserLoginedAccount(result.AccountID);
                    validateLoginResult(result, callback);
                } else {
                    callback.onSignError(errorMessage.msg);
                }
            }
        });
    }

    public void signUp(String username, final String password, final SignCompletedCallback callback){
        RegistNewAccountRequest req = new RegistNewAccountRequest();
        req.setAccountName(username);
        req.setPassword(password);
        BahamutRFKit.getClient(AccountClient.class).signUp(req, new AccountClient.SignUpCallback() {
            @Override
            public void onSignUp(RegistResult result, MessageResult errorMessage) {
                if (result != null && result.suc) {
                    UserSetting.setLastUserLoginedAccount(result.accountId);
                    signIn(result.accountId, password, callback);
                } else {
                    callback.onSignError(errorMessage.msg);
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
        request.setAppkey(BahamutConfig.getAppkey());
        BahamutRFKit.getClient(AccountClient.class).changePassword(request, callback);
    }

    private void validateLoginResult(final LoginResult loginResult, final SignCompletedCallback callback){
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setTokenApi(loginResult.AppServiceUrl + "/Tokens");
        request.setAccountId(loginResult.AccountID);
        request.setAccessToken(loginResult.AccessToken);
        request.setAppkey(BahamutConfig.getAppkey());
        BahamutRFKit.getClient(AccountClient.class).validateAccessToken(request, new AccountClient.ValidateAccessTokenCallback() {
            @Override
            public void validateAccessTokenCallback(ValidateResult validateResult, MessageResult errorMessage) {
                if (validateResult != null) {
                    if (validateResult.isNotRegistAccount()) {
                        registNewVessageUser(loginResult, validateResult, callback);
                    } else {
                        UserSetting.setUserId(validateResult.UserId);
                        useValidateResult(validateResult);
                        callback.onSignCompleted(validateResult);
                    }
                } else {
                    callback.onSignError(errorMessage.msg);
                }
            }
        });
    }

    private void registNewVessageUser(LoginResult loginResult, final ValidateResult validateResult, final SignCompletedCallback callback){
        RegistNewVessageUserRequest request = new RegistNewVessageUserRequest();
        request.setAccessToken(loginResult.AccessToken);
        request.setAccountId(loginResult.AccountID);
        request.setNickName(loginResult.AccountName);
        request.setRegistNewUserApiServerUrl(validateResult.RegistAPIServer);
        request.setAppkey(BahamutConfig.getAppkey());
        request.setRegion(BahamutConfig.getRegion());
        request.setMotto("Using Vege");
        BahamutRFKit.getClient(AccountClient.class).executeRequest(request, new OnRequestCompleted<JSONObject>() {
            @Override
            public void callback(Boolean isOk, int statusCode, JSONObject result) {
                if (isOk) {
                    ValidateResult registedValidationResult = new ValidateResult();
                    registedValidationResult.setFieldValuesByJson(result);
                    UserSetting.setUserId(validateResult.UserId);
                    useValidateResult(validateResult);
                    callback.onSignCompleted(registedValidationResult);
                } else {
                    callback.onSignError("REGIST_ERROR");
                }
            }
        });
    }

    public void useValidateResult(ValidateResult validateResult){
        useAPIClient(validateResult);
        useFireClient(validateResult);
    }

    private void useAccountClient(){
        AccountClient client = new AccountClient();
        BahamutRFKit.instance.useClient(client);
    }

    private void useFireClient(ValidateResult validateResult) {
        FireClient.FireClientInfo fireClientInfo = new FireClient.FireClientInfo();
        fireClientInfo.appKey = BahamutConfig.getAppkey();
        fireClientInfo.appToken = validateResult.AppToken;
        fireClientInfo.fileAPIServer = validateResult.FileAPIServer;
        fireClientInfo.userId = validateResult.UserId;
        FireClient fireClient = new FireClient();
        fireClient.setClientInfo(fireClientInfo);
        BahamutRFKit.instance.useClient(fireClient);
    }

    private void useAPIClient(ValidateResult validateResult) {
        APIClient.APIClientInfo apiClientInfo = new APIClient.APIClientInfo();
        apiClientInfo.apiServer = validateResult.APIServer;
        apiClientInfo.appToken = validateResult.AppToken;
        apiClientInfo.userId = validateResult.UserId;
        APIClient apiClient = new APIClient();
        apiClient.setClientInfo(apiClientInfo);
        BahamutRFKit.instance.useClient(apiClient);
    }

}
