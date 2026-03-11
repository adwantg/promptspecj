package io.promptspecj.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.promptspecj.model.OutputSpec;
import io.promptspecj.model.PromptSpecDefinition;
import io.promptspecj.runtime.PromptLockEntry;
import io.promptspecj.runtime.PromptLockfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public final class PromptLockfileWriter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PromptLockfile create(List<PromptSpecDefinition> prompts) {
        return new PromptLockfile(prompts.stream()
                .map(prompt -> new PromptLockEntry(
                        prompt.id(),
                        prompt.version(),
                        hash(prompt.template().value()),
                        hash(outputSignature(prompt.output()))))
                .toList());
    }

    public void write(Path target, PromptLockfile lockfile) throws IOException {
        Files.createDirectories(target.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), lockfile);
    }

    private String outputSignature(OutputSpec outputSpec) {
        if ("java-type".equals(outputSpec.mode())) {
            return outputSpec.javaType();
        }
        return outputSpec.jsonSchema() == null ? outputSpec.mode() : outputSpec.jsonSchema().toPrettyString();
    }

    public static String hash(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest((source == null ? "" : source).getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
