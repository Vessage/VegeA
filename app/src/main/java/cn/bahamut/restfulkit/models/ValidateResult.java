package cn.bahamut.restfulkit.models;

/**
 * Created by alexchow on 16/4/5.
 */
public class ValidateResult {
    //validate success part
    String UserId;
    String AppToken;
    String APIServer;
    String FileAPIServer;
    String ChicagoServer;

    //new user part
    String RegistAPIServer;

    public Boolean isValidateResultDataComplete() {
        if (RegistAPIServer != null) {
            return true;
        } else {
            return (UserId != null &&
                    AppToken != null &&
                    FileAPIServer != null &&
                    APIServer != null &&
                    ChicagoServer != null
            );
        }
    }
}