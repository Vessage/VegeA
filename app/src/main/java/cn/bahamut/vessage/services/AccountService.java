package cn.bahamut.vessage.services;

import cn.bahamut.vessage.models.ValidationResult;

/**
 * Created by alexchow on 16/3/30.
 */
public class AccountService {

    public static interface SignCompletedCallback{
        void SignInCallback(ValidationResult result);
    }

    public void signIn(String userInfo,String password,SignCompletedCallback callback){

    }

    public void signUp(String username,String password,SignCompletedCallback callback){

    }

    public void changePassword(String originPassword, String newPassword){

    }
}
