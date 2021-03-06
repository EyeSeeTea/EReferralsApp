package org.eyeseetea.malariacare.domain.boundary;

import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ForgotPasswordMessage;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public interface IAuthenticationManager {


    interface Callback<T> {
        void onSuccess(T result);

        void onError(Throwable throwable);
    }

    void login(Credentials credentials, Callback<UserAccount> callback);

    void hardcodedLogin(String url, Callback<UserAccount> callback);

    void logout(Callback<Void> callback);

    void forgotPassword(String wsVersion, String username, Callback<ForgotPasswordMessage> callback);
}