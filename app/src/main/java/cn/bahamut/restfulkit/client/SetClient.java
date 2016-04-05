package cn.bahamut.restfulkit.client;

import cn.bahamut.restfulkit.models.LoginResult;
import cn.bahamut.vessage.models.ValidationResult;

/**
 * Created by alexchow on 16/4/5.
 */
public interface SetClient {
    void setClient(LoginResult loginResult);
    void setClient(ValidationResult validationResult);
}
