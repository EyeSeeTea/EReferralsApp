package org.eyeseetea.malariacare.data.sync.importer;

import org.eyeseetea.malariacare.data.sync.importer.models.OrgUnitTree;
import org.eyeseetea.malariacare.data.sync.importer.models.Version;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OrganisationUnitTreeApiClient {

    @GET("api/dataStore/{namespace}/{key}")
    Call<List<OrgUnitTree>> getOrgUnitsTree(@Path("namespace") String namespace,
            @Path("key") String key);

    @GET("api/dataStore/{namespace}/{key}")
    Call<Version> getMetadataVersion(@Path("namespace") String namespace,
            @Path("key") String key);
}
