/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eyeseetea.malariacare.presentation.factory.monitor.utils;


import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;

/**
 * Decorator that tells if a survey has specific info
 * Created by arrizabalaga on 26/02/16.
 */
public class SurveyMonitor {

    /**
     * Id of treatment question
     */
    protected final static Long ID_QUESTION_TREATMENT = 11l;
    /**
     * Id of counter question
     */
    protected final static Long ID_QUESTION_COUNTER = 6l;
    /**
     * Id of first question is tested? (positive, negative, not tested)
     */
    final static Long ID_QUESTION_IS_RDT_TESTED = 1l;
    /**
     * Id of not tested
     */
    final static Long ID_OPTION_RDT_YES_TESTED = 1l;
    /**
     * Id of not tested
     */
    final static Long ID_OPTION_RDT_NOT_TESTED = 2l;
    /**
     * Id of specie question(test result)
     */
    final static Long ID_QUESTION_RDT_TEST_RESULT = 5l;
    /**
     * Id of negative  of rdt tst result
     */
    final static Long ID_OPTION_TEST_NEGATIVE = 9l;
    /**
     * Id of pv specie option  of rdt tst result
     */
    final static Long ID_OPTION_SPECIE_PF = 10l;
    /**
     * Id of pv specie option  of rdt tst result
     */
    final static Long ID_OPTION_SPECIE_PV = 11l;
    /**
     * Id of pf/pv (mixed) specie option  of rdt tst result
     */
    final static Long ID_OPTION_SPECIE_PFPV = 12l;
    /**
     * Id of reason question (pregnant, severe, denied, drug)
     */
    private final static Long ID_QUESTION_REASON = 2l;
    /**
     * Id of rdt stockout reason option
     */
    private final static Long ID_OPTION_RDT_STOCKOUT = 6l;
    /**
     * Id of Combined act treatment option
     */
    private final static Long ID_OPTION_TREATMENT_REFERER_HOSPITAL = 21l;
    /**
     * Id of ACT6x1 treatment option
     */
    private final static Long ID_OPTION_TREATMENT_ACT6X1 = 17l;
    /**
     * Id of  ACT6x2 treatment option
     */
    private final static Long ID_OPTION_TREATMENT_ACT6X2 = 18l;
    /**
     * Id of ACT6x3 treatment option
     */
    private final static Long ID_OPTION_TREATMENT_ACT6X3 = 19l;
    /**
     * Id of ACT6x4 treatment option
     */
    private final static Long ID_OPTION_TREATMENT_ACT6X4 = 20l;

    private SurveyDB mSurveyDB;


    public SurveyMonitor(SurveyDB surveyDB) {
        mSurveyDB = surveyDB;
    }

    public SurveyDB getSurveyDB() {
        return mSurveyDB;
    }

    /**
     * Tells if the given survey is tested
     */
    public boolean isSuspected() {
        return (isTested() || isNotTested());
    }

    /**
     * Tells if the given survey is not tested
     */
    public boolean isNotTested() {
        return ValueDB.findValue(ID_QUESTION_IS_RDT_TESTED, ID_OPTION_RDT_NOT_TESTED, mSurveyDB)
                != null;
    }

    /**
     * Tells if the given survey is tested
     */
    public boolean isTested() {
        return ValueDB.findValue(ID_QUESTION_IS_RDT_TESTED, ID_OPTION_RDT_YES_TESTED, mSurveyDB)
                != null;
    }

    /**
     * Tells if the given survey is Rated(the same of is tested in Lao).
     */
    public boolean isRated() {
        return isTested();
    }

    /**
     * Tells if the given survey test is negative
     */
    public boolean isNegative() {
        return ValueDB.findValue(ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_TEST_NEGATIVE, mSurveyDB)
                != null;
    }

    /**
     * Tells if the given survey rdt is tested but test is not negative
     */
    public boolean isPositive() {
        return (ValueDB.findValue(ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PF, mSurveyDB) != null
                || ValueDB.findValue(
                ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PV, mSurveyDB) != null
                || ValueDB.findValue(
                ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PFPV, mSurveyDB) != null);
    }

    /**
     * Tells if the given survey has Pf specie
     */
    public boolean isPf() {
        return ValueDB.findValue(ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PF, mSurveyDB) != null;
    }

    /**
     * Tells if the given survey has Pv specie
     */
    public boolean isPv() {
        return ValueDB.findValue(ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PV, mSurveyDB) != null;
    }

    /**
     * Tells if the given survey has Pf/Pv (mixed)  specie
     */
    public boolean isPfPv() {
        return ValueDB.findValue(ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PFPV, mSurveyDB) != null;
    }

    /**
     * Tells if the given survey has Pf/Pv (mixed) or Pv  specie
     */
    public boolean isReferral() {
        return (ValueDB.findValue(ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PFPV, mSurveyDB) != null
                || ValueDB.findValue(
                ID_QUESTION_RDT_TEST_RESULT, ID_OPTION_SPECIE_PV, mSurveyDB) != null);
    }

    /**
     * Tells if the given survey is not tested number of referrals(RDT testing)
     */
    public boolean isRDTTesting() {
        return ValueDB.findValue(ID_QUESTION_IS_RDT_TESTED, ID_OPTION_RDT_NOT_TESTED, mSurveyDB)
                != null;
    }

    /**
     * Tells if the given survey is PV or PV+PF or referred to hospital
     */
    public boolean isTreatment() {
        if (isReferral() || ValueDB.findValue(ID_QUESTION_TREATMENT,
                ID_OPTION_TREATMENT_REFERER_HOSPITAL, mSurveyDB)
                != null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Tells if the given survey is referred to hospital
     */
    public boolean isACTStockout() {
        return ValueDB.findValue(ID_QUESTION_TREATMENT, ID_OPTION_TREATMENT_REFERER_HOSPITAL,
                mSurveyDB)
                != null;
    }

    /**
     * Tells if the given survey not tested by stockout
     */
    public boolean isRDTStockout() {
        return ValueDB.findValue(ID_QUESTION_REASON, ID_OPTION_RDT_STOCKOUT, mSurveyDB) != null;
    }

    /**
     * Tells if the given survey treatment is act6x4
     */
    public boolean isACT6x4() {
        return OptionDB.findOption(ID_QUESTION_TREATMENT, ID_OPTION_TREATMENT_ACT6X4, mSurveyDB);
    }

    /**
     * Tells if the given survey treatment is act6x3
     */
    public boolean isACT6x3() {
        return OptionDB.findOption(ID_QUESTION_TREATMENT, ID_OPTION_TREATMENT_ACT6X3, mSurveyDB);
    }

    /**
     * Tells if the given survey treatment is act6x2
     */
    public boolean isACT6x2() {
        return OptionDB.findOption(ID_QUESTION_TREATMENT, ID_OPTION_TREATMENT_ACT6X2, mSurveyDB);
    }

    /**
     * Tells if the given survey treatment is act6x1
     */
    public boolean isACT6x1() {
        return OptionDB.findOption(ID_QUESTION_TREATMENT, ID_OPTION_TREATMENT_ACT6X1, mSurveyDB);
    }

    /**
     * Tells if the given survey is a RDT tested
     */
    public boolean isRDTs() {
        return OptionDB.findOption(ID_QUESTION_IS_RDT_TESTED, ID_OPTION_RDT_YES_TESTED, mSurveyDB);
    }

    /**
     * Returns the number of rtd tests for each survey
     */
    public Integer countRDT() {
        if (isRDTs()) {
            return testCounter() + 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns the invalid count rdts for each survey
     */
    public int testCounter() {
        ValueDB valueDB = ValueDB.findValue(ID_QUESTION_COUNTER, mSurveyDB);
        if (valueDB == null || valueDB.getValue() == null) {
            return 0;
        }
        return Integer.parseInt(valueDB.getValue());
    }


}
