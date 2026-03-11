package io.promptspecj.runtime;

public final class BasicOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String rawOutput) {
        if (rawOutput == null) {
            return "";
        }
        return rawOutput.replace("\r\n", "\n").trim();
    }
}
