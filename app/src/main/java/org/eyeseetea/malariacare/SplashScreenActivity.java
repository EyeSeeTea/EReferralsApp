package org.eyeseetea.malariacare;

import static org.eyeseetea.malariacare.BuildConfig.maxDaysForDeletingSentSurveys;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.PostMigration;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.populatedb.PopulateDB;
import org.eyeseetea.malariacare.data.remote.SdkQueries;
import org.eyeseetea.malariacare.data.sync.importer.PullController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.exception.PostMigrationException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.strategies.SplashActivityStrategy;
import org.hisp.dhis.client.sdk.android.api.D2;

public class SplashScreenActivity extends Activity {


    public interface Callback {
        void onSuccess(boolean canEnterApp);
    }

    private static final String TAG = ".SplashScreenActivity";
    private SplashActivityStrategy splashActivityStrategy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "AndroidLifeCycle: onCreate");
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        splashActivityStrategy = new SplashActivityStrategy(this);
        splashActivityStrategy.init(new Callback() {
            @Override
            public void onSuccess(boolean canEnterApp) {
                if (canEnterApp) {
                    AsyncInitApplication asyncInitApplication = new AsyncInitApplication(activity);
                    asyncInitApplication.execute((Void) null);
                }
            }
        });
    }

    private void init() {
        D2.init(this);
        SdkQueries.createDBIndexes();
        //Added to execute a query in DB, because DBFLow doesn't do any migration until a query
        // is executed
        PopulateDB.initDBQuery();
        try {
            PostMigration.launchPostMigration();
        } catch (PostMigrationException e) {
            new AlertDialog.Builder(this)
                    .setTitle(getApplicationContext().getString(R.string.error_message))
                    .setCancelable(false)
                    .setMessage(getApplicationContext().getString(R.string.db_migration_error))
                    .setNeutralButton(android.R.string.ok, null).create().show();
        }

        if (!BuildConfig.multiuser) {
            Log.i(TAG, "Pull on SplashScreen ...");

            PullController pullController = new PullController(
                    getApplication().getApplicationContext());
            IAsyncExecutor asyncExecutor = new AsyncExecutor();
            IMainExecutor mainExecutor = new UIThreadExecutor();

            PullUseCase pullUseCase = new PullUseCase(pullController, asyncExecutor, mainExecutor);

            PullFilters pullFilters = new PullFilters();
            splashActivityStrategy.initPullFilters(pullFilters);
            splashActivityStrategy.executePull(pullUseCase, pullFilters);
        }

        if (BuildConfig.downloadLanguagesFromServer) {
            splashActivityStrategy.downloadLanguagesFromServer();
        }

        if (BuildConfig.performMaintenanceTasks) {
            performMaintenanceTasks();
        }

    }

    public class AsyncInitApplication extends AsyncTask<Void, Void, Void> {
        Activity activity;

        AsyncInitApplication(Activity activity) {
            this.activity = activity;
        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            init();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            splashActivityStrategy.finishAndGo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        splashActivityStrategy.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void performMaintenanceTasks() {
        SurveyDB.deleteOlderSentSurveys(maxDaysForDeletingSentSurveys);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "AndroidLifeCycle: onStop");
        super.onStop();
    }
    @Override
    protected void onPause() {
        Log.d(TAG, "AndroidLifeCycle: onPause");
        super.onPause();
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "AndroidLifeCycle: onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "AndroidLifeCycle: onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "AndroidLifeCycle: onStart");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "AndroidLifeCycle: onDestroy");
        super.onDestroy();
    }

    public static void clearIntentExtras(Activity activity, String bundleKey) {
        if(activity.getIntent().getExtras()!=null) {
            activity.getIntent().removeExtra(bundleKey);
        }
    }
}
