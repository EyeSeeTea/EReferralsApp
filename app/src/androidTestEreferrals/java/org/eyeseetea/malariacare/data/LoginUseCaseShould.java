package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.AssetsFileReader;
import org.eyeseetea.malariacare.data.authentication.AuthenticationManager;
import org.eyeseetea.malariacare.data.remote.AuthenticationWSDataSource;
import org.eyeseetea.malariacare.data.remote.IForgotPasswordDataSource;
import org.eyeseetea.malariacare.data.server.CustomMockServer;
import org.eyeseetea.malariacare.data.sync.exporter.eReferralsAPIClient;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ICredentialsRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IInvalidLoginAttemptsRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.InvalidLoginAttempts;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginUseCaseShould {

    private static final String AUTH_OK = "auth_ok.json";
    private static final String API_AVAILABLE_OK = "api_available_ok.json";
    private static final String API_AVAILABLE_NO_OK = "api_available_no_ok.json";
    Credentials credentials;
    private CustomMockServer mCustomMockServer;
    @Mock
    IConnectivityManager connectivityManager;
    @Mock
    IAuthenticationDataSource authenticationLocalDataSource;
    @Mock
    IForgotPasswordDataSource forgotPasswordDataSource;
    @Mock
    ISettingsRepository settingsRepository;
    @Mock
    ICredentialsRepository mCredentialsRepository;
    @Mock
    IInvalidLoginAttemptsRepository mInvalidLoginAttemptsRepository;

    @Before
    public void cleanUp() throws IOException {
        mCustomMockServer = new CustomMockServer(new AssetsFileReader());
        credentials = new Credentials(mCustomMockServer.getBaseEndpoint(), "test", "test");
    }

    @Test
    public void call_on_login_success_callback_when_server_is_available_during_login() throws IOException {

        LoginUseCase mLoginUseCase = givenALoginUseCase();
        mCustomMockServer.enqueueMockResponseFileName(200, API_AVAILABLE_OK);
        mCustomMockServer.enqueueMockResponseFileName(200, AUTH_OK);

        mLoginUseCase.execute(credentials, new LoginUseCase.Callback() {
            @Override
            public void onLoginSuccess() {
                Assert.assertTrue(true);
            }

            @Override
            public void onServerURLNotValid() {
                fail("onLoginSuccess");
            }

            @Override
            public void onInvalidCredentials() {
                fail("onLoginSuccess");
            }

            @Override
            public void onServerPinChanged() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onConfigJsonInvalid() {
                fail("onLoginSuccess");
            }

            @Override
            public void onUnexpectedError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onMaxLoginAttemptsReachedError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onServerNotAvailable() {
                fail("onServerNotAvailable");
            }

        });
    }

    @Test
    public void call_on_server_not_available_callback_when_server_is_not_available_during_login() throws IOException {

        LoginUseCase mLoginUseCase = givenALoginUseCase();
        mCustomMockServer.enqueueMockResponseFileName(200, API_AVAILABLE_NO_OK);

        mLoginUseCase.execute(new Credentials(mCustomMockServer.getBaseEndpoint(), "test", "test"), new LoginUseCase.Callback() {
            @Override
            public void onLoginSuccess() {
                fail("onLoginSuccess");
            }

            @Override
            public void onServerURLNotValid() {
                fail("onLoginSuccess");
            }

            @Override
            public void onInvalidCredentials() {
                fail("onLoginSuccess");
            }

            @Override
            public void onServerPinChanged() {
                fail("onLoginSuccess");
            }

            @Override
            public void onNetworkError() {
                fail("onNetworkError");
            }

            @Override
            public void onConfigJsonInvalid() {
                fail("onLoginSuccess");
            }

            @Override
            public void onUnexpectedError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onMaxLoginAttemptsReachedError() {
                fail("onLoginSuccess");
            }

            @Override
            public void onServerNotAvailable() {
                Assert.assertTrue(true);
            }

        });
    }

    private LoginUseCase givenALoginUseCase() {
        when(connectivityManager.isDeviceOnline()).thenReturn(true);
        IAuthenticationDataSource authenticationDataSource = new AuthenticationWSDataSource(new eReferralsAPIClient(mCustomMockServer.getBaseEndpoint()));

        AuthenticationManager authenticationManager = new AuthenticationManager(authenticationLocalDataSource, authenticationDataSource, forgotPasswordDataSource, settingsRepository);

        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new IAsyncExecutor() {
            @Override
            public void run(Runnable runnable) {
                runnable.run();
            }
        };

        when(mCredentialsRepository.getLastValidCredentials()).thenReturn(credentials);

        when(mInvalidLoginAttemptsRepository.getInvalidLoginAttempts()).thenReturn(new InvalidLoginAttempts(0,30));
        return new LoginUseCase(connectivityManager, authenticationManager, mainExecutor, asyncExecutor, mCredentialsRepository, mInvalidLoginAttemptsRepository);
    }
}
