package org.eyeseetea.malariacare;


import static junit.framework.Assert.assertEquals;

import static org.eyeseetea.malariacare.configurationimporter
        .ConstantsMetadataConfigurationImporterTest.COUNTRIES_VERSION;
import static org.eyeseetea.malariacare.configurationimporter
        .ConstantsMetadataConfigurationImporterTest.COUNTRIES_VERSION_V2;
import static org.eyeseetea.malariacare.configurationimporter
        .ConstantsMetadataConfigurationImporterTest.TZ_CONFIG_ANDROID_2_0_JSON;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import android.content.Context;

import org.eyeseetea.malariacare.data.authentication.CredentialsReader;
import org.eyeseetea.malariacare.data.database.model.CountryVersionDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.PhoneFormatDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionOptionDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.populatedb.PopulateDB;
import org.eyeseetea.malariacare.data.server.CustomMockServer;
import org.eyeseetea.malariacare.data.sync.factory.ConverterFactory;
import org.eyeseetea.malariacare.data.sync.importer.metadata.configuration
        .MetadataConfigurationApiClient;
import org.eyeseetea.malariacare.data.sync.importer.metadata.configuration
        .MetadataConfigurationDBImporter;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.network.retrofit.BasicAuthInterceptor;
import org.eyeseetea.malariacare.rules.MockWebServerRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


public class MetadataConfigurationDBImporterShould {


    @Rule
    public MockWebServerRule mockWebServerRule = new MockWebServerRule(new AssetsFileReader());

    private final Program program = new Program("T_TZ", "low6qUS2wc9");

    @Before
    public void setUp() throws Exception {
        PopulateDB.wipeDataBase();
        CredentialsReader credentialsReader = CredentialsReader.getInstance();
        Context context = PreferencesState.getInstance().getContext();
        Session.setCredentials(
                new Credentials(context.getString(R.string.web_service_url), credentialsReader.getUser(),
                        credentialsReader.getPassword()));

    }

    @Test
    public void insert_questions_to_db_after_download_the_configurations() throws Exception {

        whenCountryConfigFilesAreReceived();

        whenConfigFilesAreParsed();

        thenAssertMetadataIsInsertedInTheDB();
    }

    @Test
    public void update_data_with_new_updated_configuration_version() throws Exception {

        givenADownloadedVersionOneOfConfigurationFile();
        givenADownloadedVersionTwoOfConfigurationFile();

        thenAssertConfigurationFileVersionHasIncreaseToVersionTwo();
    }

    private void givenADownloadedVersionOneOfConfigurationFile() throws Exception {

        whenCountryConfigFilesAreReceived();
        whenConfigFilesAreParsed();

        thenAssertMetadataIsInsertedInTheDB();
    }

    private void givenADownloadedVersionTwoOfConfigurationFile() throws Exception {

        whenCountryConfigFilesVersionTwoAreReceived();
        whenConfigFilesAreParsed();
    }


    private void whenCountryConfigFilesAreReceived() throws Exception {
        mockWebServerRule.getMockServer().enqueueMockResponse(COUNTRIES_VERSION);
        mockWebServerRule.getMockServer().enqueueMockResponse(TZ_CONFIG_ANDROID_2_0_JSON);
    }

    private void whenCountryConfigFilesVersionTwoAreReceived() throws Exception {
        mockWebServerRule.getMockServer().enqueueMockResponse(COUNTRIES_VERSION_V2);
        mockWebServerRule.getMockServer().enqueueMockResponse(TZ_CONFIG_ANDROID_2_0_JSON);
    }

    private void thenAssertMetadataIsInsertedInTheDB() {
        shouldBeInDB(17, 24, 37, 1, 1);
    }

    private void thenAssertConfigurationFileVersionHasIncreaseToVersionTwo() {
        CountryVersionDB countryVersionDB = CountryVersionDB.getCountryVersionByUID(
                program.getId());

                assertTrue(countryVersionDB.getVersion() == 2);
    }

    private void whenConfigFilesAreParsed() throws Exception {

        MetadataConfigurationApiClient apiClient = new MetadataConfigurationApiClient(
                mockWebServerRule.getMockServer().getBaseEndpoint(),"",
                new BasicAuthInterceptor(""));

        MetadataConfigurationDBImporter importer = new MetadataConfigurationDBImporter(
                apiClient, ConverterFactory.getQuestionConverter()
        );

        importer.importMetadata(program);
    }

    private void shouldBeInDB(int expectedQuestionsCount, int expectedQuestionsOptionsCount,
            int expectedOptionsCount, int expectedProgramsCount, int expectedPhoneFormatsCount) {

        int questionsCount = QuestionDB.getQuestionDBCount();
        int questionsOptionsCount = QuestionOptionDB.getQuestionOptionDBCount();
        int optionsCount = OptionDB.getOptionsDBCount();
        int programsCount = ProgramDB.getProgramsDBCount();
        int formatsCount = PhoneFormatDB.getPhoneFormatDBCount();

        assertEquals(expectedQuestionsCount,questionsCount);
        assertEquals(expectedQuestionsOptionsCount,questionsOptionsCount);
        assertEquals(expectedOptionsCount,optionsCount );
        assertEquals(expectedProgramsCount,programsCount);
        assertEquals(expectedPhoneFormatsCount,formatsCount);
    }
}
