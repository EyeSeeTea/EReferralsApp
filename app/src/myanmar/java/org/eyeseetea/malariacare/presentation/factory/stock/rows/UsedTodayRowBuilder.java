package org.eyeseetea.malariacare.presentation.factory.stock.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.presentation.factory.stock.utils.SurveyStock;

/**
 * Created by manuel on 29/12/16.
 */

public class UsedTodayRowBuilder extends CounterRowBuilder {
    public UsedTodayRowBuilder(Context context) {
        super(context.getResources().getString(R.string.used_today), context);
    }


    @Override
    protected float incrementCount(SurveyStock survey, float newValue) {
        return survey.isExpenseSurvey() ? newValue : 0;
    }
}
