package io.promptspecj.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BasicOutputNormalizerTest {
    @Test
    void normalizesWhitespaceAndLineEndings() {
        BasicOutputNormalizer normalizer = new BasicOutputNormalizer();
        assertThat(normalizer.normalize(" hello\r\nworld \n")).isEqualTo("hello\nworld");
    }
}
