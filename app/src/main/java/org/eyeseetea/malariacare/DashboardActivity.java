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

import static org.eyeseetea.malariacare.BuildConfig.exitFromSurveyToImproveTab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.exception.LoadingNavigationControllerException;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.domain.usecase.RemoveSurveysInProgressUseCase;
import org.eyeseetea.malariacare.factories.AuthenticationFactoryStrategy;
import org.eyeseetea.malariacare.fragments.ReviewFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.fragments.WebViewFragment;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.strategies.DashboardActivityStrategy;
import org.eyeseetea.malariacare.utils.GradleVariantConfig;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.dialog.AnnouncementMessageDialog;

public class DashboardActivity extends BaseActivity {

    private final static String TAG = ".DashboardActivity";
    public static DashboardActivity dashboardActivity;
    /**
     * Move to that question from reviewfragment
     */
    public static String moveToThisUId;
    TabHost tabHost;
    ReviewFragment reviewFragment;
    SurveyFragment surveyFragment;
    DashboardActivityStrategy mDashboardActivityStrategy;
    static Handler handler;
    /**
     * Flag that controls the fragment change animations
     */
    boolean isMoveToLeft;
    private boolean reloadOnResume = true;
    /**
     * Flags required to decide if the survey must be deleted or not on pause the surveyFragment
     */
    private boolean isLoadingReview = false;

    /**
     * Flags required to decide if the survey must be deleted or not
     */
    private boolean isBackPressed = false;
    /**
     * Flags required to decide if the survey read only
     */
    private boolean isReadOnly = false;

    private boolean mIsInForegroundMode;

    // Some function.
    public boolean isInForeground() {
        return mIsInForegroundMode;
    }

    //Show dialog exception from class without activity.
    public static void showException(Context context, final String title, final String errorMessage) {
        showException(context, title, errorMessage, null);
    }
    public static void showException(Context context, final String title, final String errorMessage, final DialogInterface.OnClickListener listener) {
        String dialogTitle = "", dialogMessage = "";
        if (title != null) {
            dialogTitle = title;
        }
        if (errorMessage != null) {
            dialogMessage = errorMessage;
        }
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setNeutralButton(R.string.action_ok, listener)
                .create().show();
    }

    //Show dialog exception from class without activity.
    public static void closeUserFromService(final int title, final String errorMessage) {
        AnnouncementMessageDialog.closeUser(title, errorMessage, dashboardActivity);
    }

    public void setTabHostsWithText() {
        Context context = PreferencesState.getInstance().getContext();
        setTab(context.getResources().getString(R.string.tab_tag_assess), R.id.tab_assess_layout,
                context.getResources().getString(R.string.unsent_data));
        setTab(context.getResources().getString(R.string.tab_tag_improve), R.id.tab_improve_layout,
                context.getResources().getString(R.string.sent_data));
        if (GradleVariantConfig.isStockFragmentActive()) {
            setTab(context.getResources().getString(R.string.tab_tag_stock), R.id.tab_stock_layout,
                    context.getResources().getString(R.string.tab_stock));
        }
        if (GradleVariantConfig.isAVFragmentActive()) {
            setTab(context.getResources().getString(R.string.tab_tag_av), R.id.tab_av_layout,
                    context.getResources().getString(R.string.tab_av));
        }
        if (GradleVariantConfig.isMonitoringFragmentActive()) {
            setTab(context.getResources().getString(R.string.tab_tag_monitor),
                    R.id.tab_monitor_layout,
                    context.getResources().getString(R.string.common_menu_statistics));
        }
        if (GradleVariantConfig.isAVFragmentActive()) {

        }
        if (GradleVariantConfig.isStockFragmentActive()) {
            initStock();
        }
    }

    public void setTabHostsWithImages() {
        setTab(getResources().getString(R.string.tab_tag_assess), R.id.tab_assess_layout,
                getResources().getDrawable(R.drawable.tab_assess));
        setTab(getResources().getString(R.string.tab_tag_improve), R.id.tab_improve_layout,
                getResources().getDrawable(R.drawable.tab_improve));
        if (GradleVariantConfig.isStockFragmentActive()) {
            mDashboardActivityStrategy.setStockTab(tabHost);
        }
        if (GradleVariantConfig.isAVFragmentActive()) {
            setTab(getResources().getString(R.string.tab_tag_av), R.id.tab_av_layout,
                    getResources().getDrawable(R.drawable.statics));
        }
        if(GradleVariantConfig.isMonitoringFragmentActive()) {
            setTab(getResources().getString(R.string.tab_tag_monitor),
                    R.id.tab_monitor_layout,
                    getResources().getDrawable(R.drawable.tab_monitor));
        }
        if(GradleVariantConfig.isStockControlActive()){
            mDashboardActivityStrategy.setStockControlTab(tabHost);
        }
    }

    /**
     * Sets a divider drawable and background.
     */
    public void setTabDivider() {
        tabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
        tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_line);
        tabHost.getTabWidget().setBackgroundColor(
                ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                        R.color.tab_unpressed_background));
    }

    private void setTabsBackgroundColor(int color) {
        //set the tabs background as transparent
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(color));
        }
    }

    /**
     * Init the conteiner for all the tabs
     */
    private void initTabHost(Bundle savedInstanceState) {
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
    }

    /**
     * Set tab in tabHost
     *
     * @param tabName is the name of the tab
     * @param layout  is the id of the layout
     */
    private void setTab(String tabName, int layout, String text) {
        TabHost.TabSpec tab = tabHost.newTabSpec(tabName);
        tab.setContent(layout);
        tab.setIndicator(text);
        tabHost.addTab(tab);
        addTagToLastTab(tabName);
    }

    /**
     * Set tab in tabHost
     *
     * @param tabName is the name of the tab
     * @param layout  is the id of the layout
     */
    private void setTab(String tabName, int layout, Drawable image) {
        TabHost.TabSpec tab = tabHost.newTabSpec(tabName);
        tab.setContent(layout);
        tab.setIndicator("", image);
        tabHost.addTab(tab);
        addTagToLastTab(tabName);
    }

    private void addTagToLastTab(String tabName) {
        TabWidget tabWidget = tabHost.getTabWidget();
        int numTabs = tabWidget.getTabCount();
        ViewGroup tabIndicator = (ViewGroup) tabWidget.getChildTabViewAt(numTabs - 1);

        ImageView imageView = (ImageView) tabIndicator.getChildAt(0);
        imageView.setTag(tabName);
        TextView textView = (TextView) tabIndicator.getChildAt(1);
        textView.setGravity(Gravity.CENTER);
        textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

    }


    public void initAssess() {
        mDashboardActivityStrategy.showFirstFragment();
    }

    public void showUnsentFragment() {
        mDashboardActivityStrategy.showUnsentFragment();
    }

    public void restoreAssess() {
        if (surveyFragment == null) {
            surveyFragment = new SurveyFragment();
        }
        replaceFragment(mDashboardActivityStrategy.getSurveyContainer(), surveyFragment);
    }

    /**
     * This method initializes the reviewFragment
     * @param fromReviewList
     */
    public void initReview(final boolean fromReviewList, String surveyUid) {
        if (surveyFragment != null) {
            surveyFragment.mReviewMode = true;
        }

        reviewFragment = ReviewFragment.newInstance(surveyUid);

        replaceFragment(mDashboardActivityStrategy.getSurveyContainer(), reviewFragment);
        reviewFragment.reloadHeader(dashboardActivity);
        reviewFragment.setOnEndReviewListener(new ReviewFragment.OnEndReviewListener() {
            @Override
            public void onEndReview(String surveyUid, boolean afterCompletion) {
                exitReview(fromReviewList, surveyUid, afterCompletion);
            }
        });
    }

    /**
     * This method initializes the Improve fragment(DashboardSentFragment)
     */
    public void initImprove() {
        mDashboardActivityStrategy.showSecondFragment();
    }

    /**
     * This method initializes the Stock fragment(StockFragment)
     */
    public void initStock() {
        isMoveToLeft = mDashboardActivityStrategy.showStockFragment(this, isMoveToLeft);
    }

    /**
     * This method initializes the AV fragment
     */
    public void initAV() {
        mDashboardActivityStrategy.showAVFragment();
    }

    /**
     * This method initializes the Survey fragment
     */
    public void initSurvey() {
        isBackPressed = false;
        tabHost.getTabWidget().setVisibility(View.GONE);
        SurveyDB surveyDB = Session.getMalariaSurveyDB();
        if (surveyDB.isInProgress()
                || !BuildConfig.openReviewCompletedSurveys) {
            if (surveyFragment == null) {
                surveyFragment = new SurveyFragment();
            }
            surveyFragment.reloadHeader(dashboardActivity);
            replaceFragment(mDashboardActivityStrategy.getSurveyContainer(), surveyFragment);
            android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
            LayoutUtils.setSurveyActionBar(actionBar);
        } else {
            findViewById(R.id.common_header).setVisibility(View.GONE);
            initReview(true, surveyDB.getEventUid());
        }

    }

    /**
     * This method initializes the Monitor fragment
     */
    public void initMonitor() {
        mDashboardActivityStrategy.showFourthFragment();
    }


    public void initNewReceiptFragment() {
        tabHost.getTabWidget().setVisibility(View.GONE);
    }


    private Fragment currentFragment;

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(Fragment fragment) {
        //TODO: with view pager this manual management of visibility would be no necessary
        changeFragmentVisibilityFlag(currentFragment, false);
        currentFragment = fragment;
        changeFragmentVisibilityFlag(currentFragment, true);
    }

    // Add the fragment to the activity, pushing this transaction
    // on to the back stack.
    public void replaceFragment(int layout, Fragment fragment) {
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    private void changeFragmentVisibilityFlag(Fragment fragment, boolean isVisible) {
        if (fragment != null && fragment instanceof WebViewFragment) {
            WebViewFragment webViewFragment = (WebViewFragment) fragment;

            webViewFragment.changeVisibility(isVisible);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void openPendingSurveyIfRequired() {
        mDashboardActivityStrategy.openPendingSurveyIfRequired();
    }

    public void replaceListFragment(int layout, ListFragment fragment) {
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (BuildConfig.translations) {
            PreferencesState.getInstance().loadsLanguageInActivity();
        }
    }

    @NonNull
    private FragmentTransaction getFragmentTransaction() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (isMoveToLeft) {
            isMoveToLeft = false;
            ft.setCustomAnimations(R.animator.anim_slide_in_right, R.animator.anim_slide_out_right);
        } else {
            ft.setCustomAnimations(R.animator.anim_slide_in_left, R.animator.anim_slide_out_left);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        return ft;
    }

    /**
     * Init the fragments
     */
    private void setFragmentTransaction(int layout, ListFragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(layout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    protected void initTransition() {
        this.overridePendingTransition(R.transition.anim_slide_in_right,
                R.transition.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "AndroidLifeCycle: onResume");
        mDashboardActivityStrategy.onResume();
        super.onResume();
        mIsInForegroundMode = true;
        getSurveysFromService();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "AndroidLifeCycle: onRestart");
    }


    @Override
    public void onPause() {
        Log.d(TAG, "AndroidLifeCycle: onPause");
        super.onPause();
        mIsInForegroundMode = false;
        mDashboardActivityStrategy.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "AndroidLifeCycle: onStop");
        super.onStop();
    }

    public void setReloadOnResume(boolean doReload) {
        this.reloadOnResume = false;
    }

    public void getSurveysFromService() {
        Log.d(TAG, "getSurveysFromService (" + reloadOnResume + ")");
        if (!reloadOnResume) {
            //Flag is readjusted
            reloadOnResume = true;
            return;
        }
        Intent surveysIntent = new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        isMoveToLeft = true;

        if (isSurveyFragmentActive()) {
            onSurveyBackPressed();
        } else if (isReviewFragmentActive()) {
            onSurveyBackPressed();
        } else if (isNewHistoricReceiptBalanceFragmentActive()) {
            closeReceiptBalanceFragment();
        } else if (isStockTableFragmentActive()) {
            closeStockTableFragment();
        } else {
            if (!mDashboardActivityStrategy.onWebViewBackPressed(tabHost)) {
                confirmExitApp();
            }
        }
    }


    public void confirmExitApp() {
        Log.d(TAG, "back pressed");
        new AlertDialog.Builder(this)
                .setTitle(translate(R.string.confirmation_really_exit_title))
                .setMessage(
                        translate(R.string.confirmation_really_exit))
                .setNegativeButton(translate(android.R.string.no),
                        null)
                .setPositiveButton(translate(android.R.string.yes),
                        new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * It is called when the user press back in a surveyFragment
     */
    public void onSurveyBackPressed() {
        Log.d(TAG, "onBackPressed");
        SurveyDB surveyDB = Session.getMalariaSurveyDB();
        if (!surveyDB.isSent()) {
            int infoMessage = surveyDB.isInProgress() ? R.string.survey_info_exit_delete
                    : R.string.survey_info_exit;
            new AlertDialog.Builder(this)
                    .setTitle(translate(R.string.survey_info_exit))
                    .setMessage(translate(infoMessage))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            //Reload data using service
                            isBackPressed = true;
                            closeSurveyFragment();
                        }
                    }).create().show();
        } else {
            closeSurveyFragment();
        }
    }

    /**
     * This method shows the review fragment to te user before decides if send the survey.
     */
    public void showReviewFragment(String surveyUid) {
        isLoadingReview = true;
        initReview(false, surveyUid);
    }


    /**
     * This method closes the survey fragment.
     * If the active survey was completed it will be saved as completed.
     * After that, loads the Assess fragment(DashboardUnSentFragment) in the Assess tab.
     */
    public void closeSurveyFragment() {
        surveyFragment.mReviewMode = false;
        boolean isSent = false;
        isReadOnly = false;
        isLoadingReview = false;
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setDashboardActionBar(actionBar);
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
        ScoreRegister.clear();
        SurveyDB lastSurvey = Session.getMalariaSurveyDB();
        if (lastSurvey != null) {
            isSent = Session.getMalariaSurveyDB().isSent();
        }
        if (isBackPressed) {
            beforeExit();
        }

        if (isSent) {
            showUnsentFragment();
            if (exitFromSurveyToImproveTab) {
                tabHost.setCurrentTabByTag(getResources().getString(R.string.tab_tag_improve));
            }
        } else {
            showUnsentFragment();
            mDashboardActivityStrategy.reloadFirstFragment();
        }

        super.surveyClosed();
    }

    public void closeReceiptBalanceFragment() {
        mDashboardActivityStrategy.showStockFragment(this, false);
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
    }

    public void closeStockTableFragment(){
        mDashboardActivityStrategy.initStockControlFragment();
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
    }


    /**
     * This method closes the Feedback Fragment and loads the Improve fragment
     * (DashboardSentFragment)
     * in the Improve tab
     */
    public void closeReviewFragment() {
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
        isLoadingReview = false;
        initAssess();
        mDashboardActivityStrategy.reloadFirstFragment();
    }


    public void beforeExit() {
        isBackPressed = mDashboardActivityStrategy.beforeExit(isBackPressed);
    }

    /**
     * Called when the user clicks the New Survey button
     */
    public void newSurvey(View view) {
        final Activity activity = this;
        AsyncExecutor asyncExecutor = new AsyncExecutor();
        UIThreadExecutor mainExecutor = new UIThreadExecutor();
        RemoveSurveysInProgressUseCase removeInProgressSurveyUseCase = new RemoveSurveysInProgressUseCase(mainExecutor, asyncExecutor, new SurveyLocalDataSource());
        removeInProgressSurveyUseCase.execute(new RemoveSurveysInProgressUseCase.Callback() {
            @Override
            public void onSuccess() {
                mDashboardActivityStrategy.newSurvey(activity);
            }
        });
    }

    /**
     * Called when the user clicks the exit Review button
     * @param fromReviewList
     * @param surveyUid
     * @param afterCompletion
     */
    public void exitReview(boolean fromReviewList, String surveyUid, boolean afterCompletion) {
        mDashboardActivityStrategy.exitReview(fromReviewList, surveyUid, afterCompletion);
    }

    public void reviewSurvey(View view) {
        reviewSurvey();
    }


    private void reviewSurvey() {
        DashboardActivity.moveToThisUId = (Session.getMalariaSurveyDB().getValuesFromDB().get(
                0).getQuestionDB()).getUid();
        hideReview();
    }

    /**
     * Called when the user clicks in other tab
     */
    public void exitReviewOnChangeTab(View view) {
        ScoreRegister.clear();
        beforeExit();
        closeReviewFragment();
    }

    /**
     * Show a final dialog to announce the survey is over
     * @param fromSurveysList
     */
    public void reviewShowDone(boolean fromSurveysList) {
        if (!fromSurveysList) {
            showSendSurveyDialog();
        } else {
            DynamicTabAdapter.isClicked = false;
            exitReviewScreen();
        }
    }

    private void showSendSurveyDialog() {
        AlertDialog.Builder msgConfirmation = new AlertDialog.Builder(this)
                .setTitle(translate(R.string.survey_completed))
                .setMessage(translate(R.string.survey_completed_text))
                .setCancelable(false)
                .setPositiveButton(translate(R.string.survey_send),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        //sendSurvey();
                        DynamicTabAdapter.isClicked = false;
                    }
                });
        msgConfirmation.setNegativeButton(
                translate(R.string.survey_review),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        reviewSurvey();
                        DynamicTabAdapter.isClicked = false;
                    }
                });

        msgConfirmation.create().show();
    }

    private void exitReviewScreen() {
        if (!DynamicTabAdapter.isClicked) {
            DynamicTabAdapter.isClicked = true;
            closeSurveyFragment();
            DynamicTabAdapter.isClicked = false;
        }
    }

    /**
     * Checks if a survey fragment is active
     */
    private boolean isReviewFragmentActive() {
        return isFragmentActive(reviewFragment, mDashboardActivityStrategy.getSurveyContainer());
    }

    /**
     * Checks if a survey fragment is active
     */
    public boolean isSurveyFragmentActive() {
        return isFragmentActive(surveyFragment, mDashboardActivityStrategy.getSurveyContainer());
    }

    private boolean isNewHistoricReceiptBalanceFragmentActive() {
        return mDashboardActivityStrategy.isHistoricNewReceiptBalanceFragment(this);
    }

    private boolean isStockTableFragmentActive() {
        return mDashboardActivityStrategy.isStockTableFragmentActive(this);
    }

    private boolean isFragmentActive(Class fragmentClass, int layout) {
        Fragment currentFragment = this.getFragmentManager().findFragmentById(layout);
        if (currentFragment.getClass().equals(fragmentClass)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a dashboardUnsentFragment is active
     */
    public boolean isFragmentActive(Fragment fragment, int layout) {
        Fragment currentFragment = this.getFragmentManager().findFragmentById(layout);
        if (currentFragment != null && currentFragment.equals(fragment)) {
            return true;
        }
        return false;
    }

    /**
     * This method moves to the Assess tab and open the active survey.
     */
    public void openSentSurvey() {
        isReadOnly = true;
        mDashboardActivityStrategy.openSentSurvey();
    }

    /**
     * This method hide the reviewFragment restoring the Assess tab with the active SurveyFragment
     */
    public void hideReview(String questionUId) {
        moveToThisUId = questionUId;
        hideReview();
    }

    /**
     * This method hide the reviewFragment restoring the Assess tab with the active SurveyFragment
     */
    public void hideReview() {
        isMoveToLeft = true;
        restoreAssess();
    }

    public void setLoadingReview(boolean loadingReview) {
        isLoadingReview = loadingReview;
    }

    public boolean isLoadingReview() {
        return isLoadingReview;
    }

    public void completeSurvey() {
        mDashboardActivityStrategy.completeSurvey();
        closeSurveyFragment();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferencesState.getInstance().onCreateActivityPreferences(getResources(), getTheme());

        handler = new Handler(Looper.getMainLooper());
        mDashboardActivityStrategy = new DashboardActivityStrategy(this);
        mDashboardActivityStrategy.onCreate();
        dashboardActivity = this;
        setContentView(R.layout.tab_dashboard);
        if (savedInstanceState == null) {
            initImprove();
            if(GradleVariantConfig.isMonitoringFragmentActive()) {
                initMonitor();
            }else{
                mDashboardActivityStrategy.hideMonitoring();
            }
            if (GradleVariantConfig.isStockFragmentActive()) {
                initStock();
            }
            if (GradleVariantConfig.isAVFragmentActive()) {
                initAV();
            }
            if(GradleVariantConfig.isStockControlActive()){
                initStockControlFragment();
            }
            initAssess();
        }
        initTabHost(savedInstanceState);
        mDashboardActivityStrategy.initTabWidget(tabHost,reviewFragment,surveyFragment,isReadOnly);



        if (BuildConfig.multiuser) {
            try {
                initNavigationController();
            } catch (LoadingNavigationControllerException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initStockControlFragment() {
        mDashboardActivityStrategy.initStockControlFragment();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "AndroidLifeCycle: onStart");
        super.onStart();
        mDashboardActivityStrategy.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "AndroidLifeCycle: onDestroy");
        super.onDestroy();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        Log.d(TAG, String.format("onActivityResult(%d, %d)", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, data);

        //Delegate activity result to media controller
        mDashboardActivityStrategy.onActivityResult(requestCode, resultCode, data);
    }

    private void initNavigationController() throws LoadingNavigationControllerException {
        mDashboardActivityStrategy.initNavigationController();
    }

    public void executeLogout() {
        LogoutUseCase logoutUseCase =
                new AuthenticationFactoryStrategy().getLogoutUseCase(this);
        AlarmPushReceiver.cancelPushAlarm(this);
        logoutUseCase.execute(new LogoutUseCase.Callback() {
            @Override
            public void onLogoutSuccess() {
                DashboardActivityStrategy.onLogoutSuccess();
            }

            @Override
            public void onLogoutError(String message) {
                Log.e("." + this.getClass().getSimpleName(), message);
            }
        });
    }

    public void closeUser() {
        AnnouncementMessageDialog.closeUser(R.string.admin_announcement,
                PreferencesState.getInstance().getContext().getString(R.string.user_close),
                DashboardActivity.dashboardActivity);
    }

    public void refreshStatus() {
        mDashboardActivityStrategy.reloadFirstFragmentHeader();
        mDashboardActivityStrategy.onConnectivityStatusChange();
    }


    public TabHost getTabHost() {
        return tabHost;
    }

    public String translate(@StringRes int id){
        return Utils.getInternationalizedString(id, this);
    }
}
