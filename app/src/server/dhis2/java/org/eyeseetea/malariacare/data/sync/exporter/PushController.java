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

package org.eyeseetea.malariacare.data.sync.exporter;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.authentication.AuthenticationManager;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.PushDhisSDKDataSource;
import org.eyeseetea.malariacare.data.sync.exporter.strategies.APushControllerStrategy;
import org.eyeseetea.malariacare.data.sync.exporter.strategies.PushControllerStrategy;
import org.eyeseetea.malariacare.data.sync.importer.models.EventExtended;
import org.eyeseetea.malariacare.domain.boundary.IAuthenticationManager;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.ApiCallException;
import org.eyeseetea.malariacare.domain.exception.ClosedUserPushException;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.ConvertedEventsToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.ForceHardcodedLoginOnPushException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.SurveysToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.push.PushDhisException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.network.ServerAPIController;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;
import java.util.Map;

/**
 * A static controller that orchestrate the push process
 */
public class PushController implements IPushController {

    private final String TAG = ".PushControllerB&D";

    private Context mContext;
    private PushDhisSDKDataSource mPushDhisSDKDataSource;
    private IAuthenticationManager mAuthenticationManager;
    private ConvertToSDKVisitor mConvertToSDKVisitor;
    private APushControllerStrategy mPushControllerStrategy;


    public PushController(Context context, IAuthenticationManager authenticationManager) {
        mContext = context;
        mPushDhisSDKDataSource = new PushDhisSDKDataSource();
        mAuthenticationManager = authenticationManager;
        mConvertToSDKVisitor = new ConvertToSDKVisitor(mContext);
        mPushControllerStrategy = new PushControllerStrategy();
    }

    public void push(final IPushControllerCallback callback) {

        if (!ServerAPIController.isNetworkAvailable()) {
            Log.d(TAG, "No network");
            callback.onError(new NetworkException());
        } else {
            Log.d(TAG, "Network connected");

            final List<SurveyDB> surveyDBs = mPushControllerStrategy.getSurveysToPush();
            Boolean isUserClosed = false;

            UserDB loggedUserDB = UserDB.getLoggedUser();
            if (loggedUserDB != null && loggedUserDB.getUid() != null) {
                try {
                isUserClosed = ServerAPIController.isUserClosed(UserDB.getLoggedUser().getUid());
                } catch (ApiCallException e) {
                    isUserClosed = null;
            }
            }

            if(isUserClosed==null){
                callback.onError(new ApiCallException("The user api call returns a exception"));
                return;
            }
            if (isUserClosed) {
                Log.d(TAG, "The user is closed, Surveys not sent");
                callback.onError(new ClosedUserPushException());
            } else {
                if (surveyDBs == null || surveyDBs.size() == 0) {
                    callback.onError(new SurveysToPushNotFoundException("Null surveys"));
                    return;
                }

                hardcodedLogin(Session.getCredentials().getServerURL(), new IAuthenticationManager.Callback() {
                            @Override
                            public void onSuccess(Object result) {
                                Log.d(TAG, "wipe events");
                                mPushDhisSDKDataSource.wipeEvents();
                                try {
                                    Log.d(TAG, "convert surveys to sdk");
                                    convertToSDK(surveyDBs);
                                } catch (Exception ex) {
                                    callback.onError(new ConversionException(ex));
                                    return;
                                }

                                if (EventExtended.getAllEvents().size() == 0) {
                                    callback.onError(new ConvertedEventsToPushNotFoundException());
                                    return;
                                } else {
                                    Log.d(TAG, "push data");
                                    pushData(callback);
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                callback.onError(new ForceHardcodedLoginOnPushException(throwable.getMessage()));
                            }
                        });
            }
        }
    }

    @Override
    public boolean isPushInProgress() {
        return PreferencesState.getInstance().isPushInProgress();
    }

    @Override
    public void changePushInProgress(boolean inProgress) {
        PreferencesState.getInstance().setPushInProgress(inProgress);
    }

    private void pushData(final IPushControllerCallback callback) {
        mPushDhisSDKDataSource.pushData(
                new IDataSourceCallback<Map<String, PushReport>>() {
                    @Override
                    public void onSuccess(
                            Map<String, PushReport> mapEventsReports) {
                        if(mapEventsReports==null || mapEventsReports.size()==0){
                            onError(new PushReportException("EventReport is null or empty"));
                            return;
                        }
                        try {
                            mConvertToSDKVisitor.saveSurveyStatus(mapEventsReports, callback);
                            callback.onComplete();
                        }catch (Exception e){
                            onError(new PushReportException(e));
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable instanceof PushReportException
                                || throwable instanceof PushDhisException) {
                            mConvertToSDKVisitor.setSurveysAsQuarantine();
                        }
                        callback.onError(throwable);
                    }
                });
    }

    /**
     * Launches visitor that turns an APP survey into a SDK event
     */
    private void convertToSDK(List<SurveyDB> surveyDBs) throws ConversionException {
        Log.d(TAG, "Converting APP survey into a SDK event");
        for (SurveyDB surveyDB : surveyDBs) {
            surveyDB.setStatus(Constants.SURVEY_SENDING);
            surveyDB.save();
            Log.d(TAG, "Status of survey to be push is = " + surveyDB.getStatus());
            surveyDB.accept(mConvertToSDKVisitor);
        }
    }



    private void hardcodedLogin(String url, final AuthenticationManager.Callback callback) {
        mAuthenticationManager.hardcodedLogin(url,
                new IAuthenticationManager.Callback<UserAccount>() {

                    @Override
                    public void onSuccess(UserAccount userAccount) {
                        callback.onSuccess(userAccount);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }
}
