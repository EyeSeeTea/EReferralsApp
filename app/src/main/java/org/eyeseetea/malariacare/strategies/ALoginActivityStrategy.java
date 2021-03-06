package org.eyeseetea.malariacare.strategies;


import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.entity.Credentials;

public abstract class ALoginActivityStrategy {
    protected LoginActivity loginActivity;

    public ALoginActivityStrategy(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    public abstract void onBackPressed();

    public abstract void finishAndGo();

    public abstract void onCreate();


    public abstract void initViews();

    public abstract void onLoginSuccess(Credentials credentials);

    public void onLoginNetworkError(Credentials credentials) {
        loginActivity.hideProgressBar();
        loginActivity.showError(loginActivity.getString(R.string.network_error));
    }

    public void onServerNotAvailable(String message) {
        loginActivity.hideProgressBar();
        loginActivity.showError(message);
    }

    public void onBadCredentials() {
        loginActivity.hideProgressBar();
        loginActivity.showError(R.string.login_invalid_credentials);
    }

    public void onStart() {
    }

    public void disableLogin() {

    }

    public void onTextChange() {
        loginActivity.getLoginButton().setEnabled(
                !(loginActivity.getServerText().getText().toString().isEmpty()) &&
                        !(loginActivity.getUsernameEditText().getText().toString().isEmpty()) &&
                        !(loginActivity.getPasswordEditText().getText().toString().isEmpty()));
    }

    public abstract void initLoginUseCase();

    public void initProgramServer(){ }

    public void initWebviewServer(){}

    public void initProgramEndpoint(){}

    public void saveOtherValues(final ALoginActivityStrategy.SettingsCallback callback){
        callback.onSuccess();
    }

    public void loadSettings(SettingsCallback settingsCallback){
        settingsCallback.onSuccess();
    }

    public interface Callback {
        void onSuccess();

        void onSuccessDoLogin();

        void onError();
    }

    public interface SettingsCallback {
        void onSuccess();
    }


}
