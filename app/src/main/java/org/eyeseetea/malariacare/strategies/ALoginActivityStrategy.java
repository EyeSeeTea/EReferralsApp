package org.eyeseetea.malariacare.strategies;


import android.view.MenuItem;

import org.eyeseetea.malariacare.LoginActivity;

public abstract class ALoginActivityStrategy {
    protected LoginActivity loginActivity;

    public ALoginActivityStrategy(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    public abstract void onBackPressed();

    public abstract void finishAndGo();

    public abstract void onCreate();

}
