/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.exception.ApiCallException;
import org.eyeseetea.malariacare.domain.usecase.ALoginUseCase;
import org.eyeseetea.malariacare.domain.usecase.LoginUseCase;
import org.eyeseetea.malariacare.network.ServerAPIController;
import org.eyeseetea.malariacare.strategies.ALoginActivityStrategy;
import org.eyeseetea.malariacare.strategies.LoginActivityStrategy;
import org.eyeseetea.malariacare.utils.LanguageContextWrapper;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.AbsTextWatcher;
import org.eyeseetea.malariacare.views.dialog.AnnouncementMessageDialog;

import java.io.InputStream;
import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

/**
 * Login Screen.
 * It shows only when the user has an open session.
 */
public class LoginActivity extends Activity {

    public static final String PULL_REQUIRED = "PULL_REQUIRED";
    public static final String DEFAULT_USER = "";
    public static final String DEFAULT_PASSWORD = "";
    private static final String TAG = ".LoginActivity";
    private static final String IS_LOADING = "state:isLoading";
    public LoginUseCase mLoginUseCase;
    public LoginActivityStrategy mLoginActivityStrategy;
    EditText serverText;
    EditText usernameEditText;
    EditText passwordEditText;
    private Button loginButton;


    private ProgressBar bar;
    private LayoutTransition layoutTransition;
    // Animations for pre-JellyBean devices
    private Animation layoutTransitionSlideIn;
    private Animation layoutTransitionSlideOut;
    // Action which should be executed after animation is finished
    private OnPostAnimationRunnable onPostAnimationAction;

    // Callback which will be triggered when animations are finished
    private OnPostAnimationListener onPostAnimationListener;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AndroidLifeCycle: onCreate");
        setContentView(R.layout.activity_login);
        PreferencesState.getInstance().onCreateActivityPreferences(getResources(), getTheme());
        initLoginUseCase();
        AsyncInit asyncPopulateDB = new AsyncInit(this);
        asyncPopulateDB.execute((Void) null);
        if(BuildConfig.translations) {
            PreferencesState.getInstance().loadsLanguageInActivity();
        }
        mContext = this;
    }

    private void initLoginUseCase() {
        mLoginActivityStrategy = new LoginActivityStrategy(this);
        mLoginActivityStrategy.initLoginUseCase();
    }

    private void initDataDownloadPeriodDropdown() {
        if (!BuildConfig.loginDataDownloadPeriod) {
            return;
        }

        ViewGroup loginViewsContainer = (ViewGroup) findViewById(
                R.id.login_dynamic_views_container);

        getLayoutInflater().inflate(R.layout.login_spinner, loginViewsContainer,
                true);

        //Add left text for the spinner "title"
        findViewById(R.id.date_spinner_container).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.data_text_view);
        textView.setText(R.string.download);

        //add options
        ArrayList<String> dataLimitOptions = new ArrayList<>();
        dataLimitOptions.add(getString(R.string.no_data));
        dataLimitOptions.add(getString(R.string.last_6_days));
        dataLimitOptions.add(getString(R.string.last_6_weeks));
        dataLimitOptions.add(getString(R.string.last_6_months));

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dataLimitOptions);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //add spinner
        Spinner spinner = (Spinner) findViewById(R.id.data_spinner);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                PreferencesState.getInstance().setDataLimitedByDate(
                        spinnerArrayAdapter.getItem(pos).toString());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //select the selected option or default no data option
        String dateLimit = PreferencesState.getInstance().getDataLimitedByDate();
        if (dateLimit.equals("")) {
            spinner.setSelection(spinnerArrayAdapter.getPosition(getString(R.string.no_data)));
        } else {
            spinner.setSelection(spinnerArrayAdapter.getPosition(dateLimit));
        }
        if(BuildConfig.pullDataDropdown) {
            spinner.setVisibility(View.VISIBLE);
        }else{
            (findViewById(R.id.date_spinner_container)).setVisibility(View.GONE);
            PreferencesState.getInstance().setDataLimitedByDate(getString(R.string.no_data));
        }
    }

    protected void onLoginButtonClicked(Editable server, Editable username, Editable password) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.eula_accepted), false) || !BuildConfig.askEula) {
            login(server.toString(), username.toString(), password.toString());
        } else {
            askEula(R.string.app_EULA, R.raw.eula, LoginActivity.this);
        }
    }


    /**
     * Shows an alert dialog asking for acceptance of the EULA terms. If ok calls login function,
     * do
     * nothing otherwise
     */
    public void askEula(int titleId, int rawId, final Context context) {
        InputStream message = context.getResources().openRawResource(rawId);
        String stringMessage = Utils.convertFromInputStreamToString(message).toString();
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setMessage(linkedMessage)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rememberEulaAccepted(context);
                        login(serverText.getText().toString(),
                                usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.no, null).create();

        dialog.show();

        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(
                LinkMovementMethod.getInstance());
    }

    /**
     * Save a preference to remember that EULA was already accepted
     */
    public void rememberEulaAccepted(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.eula_accepted), true);
        editor.commit();
    }

    private static boolean isGreaterThanOrJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mLoginActivityStrategy.onOptionsItemSelected(item);

        return super.onOptionsItemSelected(item);
    }
    public void showError(int message) {
        Toast.makeText(this, translate(message),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        mLoginActivityStrategy.onBackPressed();
    }

    private void init() {
        FieldTextWatcher watcher = new FieldTextWatcher();
        initDataDownloadPeriodDropdown();
        //Populate server with the current value
        initServerUrls(watcher);
        //Username, Password blanks to force real login
        usernameEditText = (EditText) findViewById(R.id.edittext_username);
        usernameEditText.setText(DEFAULT_USER);
        usernameEditText.addTextChangedListener(watcher);
        passwordEditText = (EditText) findViewById(R.id.edittext_password);
        passwordEditText.setText(DEFAULT_PASSWORD);
        passwordEditText.addTextChangedListener(watcher);
        findViewById(R.id.button_log_out).setVisibility(View.GONE);
        loginButton = (Button) findViewById(R.id.button_log_in);
        loginButton.setEnabled(false);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginActivityStrategy.saveOtherValues(new ALoginActivityStrategy.SettingsCallback() {
                    @Override
                    public void onSuccess() {
                        onLoginButtonClicked(serverText.getText(), usernameEditText.getText(),
                                passwordEditText.getText());
                    }
                });
            }
        });

        mLoginActivityStrategy.initViews();
    }

    private void initServerUrls(FieldTextWatcher watcher) {
        serverText = (EditText) findViewById(R.id.edittext_loginweb_server_url);
        serverText.setText(ServerAPIController.getServerUrl());
        serverText.addTextChangedListener(watcher);

        mLoginActivityStrategy.initProgramServer();
        mLoginActivityStrategy.initWebviewServer();
        mLoginActivityStrategy.initProgramEndpoint();
    }

    public void login(String serverUrl, String username, String password) {
        final Credentials credentials = new Credentials(serverUrl, username, password);
        onStartLoading();
        executeLoginUseCase(credentials);
    }


    private void executeLoginUseCase(final Credentials credentials) {
        mLoginUseCase.execute(credentials, new ALoginUseCase.Callback() {
            @Override
            public void onLoginSuccess() {
                Log.d(TAG, "onLoginSuccess");
                mLoginActivityStrategy.onLoginSuccess(credentials);
            }

            @Override
            public void onServerURLNotValid() {
                Log.d(TAG, "onServerURLNotValid");
                onFinishLoading(null);
                serverText.setError(
                        translate(R.string.login_invalid_server_url));
                showError(translate(R.string.login_invalid_server_url));
            }

            @Override
            public void onInvalidCredentials() {
                Log.d(TAG, "onInvalidCredentials");
                mLoginActivityStrategy.onBadCredentials();
            }

            @Override
            public void onNetworkError() {
                Log.d(TAG, "onNetworkError");
                mLoginActivityStrategy.onLoginNetworkError(credentials);
            }

            @Override
            public void onConfigJsonInvalid() {
                Log.d(TAG, "onConfigJsonInvalid");
                onFinishLoading(null);
                showError(translate(R.string.login_error_json));
            }

            @Override
            public void onUnexpectedError() {
                Log.d(TAG, "onUnexpectedError");
                hideProgressBar();
                showError(translate(R.string.login_unexpected_error));
            }

            @Override
            public void onMaxLoginAttemptsReachedError() {
                Log.d(TAG, "onMaxLoginAttemptsReachedError");
                mLoginActivityStrategy.disableLogin();
            }

            @Override
            public void onServerNotAvailable(String message) {
                Log.d(TAG, "onServerNotAvailable");
                mLoginActivityStrategy.onServerNotAvailable(message);
            }
        });
    }

    private void initProgressBar() {
        bar = (CircularProgressBar) findViewById(R.id.progress_bar_circular);
        float progressBarStrokeWidth = getResources()
                .getDimensionPixelSize(R.dimen.progressbar_stroke_width);
        bar.setIndeterminateDrawable(new CircularProgressDrawable.Builder(this)
                .color(ContextCompat.getColor(this, R.color.color_primary_default))
                .style(CircularProgressDrawable.STYLE_ROUNDED)
                .strokeWidth(progressBarStrokeWidth)
                .rotationSpeed(1f)
                .sweepSpeed(1f)
                .build());
        onPostAnimationListener = new OnPostAnimationListener();

        /* adding transition animations to root layout */
        if (isGreaterThanOrJellyBean()) {
            layoutTransition = new LayoutTransition();
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            layoutTransition.addTransitionListener(onPostAnimationListener);

            RelativeLayout loginLayoutContent = (RelativeLayout) findViewById(R.id.layout_content);

            loginLayoutContent.setLayoutTransition(layoutTransition);
        } else {
            layoutTransitionSlideIn = AnimationUtils.loadAnimation(this, R.anim.in_up);
            layoutTransitionSlideOut = AnimationUtils.loadAnimation(this, R.anim.out_down);

            layoutTransitionSlideIn.setAnimationListener(onPostAnimationListener);
            layoutTransitionSlideOut.setAnimationListener(onPostAnimationListener);
        }
    }

    public void showProgressBar() {
        hideKeyboard();
        if (layoutTransitionSlideOut != null) {
            findViewById(R.id.layout_login_views).startAnimation(layoutTransitionSlideOut);
        }
        bar.setVisibility(View.VISIBLE);
        findViewById(R.id.layout_login_views).setVisibility(View.GONE);
    }

    public void hideProgressBar() {
        if (layoutTransitionSlideIn != null) {
            findViewById(R.id.layout_login_views).startAnimation(layoutTransitionSlideIn);
        }
        bar.setVisibility(View.GONE);
        findViewById(R.id.layout_login_views).setVisibility(View.VISIBLE);
    }

    /**
     * Should be called in order to show progressbar.
     */
    public final void onStartLoading() {
        if (isAnimationInProgress()) {
            onPostAnimationAction = new OnPostAnimationRunnable(null, this, true);
        } else {
            showProgressBar();
        }
    }

    public void checkAnnouncement() {
        PreferencesState.getInstance().setUserAccept(false);
        AsyncPullAnnouncement asyncPullAnnouncement = new AsyncPullAnnouncement();
        asyncPullAnnouncement.execute(LoginActivity.this);
    }

    public void enableLogin(boolean enable) {
        if (loginButton != null && !usernameEditText.getText().toString().isEmpty()
                && !passwordEditText.getText().toString().isEmpty()) {
            loginButton.setEnabled(enable);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "AndroidLifeCycle: onStart");
        super.onStart();
        mLoginActivityStrategy.onStart();
    }

    private void onTextChanged() {
        mLoginActivityStrategy.onTextChange();
    }

    public EditText getServerText() {
        return serverText;
    }

    public EditText getUsernameEditText() {
        return usernameEditText;
    }

    public EditText getPasswordEditText() {
        return passwordEditText;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    /**
     * Should be called after the loading is complete.
     */
    public final void onFinishLoading(OnAnimationFinishListener listener) {
        if (isAnimationInProgress()) {
            onPostAnimationAction = new OnPostAnimationRunnable(listener, this, false);
            return;
        }

        hideProgressBar();
        if (listener != null) {
            listener.onFinish();
        }
    }

    private boolean isAnimationInProgress() {
        boolean layoutTransitionAnimationsInProgress =
                layoutTransition != null && layoutTransition.isRunning();
        boolean layoutTransitionAnimationSlideUpInProgress = layoutTransitionSlideIn != null &&
                layoutTransitionSlideIn.hasStarted() && !layoutTransitionSlideIn.hasEnded();
        boolean layoutTransitionAnimationSlideOutInProgress = layoutTransitionSlideOut != null &&
                layoutTransitionSlideOut.hasStarted() && !layoutTransitionSlideOut.hasEnded();

        return layoutTransitionAnimationsInProgress ||
                layoutTransitionAnimationSlideUpInProgress ||
                layoutTransitionAnimationSlideOutInProgress;
    }

    @Override
    protected final void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(IS_LOADING, false)) {
            showProgressBar();
        } else {
            hideProgressBar();
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "AndroidLifeCycle: onPause");
        if (onPostAnimationAction != null) {
            onPostAnimationAction.run();
            onPostAnimationAction = null;
        }

        super.onPause();
    }

    @Override
    protected final void onSaveInstanceState(Bundle outState) {
        if (onPostAnimationAction != null) {
            outState.putBoolean(IS_LOADING,
                    onPostAnimationAction.isProgressBarWillBeShown());
        } else {
            outState.putBoolean(IS_LOADING, bar.isShown());
        }

        super.onSaveInstanceState(outState);
    }

    protected interface OnAnimationFinishListener {
        void onFinish();
    }

    /* since this runnable is intended to be executed on UI (not main) thread, we should
    be careful and not keep any implicit references to activities */
    private static class OnPostAnimationRunnable implements Runnable {
        private final OnAnimationFinishListener listener;
        private final LoginActivity loginActivity;
        private final boolean showProgress;

        public OnPostAnimationRunnable(OnAnimationFinishListener listener,
                LoginActivity loginActivity, boolean showProgress) {
            this.listener = listener;
            this.loginActivity = loginActivity;
            this.showProgress = showProgress;
        }

        @Override
        public void run() {
            if (loginActivity != null) {
                if (showProgress) {
                    loginActivity.showProgressBar();
                } else {
                    loginActivity.hideProgressBar();
                }
            }

            if (listener != null) {
                listener.onFinish();
            }
        }

        public boolean isProgressBarWillBeShown() {
            return showProgress;
        }
    }

    public class AsyncInit extends AsyncTask<Void, Void, Exception> {
        Activity activity;

        AsyncInit(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            //// FIXME: 30/12/16  Fix mising progressbar
            initProgressBar();
            onStartLoading();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            //TODO jsanchez, Why is called from AsyncTask?, It's not very correct and force
            //run explicitly in main thread accions over views in LoginActivityStrategy
            mLoginActivityStrategy.onCreate();
            return null;
        }

        @Override
        protected void onPostExecute(final Exception exception) {
            //Error
            mLoginActivityStrategy.loadSettings(new ALoginActivityStrategy.SettingsCallback() {
                @Override
                public void onSuccess() {
                    onFinishLoading(null);
                    init();
                }
            });
        }
    }

    public class AsyncPullAnnouncement extends AsyncTask<LoginActivity, Void, Void> {
        //userCloseChecker is never saved, Only for check if the date is closed.
        LoginActivity loginActivity;
        Boolean isUserClosed = false;

        @Override
        protected Void doInBackground(LoginActivity... params) {
            loginActivity = params[0];
            if(Session.getUserDB()==null){
                isUserClosed = null;
                return null;
            }
            try {
                isUserClosed = ServerAPIController.isUserClosed(Session.getUserDB().getUid());
            }catch (ApiCallException e){
                isUserClosed = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isUserClosed != null && isUserClosed) {
                onFinishLoading(null);
                Log.d(TAG, "user closed");
                AnnouncementMessageDialog.closeUser(R.string.admin_announcement,
                        PreferencesState.getInstance().getContext().getString(R.string.user_close),
                        LoginActivity.this);
            } else {
                mLoginActivityStrategy.finishAndGo();
            }
        }
    }

    private class FieldTextWatcher extends AbsTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            LoginActivity.this.onTextChanged();
        }
    }

    private class OnPostAnimationListener implements LayoutTransition.TransitionListener,
            Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            // stub implementation
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // stub implementation
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            onPostAnimation();
        }

        @Override
        public void startTransition(
                LayoutTransition transition, ViewGroup container, View view, int type) {
            // stub implementation
        }

        @Override
        public void endTransition(
                LayoutTransition transition, ViewGroup container, View view, int type) {
            if (LayoutTransition.CHANGE_APPEARING == type ||
                    LayoutTransition.CHANGE_DISAPPEARING == type) {
                onPostAnimation();
            }
        }

        private void onPostAnimation() {
            if (onPostAnimationAction != null) {
                onPostAnimationAction.run();
                onPostAnimationAction = null;
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLanguage = PreferencesState.getInstance().getCurrentLocale();
        Context context = LanguageContextWrapper.wrap(newBase, currentLanguage);
        super.attachBaseContext(context);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "AndroidLifeCycle: onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "AndroidLifeCycle: onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "AndroidLifeCycle: onRestart");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "AndroidLifeCycle: onDestroy");
        super.onDestroy();
    }

    public String translate(@StringRes int id){
        return Utils.getInternationalizedString(id, this);
    }
}



