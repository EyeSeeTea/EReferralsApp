package org.eyeseetea.malariacare.strategies;


import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.domain.boundary.IAuthenticationManager;
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

    public abstract void initLoginUseCase(IAuthenticationManager authenticationManager);
}
