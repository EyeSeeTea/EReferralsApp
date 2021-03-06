package org.eyeseetea.malariacare.data.database.datasources.strategies;


import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.datasources.AuthenticationLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PopulateDBStrategy;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;

public class AuthenticationLocalDataSourceStrategy extends AAuthenticationLocalDataSourceStrategy {

    public AuthenticationLocalDataSourceStrategy(
            AuthenticationLocalDataSource authenticationLocalDataSource) {
        super(authenticationLocalDataSource);
    }

    @Override
    public void logout(IDataSourceCallback<Void> callback) {
        UserDB user = UserDB.getLoggedUser();

        if (user != null) {
            user.delete();
        }

        mAuthenticationLocalDataSource.clearCredentials();

        Session.logout();

        //reset org_unit
        PreferencesState.getInstance().saveStringPreference(R.string.org_unit,
                "");


        new PopulateDBStrategy().logoutWipe();

        mAuthenticationLocalDataSource.deleteOrgUnitQuestionOptions();

        callback.onSuccess(null);
    }
}
