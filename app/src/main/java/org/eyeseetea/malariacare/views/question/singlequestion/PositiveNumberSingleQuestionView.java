package org.eyeseetea.malariacare.views.question.singlequestion;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.PositiveNumber;
import org.eyeseetea.malariacare.domain.entity.Validation;
import org.eyeseetea.malariacare.domain.exception.InvalidPositiveNumberException;
import org.eyeseetea.malariacare.views.question.AKeyboardSingleQuestionView;
import org.eyeseetea.malariacare.views.question.IQuestionView;
import org.eyeseetea.malariacare.views.question.singlequestion.strategies.ANumberSingleQuestionViewStrategy;
import org.eyeseetea.malariacare.views.question.singlequestion.strategies.NumberSingleQuestionViewStrategyStrategy;
import org.eyeseetea.sdk.presentation.views.CustomButton;
import org.eyeseetea.sdk.presentation.views.CustomEditText;

public class PositiveNumberSingleQuestionView extends AKeyboardSingleQuestionView implements
        IQuestionView {
    CustomEditText numberPicker;
    CustomButton sendButton;
    private ANumberSingleQuestionViewStrategy mPositiveANumberSingleQuestionViewStrategy;

    public PositiveNumberSingleQuestionView(Context context) {
        super(context);
        mPositiveANumberSingleQuestionViewStrategy =
                new NumberSingleQuestionViewStrategyStrategy();
        init(context);
    }

    @Override
    public EditText getAnswerView() {
        return numberPicker;
    }

    @Override
    public boolean isEnabled(){
        return numberPicker.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        numberPicker.setEnabled(enabled);
        sendButton.setEnabled(enabled);

        if (enabled) {
            showKeyboard(numberPicker);
        }
    }

    @Override
    public void setHelpText(String helpText) {
        numberPicker.setHint(helpText);
    }

    @Override
    public void setValue(ValueDB valueDB) {
        if (valueDB != null) {
            numberPicker.setText(valueDB.getValue());
        }
    }

    private void init(final Context context) {
        inflate(context, R.layout.dynamic_tab_positiveint_row, this);

        numberPicker = (CustomEditText) findViewById(R.id.answer);
        numberPicker.setFocusable(true);
        numberPicker.setFocusableInTouchMode(true);

        Validation.getInstance().addInput(numberPicker);
        sendButton = (CustomButton) findViewById(R.id.dynamic_positiveInt_btn);

        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAnswer(context);
            }
        });

        numberPicker.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    action(context);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void setQuestionDB(QuestionDB questionDB) {
        mPositiveANumberSingleQuestionViewStrategy.setQuestionDB(this, questionDB);
    }

    @Override
    public void validateAnswer(Context context) {
        try {
            PositiveNumber positiveNumber = PositiveNumber.parse(
                    numberPicker.getText().toString());
            if(validateQuestionRegExp(numberPicker)) {
                Validation.getInstance().removeInputError(numberPicker);
                hideKeyboard(numberPicker);
                notifyAnswerChanged(String.valueOf(positiveNumber.getValue()));
            }
        } catch (InvalidPositiveNumberException e) {
            Validation.getInstance().addinvalidInput(numberPicker,
                    context.getString(R.string.dynamic_error_invalid_positive_number));
            numberPicker.setError(context.getString(R.string.dynamic_error_invalid_positive_number));
        }
    }
}
