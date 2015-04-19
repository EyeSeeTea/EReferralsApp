package org.eyeseetea.malariacare.layout;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.listeners.AutomaticTabListeners;
import org.eyeseetea.malariacare.layout.score.ANumDenRecord;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

public class AutomaticTabLayout {


    public static void generateAutomaticTab(MainActivity mainActivity, Tab tab, LayoutInflater inflater, GridLayout layoutParent, Option defaultOption) {
        //Iterator for background (odd and even)
        int iterBacks = 0;

        Log.i(".Layout", "Generate Headers");
        for (Header header: tab.getHeaders()){
            // First we introduce header text according to the template
            //Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(R.layout.headers, layoutParent, false);

            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setBackgroundResource(R.drawable.background_header);
            headerText.setText(header.getName());
            //Set Visibility to false until we check if it has any question visible
            headerView.setVisibility(View.GONE);
            layoutParent.addView(headerView);


            //Log.i(".Layout", "Reader questions for header " + header.toString());
            for (Question question : header.getQuestions()){
                View questionView = null;
                Value value = null;
                // Check previous existing value
                value = question.getValue(MainActivity.session.getSurvey());

                // The statement is present in every kind of question
                switch(question.getAnswer().getOutput()){
                    case Constants.DROPDOWN_LIST:
                        questionView = inflater.inflate(R.layout.ddl, layoutParent, false);
                        questionView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));

                        TextView statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(R.id.QuestionTag, question);
                        dropdown.setTag(R.id.HeaderViewTag, headerView);
                        dropdown.setTag(R.id.NumeratorViewTag, questionView.findViewById(R.id.num));
                        dropdown.setTag(R.id.DenominatorViewTag, questionView.findViewById(R.id.den));
                        dropdown.setTag(R.id.Tab, LayoutConfiguration.getTabsConfiguration().get(tab).getTabId());
                        dropdown.setTag(R.id.QuestionTypeTag, Constants.DROPDOWN_LIST);

                        // If the question has children, we load the denominator, else we hide the question
                        if (!question.hasParent()) {
                            if (question.hasChildren()) questionView.setBackgroundResource(R.drawable.background_parent);

                            denominator.setText(Utils.round(question.getDenominator_w()));
                            headerView.setVisibility(View.VISIBLE);

                            ScoreRegister.addRecord(question, 0F, question.getDenominator_w());
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        AutomaticTabListeners.createDropDownListener(tab, dropdown, mainActivity);

                        List<Option> optionList = question.getAnswer().getOptions();
                        optionList.add(0, defaultOption);
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
                        dropdown.setAdapter(adapter);

                        // In case value existed previously
                        if (value != null) dropdown.setSelection(optionList.indexOf(value.getOption()));
                        break;
                    case Constants.INT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.integer, Constants.INT);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.LONG_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.longtext, Constants.LONG_TEXT);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.SHORT_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.shorttext, Constants.SHORT_TEXT);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.SHORT_DATE: case Constants. LONG_DATE:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.date, Constants.SHORT_TEXT);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                }
                if (questionView != null) layoutParent.addView(questionView);
                iterBacks++;
            }
        }
    }

    private static View getView(int iterBacks, LayoutInflater inflater, GridLayout layoutParent, View headerView, Question question, Integer componentType, int questionType) {
        View questionView = inflater.inflate(componentType, layoutParent, false);
        questionView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));
        TextView statement = (TextView) questionView.findViewById(R.id.statement);
        statement.setText(question.getForm_name());
        EditText answerI = (EditText) questionView.findViewById(R.id.answer);
        answerI.setTag(R.id.QuestionTag, question);
        answerI.setTag(R.id.HeaderViewTag, headerView);
        answerI.setTag(R.id.QuestionTypeTag, questionType);

        // If the question has children, we load the denominator, else we hide the question
        if (!question.hasParent()) {
            //set header to visible
            headerView.setVisibility(View.VISIBLE);
        } else {
            questionView.setVisibility(View.GONE);
        }
        return questionView;
    }
}
