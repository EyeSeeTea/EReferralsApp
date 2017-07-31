package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Calendar;
import java.util.Date;

public class OrganisationUnitTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void organisationunit_isbanned_after_ban() {
        OrganisationUnit organisationUnit = new OrganisationUnit("", "", false);
        organisationUnit.ban();
        assertThat(organisationUnit.isBanned(), is(true));
    }

    @Test
    public void organisationunit_isbanned_constructor_banned() {
        OrganisationUnit organisationUnit = new OrganisationUnit("", "", true);
        assertThat(organisationUnit.isBanned(), is(true));
    }

    @Test
    public void organisationunit_no_banned_constructor_no_banned() {
        OrganisationUnit organisationUnit = new OrganisationUnit("", "", false);
        assertThat(organisationUnit.isBanned(), is(false));
    }

    @Test
    public void organisationunit_banned_constructor_previous_date() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date date = calendar.getTime();
        OrganisationUnit organisationUnit = new OrganisationUnit("", "", "", date);
        assertThat(organisationUnit.isBanned(), is(true));
    }

    @Test
    public void organisationunit_no_banned_constructor_future_date() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        Date date = calendar.getTime();
        OrganisationUnit organisationUnit = new OrganisationUnit("", "", "", date);
        assertThat(organisationUnit.isBanned(), is(false));
    }

    @Test
    public void throw_exception_if_UID_not_provided_first_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UID is required");

        new OrganisationUnit(null, "name", false);
    }

    @Test
    public void throw_exception_if_name_not_provided_first_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Name is required");

        new OrganisationUnit("uid", null, false);
    }


    @Test
    public void throw_exception_if_UID_not_provided_second_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UID is required");

        new OrganisationUnit(null, "name", "description", new Date());
    }

    @Test
    public void throw_exception_if_name_not_provided_second_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Name is required");

        new OrganisationUnit("uid", null, "description", new Date());
    }

    @Test
    public void throw_exception_if_description_not_provided_second_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Description is required");

        new OrganisationUnit("uid", "name", null, new Date());
    }

    @Test
    public void throw_exception_if_UID_not_provided_third_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("UID is required");

        new OrganisationUnit(null, "name", "code", "description", new Date(), "pin",
                new Program("code", "id"));
    }

    @Test
    public void throw_exception_if_name_not_provided_third_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Name is required");

        new OrganisationUnit("uid", null, "code", "description", new Date(), "pin",
                new Program("code", "id"));
    }

    @Test
    public void throw_exception_if_code_not_provided_third_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Code is required");

        new OrganisationUnit("uid", "name", null, "description", new Date(), "pin",
                new Program("code", "id"));
    }

    @Test
    public void throw_exception_if_description_not_provided_third_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Description is required");

        new OrganisationUnit("uid", "name", "code", null, new Date(), "pin",
                new Program("code", "id"));
    }

    @Test
    public void throw_exception_if_pin_not_provided_third_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Pin is required");

        new OrganisationUnit("uid", "name", "code", "description", new Date(), null,
                new Program("code", "id"));
    }

    @Test
    public void throw_exception_if_program_not_provided_third_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Program is required");

        new OrganisationUnit("uid", "name", "code", "description", new Date(), "pin", null);
    }

    @Test
    public void test_description_when_banning() {
        OrganisationUnit organisationUnit = new OrganisationUnit("uid", "name", false);
        organisationUnit.ban();
        assertThat(organisationUnit.getDescription(), is(not(equalTo(""))));
    }


}
