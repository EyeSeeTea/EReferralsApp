package org.eyeseetea.malariacare.strategies;

import android.graphics.Color;
import android.view.View;
import android.widget.TableRow;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Value;
import org.eyeseetea.malariacare.layout.adapters.dashboard.ReviewScreenAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.sdk.presentation.views.CustomTextView;

public class ReviewScreenAdapterStrategy extends AReviewScreenAdapterStrategy {
    ReviewScreenAdapter.onClickListener onClickListener;

    public ReviewScreenAdapterStrategy(ReviewScreenAdapter.onClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    final String TITLE_SEPARATOR = ": ";

    public TableRow createViewRow(TableRow rowView, Value value, int position) {

        rowView.setTag(getCorrectQuestion(value.getQuestionUId()));

        //Sets the value text in the row and add the question as tag.
        CustomTextView questionTextView = (CustomTextView) rowView.findViewById(
                R.id.review_title_text);


        questionTextView.setText(questionTextView.getText().toString() +
                ((value.getInternationalizedCode() != null) ? value.getInternationalizedCode()
                        : value.getValue()));
        if ((value.getQuestionUId() != null)) {
            questionTextView.setText(
                    QuestionDB.findByUID(value.getQuestionUId()).getInternationalizedCodeDe_Name() + TITLE_SEPARATOR);
            //Adds click listener to hide the fragment and go to the clicked question.
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!DynamicTabAdapter.isClicked) {
                        DynamicTabAdapter.isClicked = true;
                        String questionUId = (String) v.getTag();
                        onClickListener.onClickOnValue(questionUId);
                    }
                }
            });

            questionTextView.setBackgroundColor(
                    Color.parseColor(value.getBackgroundColor()));
        }
        return rowView;
    }

    private QuestionDB getCorrectQuestion(String questionUId) {
        if (questionUId.equals(PreferencesState.getInstance().getContext().getString(
                R.string.dynamicTreatmentQuestionUID)) || questionUId.equals(
                PreferencesState.getInstance().getContext().getString(
                        R.string.referralQuestionUID))
                || questionUId.equals(
                PreferencesState.getInstance().getContext().getString(
                        R.string.treatmentDiagnosisVisibleQuestion))) {
            return QuestionDB.findByUID(PreferencesState.getInstance().getContext().getString(
                    R.string.dynamicTreatmentHideQuestionUID));
        }
        if (questionUId.equals(PreferencesState.getInstance().getContext().getString(
                R.string.outOfStockQuestionUID))) {
            return QuestionDB.findByUID(PreferencesState.getInstance().getContext().getString(
                    R.string.dynamicStockQuestionUID));
        }
        return QuestionDB.findByUID(questionUId);
    }

    public static boolean isValidValue(Value value) {
        if (Session.getStockSurveyDB()==null || value.getQuestionUId() == null) {
            return false;
        }
        for (org.eyeseetea.malariacare.data.database.model.ValueDB stockValue : Session.getStockSurveyDB().getValuesFromDB()) {
            if (stockValue.getQuestionDB() != null) {
                if (stockValue.getQuestionDB().getUid().equals(value.getQuestionUId())) {
                    return true;
                }
            }
        }
        return false;
    }

}
