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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.stkent.bugshaker.BugShaker;
import com.github.stkent.bugshaker.flow.dialog.AlertDialogType;
import com.github.stkent.bugshaker.github.GitHubConfiguration;
import com.raizlabs.android.dbflow.config.EyeSeeTeaGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.strategies.AEyeSeeTeaApplicationStrategy;
import org.eyeseetea.malariacare.strategies.EyeSeeTeaApplicationStrategy;
import org.eyeseetea.malariacare.utils.DBTranslator;
import org.eyeseetea.malariacare.utils.Permissions;
import org.eyeseetea.sdk.common.EyeSeeTeaSdk;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nacho on 04/08/15.
 */
public class EyeSeeTeaApplication extends Application {

    private static final String TAG = ".EyeSeeTeaApplication";
    public static Permissions permissions;

    private static boolean isAppInBackground = false;

    private static boolean isWindowFocused = false;

    private static boolean isBackPressed = false;

    private static EyeSeeTeaApplication mInstance;

    private AEyeSeeTeaApplicationStrategy mEyeSeeTeaApplicationStrategy;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mEyeSeeTeaApplicationStrategy = new EyeSeeTeaApplicationStrategy(this);
        mEyeSeeTeaApplicationStrategy.onCreate();
        mInstance = this;

        //Apply for Release build
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        } else {
            // Set to verbose logging of select and delete instructions in DBFlow
            FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
        }

        PreferencesState.getInstance().init(getApplicationContext());
        FlowConfig flowConfig = new FlowConfig
                .Builder(this)
                .addDatabaseHolder(EyeSeeTeaGeneratedDatabaseHolder.class)
                .build();
        FlowManager.init(flowConfig);
        //initBugShaker();
        initEyeSeeTeaSDK();
    }

    private void initEyeSeeTeaSDK() {
        EyeSeeTeaSdk.getInstance().init(new DBTranslator());
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "AndroidLifeCycle: onTerminate");
        super.onTerminate();
        FlowManager.destroy();
    }

    //// FIXME: 28/12/16
    //@Override
    public Class<? extends Activity> getMainActivity() {
        return DashboardActivity.class;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static EyeSeeTeaApplication getInstance() {
        return mInstance;
    }

    public boolean isAppInBackground() {
        return isAppInBackground;
    }

    public void setAppInBackground(boolean isAppInBackground) {
        EyeSeeTeaApplication.isAppInBackground = isAppInBackground;
    }

    public boolean isWindowFocused() {
        return isWindowFocused;
    }

    public void setIsWindowFocused(boolean isWindowFocused) {
        EyeSeeTeaApplication.isWindowFocused = isWindowFocused;
    }

    public boolean isBackPressed() {
        return isBackPressed;
    }

    public void setIsBackPressed(boolean isBackPressed) {
        EyeSeeTeaApplication.isBackPressed = isBackPressed;
    }

    private void initBugShaker() {
        BugShaker.get(this)
                .setEmailAddresses("someone@example.com")
                .setLoggingEnabled(BuildConfig.DEBUG)
                .setAlertDialogType(AlertDialogType.APP_COMPAT)
                .setGitHubInfo(new GitHubConfiguration(
                        "eyeseetea/EReferralsApp",
                        BuildConfig.GIT_HUB_BOT_TOKEN,
                        "eyeseeteabottest/snapshots",
                        "master"))
                .assemble()
                .start();
    }
}
