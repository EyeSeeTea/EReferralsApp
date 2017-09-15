package org.eyeseetea.malariacare.data.database.utils;

import android.content.Context;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.DrugCombinationDB;
import org.eyeseetea.malariacare.data.database.model.DrugDB;
import org.eyeseetea.malariacare.data.database.model.HeaderDB;
import org.eyeseetea.malariacare.data.database.model.MatchDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OptionAttributeDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.PartnerDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionOptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionRelationDB;
import org.eyeseetea.malariacare.data.database.model.QuestionThresholdDB;
import org.eyeseetea.malariacare.data.database.model.StringKeyDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.model.TranslationDB;
import org.eyeseetea.malariacare.data.database.model.TreatmentDB;
import org.eyeseetea.malariacare.data.database.model.TreatmentMatchDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.populatedb.FileCsvs;
import org.eyeseetea.malariacare.data.database.utils.populatedb.IPopulateDBStrategy;
import org.eyeseetea.malariacare.data.database.utils.populatedb.PopulateDB;
import org.eyeseetea.malariacare.data.database.utils.populatedb.PopulateRow;
import org.eyeseetea.malariacare.data.database.utils.populatedb.RelationsIdCsvDB;
import org.eyeseetea.malariacare.data.database.utils.populatedb.TreatmentTableOperations;
import org.eyeseetea.malariacare.data.sync.importer.OrgUnitToOptionConverter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PopulateDBStrategy implements IPopulateDBStrategy {

    public static List<Class<? extends BaseModel>> allMandatoryTables = Arrays.asList(
            UserDB.class,
            StringKeyDB.class,
            TranslationDB.class,
            ProgramDB.class,
            TabDB.class,
            HeaderDB.class,
            AnswerDB.class,
            OptionAttributeDB.class,
            OptionDB.class,
            QuestionDB.class,
            QuestionRelationDB.class,
            MatchDB.class,
            QuestionOptionDB.class,
            QuestionThresholdDB.class,
            DrugDB.class,
            PartnerDB.class,
            TreatmentDB.class,
            DrugCombinationDB.class,
            TreatmentMatchDB.class,
            OrgUnitDB.class
    );

    @Override
    public void init() throws IOException {
        FileCsvs fileCsvs = new FileCsvs();
        fileCsvs.saveCsvsInFileIfNeeded();
        TreatmentTableOperations treatmentTable = new TreatmentTableOperations();
        treatmentTable.generateTreatmentMatrixIFNeeded();
    }

    @Override
    public InputStream openFile(Context context, String table)
            throws IOException, FileNotFoundException {
        return context.openFileInput(table);
    }


    public static void updateOptions(Context context) throws IOException {
        List<OptionDB> optionToDelete = QuestionDB.getOptions(
                PreferencesState.getInstance().getContext().getString(
                        R.string.residenceVillageUID));
        for (OptionDB option : optionToDelete) {
            if (!option.getCode().equals(PreferencesState.getInstance().getContext().getString(
                    R.string.patientResidenceVillageOtherCode))) {
                option.delete();
            }
        }
        FileCsvs fileCsvs = new FileCsvs();
        fileCsvs.saveCsvFromAssetsToFile(PopulateDB.OPTIONS_CSV);
        List<OptionDB> options = OptionDB.getAllOptions();
        HashMap<Long, AnswerDB> answersIds = RelationsIdCsvDB.getAnswerFKRelationCsvDB(context);
        HashMap<Long, OptionAttributeDB> optionAttributeIds =
                RelationsIdCsvDB.getOptionAttributeIdRelationCsvDB(context);
        CSVReader reader = new CSVReader(
                new InputStreamReader(context.openFileInput(PopulateDB.OPTIONS_CSV)),
                PopulateDB.SEPARATOR, PopulateDB.QUOTECHAR);
        String line[];
        int i = 0;
        while ((line = reader.readNext()) != null) {
            if (i < options.size()) {
                PopulateRow.populateOption(line, answersIds, optionAttributeIds,
                        options.get(i)).save();
            } else {
                PopulateRow.populateOption(line, answersIds, optionAttributeIds,
                        null).insert();
            }
            i++;
        }

        List<OrgUnitDB> orgUnits = OrgUnitDB.getAllOrgUnit();
        for (OrgUnitDB orgUnit : orgUnits) {
            OptionDB option = new OptionDB();
            option.setCode(orgUnit.getName());
            option.setName(orgUnit.getUid());
            option.setFactor((float) 0);
            option.setId_option((long) 0);
            option.setAnswerDB(QuestionDB.getAnswer(
                    PreferencesState.getInstance().getContext().getString(
                            R.string.residenceVillageUID)));
            option.save();
        }
    }

    @Override
    public void createDummyOrganisationInDB() {
        PartnerDB testOrganisation = new PartnerDB();
        testOrganisation.setName(PreferencesState.getInstance().getContext().getString(
                R.string.test_partner_name));
        testOrganisation.setUid(PreferencesState.getInstance().getContext().getString(
                R.string.test_partner_uid));
        testOrganisation.insert();
    }



    @Override
    public void createDummyOrgUnitsDataInDB(Context context) {
        List<OrgUnitDB> orgUnits = OrgUnitDB.getAllOrgUnit();

        if (orgUnits.size() == 0) {
            try {
                PopulateDB.populateDummyData(context);
                OrgUnitToOptionConverter.convert();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void logoutWipe() {
        PopulateDB.wipeDataBase();
    }

    public static List<Class<? extends BaseModel>> getAllMandatoryTables() {
        return allMandatoryTables;
    }
}
