package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LanguageShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void should_throw_exception_if_language_name_is_null() {
        thrown.expect(IllegalArgumentException.class);
        new Language("es", null);
    }

    @Test
    public void should_throw_exception_if_language_code_is_null() {
        thrown.expect(IllegalArgumentException.class);
        new Language(null, "Spanish");
    }

    @Test
    public void should_throw_exception_if_language_name_is_empty() {
        thrown.expect(IllegalArgumentException.class);
        new Language("es", "");
    }

    @Test
    public void should_throw_exception_if_language_code_is_empty() {
        thrown.expect(IllegalArgumentException.class);
        new Language("", "Spanish");
    }

    @Test
    public void should_return_language_code_and_name_correctly() {
        Language language = new Language("es", "Spanish");
        assertThat(language.getCode(), is("es"));
        assertThat(language.getName(),is("Spanish"));
    }

}
