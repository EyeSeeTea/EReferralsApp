package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class QuestionLocalDataSource implements IQuestionRepository {
    @Override
    public List<Question> getQuestionsByProgram(String programUID) {
        List<QuestionDB> questionDBS = QuestionDB.listByProgram(
                ProgramDB.getProgram(programUID));

        List<Question> questions = new ArrayList<>();

        for (QuestionDB questionDB : questionDBS) {
            questions.add(
                    Question.newBuilder()
                            .id(questionDB.getId_question())
                            .code(questionDB.getCode())
                            .name(questionDB.getInternationalizedForm_name())
                            .uid(questionDB.getUid())
                            .type(mapOutputToQuestionType(questionDB.getOutput()))
                            .build());
        }
        return questions;
    }

    public static Question.Type mapOutputToQuestionType(int output) {
        Question.Type questionType;

        switch (output) {
            case Constants.INT:
                questionType = Question.Type.INT;
                break;
            case Constants.LONG_TEXT:
                questionType = Question.Type.LONG_TEXT;
                break;
            case Constants.SHORT_TEXT:
                questionType = Question.Type.DROPDOWN_LIST;
                break;
            case Constants.DATE:
                questionType = Question.Type.DATE;
                break;
            case Constants.POSITIVE_INT:
                questionType = Question.Type.POSITIVE_INT;
                break;
            case Constants.NO_ANSWER:
                questionType = Question.Type.NO_ANSWER;
                break;
            case Constants.RADIO_GROUP_HORIZONTAL:
                questionType = Question.Type.RADIO_GROUP_HORIZONTAL;
                break;
            case Constants.RADIO_GROUP_VERTICAL:
                questionType = Question.Type.RADIO_GROUP_VERTICAL;
                break;
            case Constants.IMAGES_2:
                questionType = Question.Type.IMAGES_2;
                break;
            case Constants.IMAGES_4:
                questionType = Question.Type.IMAGES_4;
                break;
            case Constants.IMAGES_6:
                questionType = Question.Type.IMAGES_6;
                break;
            case Constants.PHONE:
                questionType = Question.Type.PHONE;
                break;
            case Constants.IMAGES_3:
                questionType = Question.Type.IMAGES_3;
                break;
            case Constants.IMAGES_5:
                questionType = Question.Type.IMAGES_5;
                break;
            case Constants.COUNTER:
                questionType = Question.Type.COUNTER;
                break;
            case Constants.WARNING:
                questionType = Question.Type.WARNING;
                break;
            case Constants.REMINDER:
                questionType = Question.Type.REMINDER;
                break;
            case Constants.DROPDOWN_OU_LIST:
                questionType = Question.Type.DROPDOWN_OU_LIST;
                break;
            case Constants.IMAGE_3_NO_DATAELEMENT:
                questionType = Question.Type.IMAGE_3_NO_DATAELEMENT;
                break;
            case Constants.HIDDEN:
                questionType = Question.Type.HIDDEN;
                break;
            case Constants.SWITCH_BUTTON:
                questionType = Question.Type.SWITCH_BUTTON;
                break;
            case Constants.QUESTION_LABEL:
                questionType = Question.Type.QUESTION_LABEL;
                break;
            case Constants.IMAGE_RADIO_GROUP_NO_DATAELEMENT:
                questionType = Question.Type.IMAGE_RADIO_GROUP_NO_DATAELEMENT;
                break;
            case Constants.IMAGE_RADIO_GROUP:
                questionType = Question.Type.IMAGE_RADIO_GROUP;
                break;
            case Constants.POSITIVE_OR_ZERO_INT:
                questionType = Question.Type.POSITIVE_OR_ZERO_INT;
                break;
            case Constants.DYNAMIC_TREATMENT_SWITCH_NUMBER:
                questionType = Question.Type.DYNAMIC_TREATMENT_SWITCH_NUMBER;
                break;
            case Constants.DYNAMIC_STOCK_IMAGE_RADIO_BUTTON:
                questionType = Question.Type.DYNAMIC_STOCK_IMAGE_RADIO_BUTTON;
                break;
            case Constants.PREGNANT_MONTH_INT:
                questionType = Question.Type.PREGNANT_MONTH_INT;
                break;
            case Constants.YEAR:
                questionType = Question.Type.YEAR;
                break;
            case Constants.DROPDOWN_LIST_OU_TREE:
                questionType = Question.Type.DROPDOWN_LIST_OU_TREE;
                break;
            default:
                throw new IllegalArgumentException("There are not domain type for output" + output);
        }

        return questionType;
    }
}
