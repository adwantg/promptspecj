package io.promptspecj.maven;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidateMojoTest {
    @Test
    void mojoClassesArePresent() {
        assertThat(ValidateMojo.class).isNotNull();
        assertThat(GenerateMojo.class).isNotNull();
    }
}
