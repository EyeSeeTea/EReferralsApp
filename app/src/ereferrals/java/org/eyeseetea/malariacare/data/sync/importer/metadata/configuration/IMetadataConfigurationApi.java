package org.eyeseetea.malariacare.data.sync.importer.metadata.configuration;


import org.eyeseetea.malariacare.data.sync.importer.metadata.configuration.model
        .MetadataConfigurationsApi;
import org.eyeseetea.malariacare.data.sync.importer.metadata.configuration.model
        .MetadataCountryVersionApi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IMetadataConfigurationApi {

    @GET("{countryFolder}")
    Call<MetadataCountryVersionApi> getCountriesVersions(@Path("countryFolder") String countryFolder);

    @GET("{countryCode}")
    Call<MetadataConfigurationsApi> getConfiguration(@Path("countryCode") String countryCode);
}
