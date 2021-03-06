package org.eyeseetea.malariacare.data.authentication.api;

import android.support.annotation.NonNull;

import org.eyeseetea.malariacare.data.authentication.CredentialsReader;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.exception.ConfigJsonIOException;

import okhttp3.Credentials;

public class AuthenticationApi {


    public static String getHardcodedApiCredentials() throws ConfigJsonIOException {
        return
                Credentials.basic(getHardcodedApiUser(),
                        getHardcodedApiPass());
    }

    public static String getApiCredentialsFromLogin() {
        return
                Credentials.basic(Session.getCredentials().getUsername(),
                        Session.getCredentials().getPassword());
    }

    @NonNull
    public static String getHardcodedApiUser() throws ConfigJsonIOException {
        return CredentialsReader.getInstance().getUser();
    }

    @NonNull
    public static String getHardcodedApiPass() throws ConfigJsonIOException {
        return CredentialsReader.getInstance().getPassword();
    }
}
