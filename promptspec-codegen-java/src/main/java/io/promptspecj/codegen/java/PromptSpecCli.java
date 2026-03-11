package io.promptspecj.codegen.java;

import io.promptspecj.model.ToolRegistryDocument;
import io.promptspecj.parser.ParsedDocument;
import io.promptspecj.parser.PromptSpecParser;
import io.promptspecj.validator.PromptDiagnostic;
import io.promptspecj.validator.PromptSpecValidator;
import io.promptspecj.validator.ValidationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class PromptSpecCli {
    private PromptSpecCli() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: validate <contractsDir> <lockfile> | generate <contractsDir> <outputDir> <basePackage>");
        }

        String command = args[0];
        Path contractsDir = Path.of(args[1]);
        if (!Files.exists(contractsDir)) {
            System.out.println("No PromptSpec directory found at " + contractsDir + "; skipping.");
            return;
        }

        PromptSpecParser parser = new PromptSpecParser();
        PromptSpecValidator validator = new PromptSpecValidator();
        ToolRegistryDocument registry = loadToolRegistry(parser, contractsDir);
        List<Path> contracts = Files.list(contractsDir)
                .filter(path -> path.getFileName().toString().endsWith(".promptspec.yaml")
                        || path.getFileName().toString().endsWith(".promptspec.json"))
                .sorted()
                .toList();

        if (contracts.isEmpty()) {
            System.out.println("No PromptSpec contracts found in " + contractsDir + "; skipping.");
            return;
        }

        switch (command) {
            case "validate" -> validateContracts(contracts, registry, validator, Path.of(args[2]));
            case "generate" -> generateContracts(contracts, registry, parser, validator, Path.of(args[2]), args[3]);
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private static void validateContracts(
            List<Path> contracts, ToolRegistryDocument registry, PromptSpecValidator validator, Path lockfile)
            throws IOException {
        PromptSpecParser parser = new PromptSpecParser();
        for (Path contract : contracts) {
            ParsedDocument parsed = parser.parse(contract);
            ValidationResult result = validator.validate(contract, parsed.document(), registry, lockfile);
            failIfInvalid(result);
            validator.lockfileWriter().write(lockfile, validator.lockfileWriter().create(parsed.document().prompts()));
        }
    }

    private static void generateContracts(
            List<Path> contracts,
            ToolRegistryDocument registry,
            PromptSpecParser parser,
            PromptSpecValidator validator,
            Path outputDirectory,
            String basePackage)
            throws IOException {
        JavaPromptSpecGenerator generator = new JavaPromptSpecGenerator();
        for (Path contract : contracts) {
            ParsedDocument parsed = parser.parse(contract);
            ValidationResult result = validator.validate(contract, parsed.document(), registry, null);
            failIfInvalid(result);
            generator.generate(new GenerationRequest(parsed.document(), basePackage, outputDirectory));
        }
    }

    private static ToolRegistryDocument loadToolRegistry(PromptSpecParser parser, Path contractsDir) {
        Path registryFile = contractsDir.resolve("tools.yaml");
        return Files.exists(registryFile) ? parser.parseToolRegistry(registryFile) : null;
    }

    private static void failIfInvalid(ValidationResult result) {
        if (result.isSuccessful()) {
            return;
        }
        StringBuilder message = new StringBuilder("PromptSpec validation failed:\n");
        for (PromptDiagnostic diagnostic : result.diagnostics()) {
            message.append(diagnostic.code()).append(": ").append(diagnostic.message()).append('\n');
        }
        throw new IllegalStateException(message.toString());
    }
}
