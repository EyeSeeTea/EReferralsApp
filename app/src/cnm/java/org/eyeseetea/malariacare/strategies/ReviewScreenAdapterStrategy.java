package org.eyeseetea.malariacare.strategies;

import android.graphics.Color;
import android.view.View;
import android.widget.TableRow;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Value;
import org.eyeseetea.malariacare.layout.adapters.dashboard.ReviewScreenAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.sdk.presentation.views.CustomTextView;

public class ReviewScreenAdapterStrategy extends AReviewScreenAdapterStrategy {
    ReviewScreenAdapter.onClickListener onClickListener;

    public ReviewScreenAdapterStrategy(ReviewScreenAdapter.onClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public TableRow createViewRow(TableRow rowView, Value value, int position) {
        //Sets the value text in the row and add the question as tag.
        CustomTextView textCard = (CustomTextView) rowView.findViewById(R.id.review_content_text);
        textCard.setText((value.getInternationalizedName() != null) ? value.getInternationalizedName()
                : value.getValue());
        if (textCard.getText().equals("")) {
            textCard.setText(PreferencesState.getInstance().getContext().getString(
                    R.string.empty_review_value));
        }
        if ((value.getQuestionUId() != null)) {
            textCard.setTag(value.getQuestionUId());

            //Adds click listener to hide the fragment and go to the clicked question.
            textCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!DynamicTabAdapter.isClicked) {
                        DynamicTabAdapter.isClicked = true;
                        String questionUId = (String) v.getTag();
                        onClickListener.onClickOnValue(questionUId);
                    }
                }
            });
            textCard.setBackgroundColor(
                    Color.parseColor(value.getBackgroundColor()));

        }
        return rowView;
    }

    public static boolean shouldShowReviewScreen(){
        return true;
    }

}
