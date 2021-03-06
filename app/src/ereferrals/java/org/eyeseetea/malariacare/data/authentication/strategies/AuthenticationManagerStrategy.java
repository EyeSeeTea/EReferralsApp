package org.eyeseetea.malariacare.data.authentication.strategies;

import android.content.Context;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.datasources.SettingsDataSource;
import org.eyeseetea.malariacare.data.remote.ForgotPasswordWSDataSource;
import org.eyeseetea.malariacare.data.remote.IForgotPasswordDataSource;
import org.eyeseetea.malariacare.domain.boundary.IAuthenticationManager;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.ForgotPasswordMessage;
import org.eyeseetea.malariacare.domain.entity.Settings;


public class AuthenticationManagerStrategy extends AAuthenticationManagerStrategy {
    IForgotPasswordDataSource mForgotPasswordDataSource;
    ISettingsRepository mSettingsRepository;

    public AuthenticationManagerStrategy(IForgotPasswordDataSource forgotPasswordDataSource,
                                         ISettingsRepository settingsDataSource) {
        mForgotPasswordDataSource = forgotPasswordDataSource;
        mSettingsRepository = settingsDataSource;
    }

    @Override
    public void forgotPassword(final String wsVersion, final String username,
            final IAuthenticationManager.Callback<ForgotPasswordMessage> callback) {

        Settings settings = mSettingsRepository.getSettings();
        mForgotPasswordDataSource.forgotPassword(wsVersion, username, settings.getLanguage(),
                        new IDataSourceCallback<ForgotPasswordMessage>() {
                            @Override
                            public void onSuccess(ForgotPasswordMessage result) {
                                callback.onSuccess(result);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                callback.onError(throwable);
                            }
                        });
            }
}
