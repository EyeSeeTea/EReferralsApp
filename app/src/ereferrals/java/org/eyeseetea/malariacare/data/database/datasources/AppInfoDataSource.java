package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.model.CountryVersionDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.sync.importer.MetadataUpdater;
import org.eyeseetea.malariacare.domain.boundary.repositories.IAppInfoRepository;
import org.eyeseetea.malariacare.domain.entity.AppInfo;

import java.io.IOException;


public class AppInfoDataSource implements IAppInfoRepository {

    private static final String TAG = "AppInfoDataSource";

    @Override
    public AppInfo getAppInfo() {
        return new AppInfo(getMetadataVersion(), getConfigFileVersion(), getAppVersion());
    }

    @Override
    public void getAppInfo(IDataSourceCallback<AppInfo> callback) {
        callback.onSuccess(new AppInfo(getMetadataVersion(), getConfigFileVersion(), getAppVersion()));
    }

    @Override
    public void saveAppInfo(AppInfo appInfo) {
        //Not Implemented for this variant yet
    }

    private String getMetadataVersion() {
        try {
            return String.valueOf(MetadataUpdater.getPhoneCSVersion());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting metadata version: " + e.getMessage());
        }
        return null;
    }
    //TODO Replace  getMetadataVersion with this implentation
    public String getConfigFileVersion() {
           return String.valueOf(CountryVersionDB.getMetadataVersion());
    }

    private String getAppVersion() {
        Context context = PreferencesState.getInstance().getContext();
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting app version" + e.getMessage());
        }
        return String.valueOf(pInfo.versionCode);
    }
}