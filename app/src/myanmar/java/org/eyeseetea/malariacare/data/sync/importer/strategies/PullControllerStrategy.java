package org.eyeseetea.malariacare.data.sync.importer.strategies;

import org.eyeseetea.malariacare.data.remote.SdkQueries;
import org.eyeseetea.malariacare.data.sync.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.sync.importer.PullController;
import org.eyeseetea.malariacare.data.sync.importer.models.CategoryOptionGroupExtended;
import org.eyeseetea.malariacare.domain.boundary.IPullController;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.CategoryOptionGroupFlow;

public class PullControllerStrategy extends APullControllerStrategy {
    public PullControllerStrategy(PullController pullController) {
        super(pullController);
    }
    @Override
    public void convertMetadata(ConvertFromSDKVisitor converter,
            IPullController.Callback callback) {
        for (CategoryOptionGroupFlow categoryOptionGroupFlow : SdkQueries.getCategoryOptionGroups
                ()) {
            CategoryOptionGroupExtended categoryOptionGroupExtended =
                    new CategoryOptionGroupExtended(categoryOptionGroupFlow);
            categoryOptionGroupExtended.accept(converter);
        }
        callback.onComplete();
    }
}
